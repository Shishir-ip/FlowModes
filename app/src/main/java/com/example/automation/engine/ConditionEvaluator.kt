package com.example.automation.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import com.example.automation.services.NotificationEventHub
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.InterceptedNotification
import com.example.domain.models.NotificationField
import com.example.domain.models.NotificationMatchOperator
import org.json.JSONObject
import java.util.Calendar

object ConditionEvaluator {

    data class TriggerContext(
        val triggerType: ConditionType? = null,
        val extraData: Map<String, Any> = emptyMap(),
        val isManualRun: Boolean = false,
        val simulatedCalendar: Calendar? = null,
        val packageName: String? = null,
        val wifiSsid: String? = null
    )

    fun evaluateRoutineConditions(
        context: Context,
        conditions: List<AutomationCondition>,
        logic: ConditionLogic,
        triggerContext: TriggerContext
    ): Pair<Boolean, String> {
        if (conditions.isEmpty()) {
            return Pair(true, "No conditions specified")
        }

        if (triggerContext.isManualRun) {
            return Pair(true, "Manual user override")
        }

        val results = conditions.map { condition ->
            evaluateSingleCondition(context, condition, triggerContext)
        }

        val isSatisfied = when (logic) {
            ConditionLogic.ALL -> results.all { it.first }
            ConditionLogic.ANY -> results.any { it.first }
        }

        val summary = results.joinToString(" | ") { (success, reason) ->
            val icon = if (success) "\u2713" else "\u2715"
            "$icon $reason"
        }

        return Pair(isSatisfied, summary)
    }

    fun evaluateSingleCondition(
        context: Context,
        condition: AutomationCondition,
        triggerContext: TriggerContext
    ): Pair<Boolean, String> {
        return try {
            val config = JSONObject(condition.configJson.ifEmpty { "{}" })
            val (rawMatched, rawDescription) = evaluateInternal(context, condition.type, config, triggerContext)

            val finalMatched = if (condition.isNegated) !rawMatched else rawMatched
            val finalDescription = if (condition.isNegated) {
                "NOT ($rawDescription)"
            } else {
                rawDescription
            }

            Pair(finalMatched, finalDescription)
        } catch (e: Exception) {
            Pair(false, "Evaluation error: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun evaluateInternal(
        context: Context,
        type: ConditionType,
        config: JSONObject,
        triggerContext: TriggerContext
    ): Pair<Boolean, String> {
        when (type) {
            ConditionType.MANUAL_TRIGGER -> {
                return Pair(triggerContext.isManualRun, "Manual activation")
            }

            ConditionType.TIME_RANGE -> {
                val startH = config.optInt("startHour", 0)
                val startM = config.optInt("startMinute", 0)
                val endH = config.optInt("endHour", 23)
                val endM = config.optInt("endMinute", 59)

                val now = triggerContext.simulatedCalendar ?: Calendar.getInstance()
                val currentMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val startTotalMins = startH * 60 + startM
                val endTotalMins = endH * 60 + endM

                val inRange = if (startTotalMins <= endTotalMins) {
                    currentMins in startTotalMins..endTotalMins
                } else {
                    // Overnight window (e.g. 22:00 to 07:00)
                    currentMins >= startTotalMins || currentMins <= endTotalMins
                }

                // Check optional days within time range
                val daysArray = config.optJSONArray("days")
                val dayMatch = if (daysArray != null && daysArray.length() > 0) {
                    val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                    var found = false
                    for (i in 0 until daysArray.length()) {
                        if (daysArray.getInt(i) == currentDayOfWeek) {
                            found = true
                            break
                        }
                    }
                    found
                } else {
                    true
                }

                val matched = inRange && dayMatch
                val msg = "Time window: %02d:%02d–%02d:%02d (Now: %02d:%02d)".format(
                    startH, startM, endH, endM,
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)
                )
                return Pair(matched, msg)
            }

            ConditionType.TIME_SPECIFIC -> {
                val targetH = config.optInt("hour", 8)
                val targetM = config.optInt("minute", 0)
                val now = triggerContext.simulatedCalendar ?: Calendar.getInstance()
                val isMatch = (now.get(Calendar.HOUR_OF_DAY) == targetH &&
                        Math.abs(now.get(Calendar.MINUTE) - targetM) <= 1)
                val msg = "Specific time: %02d:%02d".format(targetH, targetM)
                return Pair(isMatch, msg)
            }

            ConditionType.DAYS_OF_WEEK -> {
                val daysArray = config.optJSONArray("days")
                val now = triggerContext.simulatedCalendar ?: Calendar.getInstance()
                val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat

                val matched = if (daysArray != null && daysArray.length() > 0) {
                    var found = false
                    for (i in 0 until daysArray.length()) {
                        if (daysArray.getInt(i) == currentDayOfWeek) {
                            found = true
                            break
                        }
                    }
                    found
                } else {
                    true
                }
                val dayNames = arrayOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val curName = if (currentDayOfWeek in 1..7) dayNames[currentDayOfWeek] else "?"
                return Pair(matched, "Day of week ($curName)")
            }

            ConditionType.BATTERY_LEVEL_BELOW -> {
                val threshold = config.optInt("threshold", 20)
                val currentLevel = (triggerContext.extraData["battery_level"] as? Number)?.toInt()
                    ?: getBatteryPercentage(context)
                val matched = currentLevel <= threshold
                return Pair(matched, "Battery \u2264 $threshold% (Current: $currentLevel%)")
            }

            ConditionType.BATTERY_LEVEL_ABOVE -> {
                val threshold = config.optInt("threshold", 80)
                val currentLevel = (triggerContext.extraData["battery_level"] as? Number)?.toInt()
                    ?: getBatteryPercentage(context)
                val matched = currentLevel >= threshold
                return Pair(matched, "Battery \u2265 $threshold% (Current: $currentLevel%)")
            }

            ConditionType.CHARGING_STARTED -> {
                val isCharging = (triggerContext.extraData["charging"] as? Boolean)
                    ?: isDeviceCharging(context)
                val matched = if (triggerContext.triggerType == ConditionType.CHARGING_STARTED) {
                    true
                } else {
                    isCharging
                }
                return Pair(matched, "Device Charging (State: $isCharging)")
            }

            ConditionType.CHARGING_STOPPED -> {
                val isCharging = (triggerContext.extraData["charging"] as? Boolean)
                    ?: isDeviceCharging(context)
                val matched = if (triggerContext.triggerType == ConditionType.CHARGING_STOPPED) {
                    true
                } else {
                    !isCharging
                }
                return Pair(matched, "Device Unplugged (State: ${!isCharging})")
            }

            ConditionType.WIFI_CONNECTED -> {
                val isConnected = isWifiConnected(context)
                val expectedSsid = config.optString("ssid", "").trim()

                if (expectedSsid.isEmpty() || expectedSsid.equals("Any", ignoreCase = true)) {
                    val matched = if (triggerContext.triggerType == ConditionType.WIFI_CONNECTED) true else isConnected
                    return Pair(matched, "Wi-Fi Connected (Any network)")
                }

                val currentSsid = getCurrentWifiSsid(context, triggerContext)
                val matched = isConnected && currentSsid.equals(expectedSsid, ignoreCase = true)
                return Pair(matched, "Wi-Fi Connected to \"$expectedSsid\" (Current: \"$currentSsid\")")
            }

            ConditionType.WIFI_DISCONNECTED -> {
                val isConnected = isWifiConnected(context)
                val targetSsid = config.optString("ssid", "").trim()

                if (targetSsid.isEmpty() || targetSsid.equals("Any", ignoreCase = true)) {
                    val matched = if (triggerContext.triggerType == ConditionType.WIFI_DISCONNECTED) true else !isConnected
                    return Pair(matched, "Wi-Fi Disconnected")
                }

                // If leaving specific Wi-Fi network (e.g. Leaving Home)
                val disconnectedFrom = triggerContext.extraData["disconnected_ssid"] as? String
                val matched = if (disconnectedFrom != null) {
                    disconnectedFrom.equals(targetSsid, ignoreCase = true)
                } else {
                    // Not connected to this specific SSID
                    val currentSsid = getCurrentWifiSsid(context, triggerContext)
                    !isConnected || !currentSsid.equals(targetSsid, ignoreCase = true)
                }
                return Pair(matched, "Disconnected from Wi-Fi \"$targetSsid\"")
            }

            ConditionType.BLUETOOTH_CONNECTED -> {
                val expectedDevice = config.optString("deviceName", "").trim()
                if (expectedDevice.isEmpty() || expectedDevice.equals("Any", ignoreCase = true)) {
                    val matched = triggerContext.triggerType == ConditionType.BLUETOOTH_CONNECTED ||
                            triggerContext.extraData["bluetooth_connected"] == true
                    return Pair(matched, "Bluetooth Connected (Any device)")
                }

                val connectedDevice = triggerContext.extraData["device_name"] as? String ?: ""
                val matched = connectedDevice.contains(expectedDevice, ignoreCase = true)
                return Pair(matched, "Bluetooth Connected: \"$expectedDevice\"")
            }

            ConditionType.BLUETOOTH_DISCONNECTED -> {
                val expectedDevice = config.optString("deviceName", "").trim()
                if (expectedDevice.isEmpty() || expectedDevice.equals("Any", ignoreCase = true)) {
                    val matched = triggerContext.triggerType == ConditionType.BLUETOOTH_DISCONNECTED ||
                            triggerContext.extraData["bluetooth_connected"] == false
                    return Pair(matched, "Bluetooth Disconnected")
                }
                val disconnectedDevice = triggerContext.extraData["device_name"] as? String ?: ""
                val matched = disconnectedDevice.contains(expectedDevice, ignoreCase = true) ||
                        triggerContext.triggerType == ConditionType.BLUETOOTH_DISCONNECTED
                return Pair(matched, "Disconnected from Bluetooth: \"$expectedDevice\"")
            }

            ConditionType.SCREEN_ON -> {
                val matched = triggerContext.triggerType == ConditionType.SCREEN_ON ||
                        triggerContext.extraData["screen_on"] == true
                return Pair(matched, "Screen Turned On")
            }

            ConditionType.SCREEN_OFF -> {
                val matched = triggerContext.triggerType == ConditionType.SCREEN_OFF ||
                        triggerContext.extraData["screen_on"] == false
                return Pair(matched, "Screen Turned Off")
            }

            ConditionType.HEADSET_CONNECTED -> {
                val matched = triggerContext.triggerType == ConditionType.HEADSET_CONNECTED ||
                        triggerContext.extraData["headset_connected"] == true
                return Pair(matched, "Headphones Connected")
            }

            ConditionType.HEADSET_DISCONNECTED -> {
                val matched = triggerContext.triggerType == ConditionType.HEADSET_DISCONNECTED ||
                        triggerContext.extraData["headset_connected"] == false
                return Pair(matched, "Headphones Disconnected")
            }

            ConditionType.DEVICE_BOOT -> {
                val matched = triggerContext.triggerType == ConditionType.DEVICE_BOOT
                return Pair(matched, "Device Restart / Boot Event")
            }

            ConditionType.APP_OPENED -> {
                val expectedPackage = config.optString("packageName", "").trim()
                val activePackage = triggerContext.extraData["packageName"] as? String ?: ""
                val matched = if (expectedPackage.isEmpty()) {
                    activePackage.isNotEmpty()
                } else {
                    expectedPackage.equals(activePackage, ignoreCase = true)
                }
                return Pair(matched, "App opened: ${expectedPackage.ifEmpty { "Any" }}")
            }

            ConditionType.NOTIFICATION_RECEIVED -> {
                val targetPackage = config.optString("packageName", "").trim()
                val fieldStr = config.optString("field", "ANY")
                val operatorStr = config.optString("operator", "CONTAINS")
                val query = config.optString("query", config.optString("keyword", "")).trim()

                val field = try {
                    NotificationField.valueOf(fieldStr)
                } catch (_: Exception) {
                    NotificationField.ANY
                }

                val operator = try {
                    NotificationMatchOperator.valueOf(operatorStr)
                } catch (_: Exception) {
                    NotificationMatchOperator.CONTAINS
                }

                // If trigger comes from FlowNotificationListenerService
                val intercepted = triggerContext.extraData["notification"] as? InterceptedNotification

                if (intercepted != null) {
                    val matched = NotificationEventHub.evaluateMatch(
                        notification = intercepted,
                        targetPackage = targetPackage,
                        field = field,
                        operator = operator,
                        query = query
                    )
                    val desc = "Notification [${targetPackage.ifEmpty { "Any App" }} | ${field.label} ${operator.label} \"$query\"]"
                    return Pair(matched, desc)
                }

                // Fallback for string-based trigger context
                val receivedPackage = triggerContext.extraData["packageName"] as? String ?: ""
                val receivedText = triggerContext.extraData["notification_text"] as? String
                    ?: triggerContext.extraData["text"] as? String ?: ""

                val packageMatches = targetPackage.isEmpty() || targetPackage.equals(receivedPackage, ignoreCase = true)
                val textMatches = query.isEmpty() || NotificationEventHub.matchesOperator(receivedText, operator, query)

                val matched = packageMatches && textMatches
                return Pair(matched, "Notification [${targetPackage.ifEmpty { "Any App" }} ${operator.label} \"$query\"]")
            }

            ConditionType.LOCATION_GEOFENCE -> {
                val event = config.optString("event", "ARRIVING") // ARRIVING or LEAVING
                val placeName = config.optString("placeName", "Home")
                val triggerEvent = triggerContext.extraData["geofence_event"] as? String ?: event
                val triggerPlace = triggerContext.extraData["placeName"] as? String ?: placeName

                val matched = triggerEvent.equals(event, ignoreCase = true) &&
                        triggerPlace.equals(placeName, ignoreCase = true)
                return Pair(matched, "Geofence $event \"$placeName\"")
            }

            ConditionType.CALENDAR_EVENT -> {
                val keyword = config.optString("keyword", "").trim()
                val requireBusy = config.optBoolean("requireBusy", true)
                val (hasMatch, eventDetails) = evaluateCalendarEvent(context, keyword, requireBusy)
                return Pair(hasMatch, "Calendar Event: $eventDetails")
            }

            ConditionType.NFC_TAG_SCANNED -> {
                val expectedTag = config.optString("tagId", "").trim()
                val actualTag = (triggerContext.extraData["tagId"] as? String)?.trim() ?: ""
                val payload = (triggerContext.extraData["payload"] as? String)?.trim() ?: ""

                val isNfcTrigger = triggerContext.triggerType == ConditionType.NFC_TAG_SCANNED
                val tagMatches = expectedTag.isEmpty() ||
                        expectedTag.equals(actualTag, ignoreCase = true) ||
                        payload.contains(expectedTag, ignoreCase = true)

                val matched = isNfcTrigger && tagMatches
                return Pair(matched, if (matched) "NFC Tag Matched ($actualTag)" else "Awaiting NFC Tag Tap")
            }

            ConditionType.FLIP_TO_SHHH -> {
                val isFlip = triggerContext.triggerType == ConditionType.FLIP_TO_SHHH
                return Pair(isFlip, if (isFlip) "Phone Face-Down (Shhh Active)" else "Phone Face-Up / Normal")
            }

            ConditionType.SHAKE_GESTURE -> {
                val isShake = triggerContext.triggerType == ConditionType.SHAKE_GESTURE
                return Pair(isShake, if (isShake) "Shake Gesture Detected" else "No Shake Gesture")
            }
        }
    }

    private fun evaluateCalendarEvent(context: Context, keyword: String, requireBusy: Boolean): Pair<Boolean, String> {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return Pair(false, "Calendar permission not granted")
        }

        return try {
            val now = System.currentTimeMillis()
            val builder = android.provider.CalendarContract.Instances.CONTENT_URI.buildUpon()
            android.content.ContentUris.appendId(builder, now - 60_000)
            android.content.ContentUris.appendId(builder, now + 60_000)

            val projection = arrayOf(
                android.provider.CalendarContract.Instances.TITLE,
                android.provider.CalendarContract.Instances.AVAILABILITY
            )

            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                null
            )

            cursor?.use {
                val titleCol = it.getColumnIndex(android.provider.CalendarContract.Instances.TITLE)
                val availCol = it.getColumnIndex(android.provider.CalendarContract.Instances.AVAILABILITY)
                while (it.moveToNext()) {
                    val title = if (titleCol != -1) it.getString(titleCol) ?: "" else ""
                    val availability = if (availCol != -1) it.getInt(availCol) else android.provider.CalendarContract.Instances.AVAILABILITY_BUSY

                    val isBusy = availability == android.provider.CalendarContract.Instances.AVAILABILITY_BUSY
                    val keywordMatches = keyword.isEmpty() || title.contains(keyword, ignoreCase = true)

                    if (keywordMatches && (!requireBusy || isBusy)) {
                        val statusDesc = if (isBusy) "Busy" else "Free"
                        return Pair(true, "\"$title\" ($statusDesc)")
                    }
                }
            }
            Pair(false, if (keyword.isNotEmpty()) "No active event matching \"$keyword\"" else "No active calendar events")
        } catch (e: Exception) {
            Pair(false, "Calendar check error: ${e.localizedMessage}")
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            50
        }
    }

    private fun isDeviceCharging(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentWifiSsid(context: Context, triggerContext: TriggerContext): String {
        (triggerContext.extraData["wifi_ssid"] as? String)?.let { return it }
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connectionInfo: WifiInfo? = wm?.connectionInfo
            val ssid = connectionInfo?.ssid?.replace("\"", "") ?: ""
            if (ssid == "<unknown ssid>") "" else ssid
        } catch (_: Exception) {
            ""
        }
    }
}

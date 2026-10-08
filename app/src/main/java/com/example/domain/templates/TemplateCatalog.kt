package com.example.domain.templates

import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.Routine
import com.example.domain.models.RoutinePriority
import java.util.UUID

data class RoutineTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val conditionLogic: ConditionLogic = ConditionLogic.ALL,
    val priority: RoutinePriority = RoutinePriority.NORMAL,
    val conditions: List<AutomationCondition>,
    val actions: List<AutomationAction>,
    val tags: List<String> = emptyList()
) {
    fun toRoutine(): Routine {
        val newRoutineId = UUID.randomUUID().toString()
        return Routine(
            id = newRoutineId,
            name = name,
            description = description,
            iconName = iconName,
            colorHex = colorHex,
            isEnabled = true,
            isFavorite = false,
            conditionLogic = conditionLogic,
            priority = priority,
            cooldownSeconds = 30,
            conditions = conditions.map {
                it.copy(
                    id = UUID.randomUUID().toString(),
                    routineId = newRoutineId
                )
            },
            actions = actions.map {
                it.copy(
                    id = UUID.randomUUID().toString(),
                    routineId = newRoutineId
                )
            },
            createdAt = System.currentTimeMillis()
        )
    }
}

object TemplateCatalog {

    val allTemplates: List<RoutineTemplate> = listOf(
        // 1. Sleep Mode (Test Case 1)
        RoutineTemplate(
            id = "template_sleep_mode",
            name = "Sleep Mode",
            category = "Lifestyle",
            description = "DND ON, mute audio, and dim screen when charging overnight (22:00-07:00). Reverts on exit.",
            iconName = "Bedtime",
            colorHex = "#818CF8",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.CRITICAL,
            tags = listOf("Bedtime", "DND", "Night", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_sleep_1",
                    routineId = "",
                    type = ConditionType.TIME_RANGE,
                    title = "Time between 22:00 and 07:00",
                    summary = "22:00 – 07:00",
                    configJson = "{\"startHour\":22,\"startMinute\":0,\"endHour\":7,\"endMinute\":0}"
                ),
                AutomationCondition(
                    id = "c_sleep_2",
                    routineId = "",
                    type = ConditionType.CHARGING_STARTED,
                    title = "Charger connected",
                    summary = "Phone connected to charger",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_sleep_1",
                    routineId = "",
                    type = ActionType.SET_RINGER_MODE,
                    title = "Mute Ringer",
                    summary = "Ringer Silent",
                    configJson = "{\"mode\":\"SILENT\"}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_sleep_2",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Mute Media",
                    summary = "Media Volume 0%",
                    configJson = "{\"volume\":0}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_sleep_3",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Dim Screen",
                    summary = "Brightness 10%",
                    configJson = "{\"brightness\":10}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_sleep_4",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Sleep Mode Active",
                    summary = "Night routine initiated",
                    configJson = "{\"title\":\"Sleep Mode Active\",\"message\":\"Do Not Disturb enabled. Good night!\"}"
                )
            )
        ),

        // 2. Leaving Home (Test Case 2)
        RoutineTemplate(
            id = "template_leaving_home",
            name = "Leaving Home",
            category = "Location",
            description = "Boost brightness and prepare outdoor settings when disconnected from home Wi-Fi.",
            iconName = "DirectionsWalk",
            colorHex = "#F59E0B",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            tags = listOf("Outdoor", "Wi-Fi", "Brightness", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_leave_1",
                    routineId = "",
                    type = ConditionType.WIFI_DISCONNECTED,
                    title = "Wi-Fi Disconnected",
                    summary = "Disconnected from home network",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_leave_1",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Outdoor Brightness",
                    summary = "Brightness 80%",
                    configJson = "{\"brightness\":80}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_leave_2",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Haptic Notice",
                    summary = "Subtle pulse indicating departure",
                    configJson = "{}"
                ),
                AutomationAction(
                    id = "a_leave_3",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Leaving Home Routine",
                    summary = "Outdoor profile active",
                    configJson = "{\"title\":\"Leaving Home\",\"message\":\"Outdoor screen brightness applied.\"}"
                )
            )
        ),

        // 3. Driving Mode (Test Case 3)
        RoutineTemplate(
            id = "template_driving_mode",
            name = "Driving Mode",
            category = "Travel",
            description = "Turn up media volume and launch navigation when connected to car Bluetooth.",
            iconName = "DirectionsCar",
            colorHex = "#38BDF8",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.CRITICAL,
            tags = listOf("Car", "Bluetooth", "Navigation", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_drive_1",
                    routineId = "",
                    type = ConditionType.BLUETOOTH_CONNECTED,
                    title = "Connected to Car Bluetooth",
                    summary = "Car audio system connected",
                    configJson = "{\"deviceName\":\"Car\"}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_drive_1",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Media Volume 90%",
                    summary = "High volume for car speakers",
                    configJson = "{\"volume\":90}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_drive_2",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Driving Mode Started",
                    summary = "Drive safely alert",
                    configJson = "{\"title\":\"Driving Mode\",\"message\":\"Hands-free audio active. Keep eyes on the road!\"}"
                )
            )
        ),

        // 4. Battery Saver (Test Case 4)
        RoutineTemplate(
            id = "template_battery_saver",
            name = "Battery Saver",
            category = "Power",
            description = "Dim display and alert user when battery drops below 20% while unplugged.",
            iconName = "BatteryAlert",
            colorHex = "#F43F5E",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.CRITICAL,
            tags = listOf("Battery", "Power Saving", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_bat_1",
                    routineId = "",
                    type = ConditionType.BATTERY_LEVEL_BELOW,
                    title = "Battery below 20%",
                    summary = "Battery <= 20%",
                    configJson = "{\"threshold\":20}"
                ),
                AutomationCondition(
                    id = "c_bat_2",
                    routineId = "",
                    type = ConditionType.CHARGING_STARTED,
                    title = "NOT Charging",
                    summary = "Device not plugged into charger",
                    configJson = "{}",
                    isNegated = true
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_bat_1",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Conserve Display Power",
                    summary = "Brightness 20%",
                    configJson = "{\"brightness\":20}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_bat_2",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Low Battery Pulse",
                    summary = "Haptic feedback alert",
                    configJson = "{}"
                ),
                AutomationAction(
                    id = "a_bat_3",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Battery Saver Engaged",
                    summary = "FlowModes power preservation",
                    configJson = "{\"title\":\"Battery Saver Active\",\"message\":\"Battery below 20%. Display dimmed to preserve runtime.\"}"
                )
            )
        ),

        // 5. Work Focus (Test Case 5)
        RoutineTemplate(
            id = "template_work_focus",
            name = "Work Focus",
            category = "Work",
            description = "Set phone to vibrate and mute loud media during office hours (Mon-Fri 09:00-17:00).",
            iconName = "Work",
            colorHex = "#A78BFA",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            tags = listOf("Office", "Work", "Vibrate", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_work_1",
                    routineId = "",
                    type = ConditionType.DAYS_OF_WEEK,
                    title = "Monday to Friday",
                    summary = "Weekdays only",
                    configJson = "{\"days\":[2,3,4,5,6]}"
                ),
                AutomationCondition(
                    id = "c_work_2",
                    routineId = "",
                    type = ConditionType.TIME_RANGE,
                    title = "09:00 to 17:00",
                    summary = "Work hours",
                    configJson = "{\"startHour\":9,\"startMinute\":0,\"endHour\":17,\"endMinute\":0}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_work_1",
                    routineId = "",
                    type = ActionType.SET_RINGER_MODE,
                    title = "Switch to Vibrate",
                    summary = "Ringer Vibrate",
                    configJson = "{\"mode\":\"VIBRATE\"}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_work_2",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Media Volume 15%",
                    summary = "Quiet office volume",
                    configJson = "{\"volume\":15}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_work_3",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Work Focus Active",
                    summary = "Distraction-free profile enabled",
                    configJson = "{\"title\":\"Work Focus\",\"message\":\"Vibrate mode enabled for work hours.\"}"
                )
            )
        ),

        // 6. Urgent Notification Alert (Test Case 6)
        RoutineTemplate(
            id = "template_urgent_alert",
            name = "Urgent Notification Alert",
            category = "Alerts",
            description = "Flash torch, vibrate intensely, and boost volume when an emergency notification arrives.",
            iconName = "NotificationsActive",
            colorHex = "#F43F5E",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.CRITICAL,
            tags = listOf("Emergency", "Notification", "Torch", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_urgent_1",
                    routineId = "",
                    type = ConditionType.NOTIFICATION_RECEIVED,
                    title = "Notification with 'Emergency' or 'Urgent'",
                    summary = "Contains alert keyword",
                    configJson = "{\"keyword\":\"Emergency|Urgent\",\"operator\":\"REGEX\",\"field\":\"ANY\",\"caseSensitive\":false}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_urgent_1",
                    routineId = "",
                    type = ActionType.TOGGLE_FLASHLIGHT,
                    title = "Flash Torch",
                    summary = "Turn Flashlight ON",
                    configJson = "{\"enable\":true}"
                ),
                AutomationAction(
                    id = "a_urgent_2",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Emergency Vibration Pulse",
                    summary = "Intense haptic alert",
                    configJson = "{}"
                ),
                AutomationAction(
                    id = "a_urgent_3",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Max Media Volume",
                    summary = "Volume 100%",
                    configJson = "{\"volume\":100}"
                ),
                AutomationAction(
                    id = "a_urgent_4",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Urgent Alert Received",
                    summary = "High priority dispatch",
                    configJson = "{\"title\":\"URGENT DISPATCH\",\"message\":\"An urgent notification was intercepted!\"}"
                )
            )
        ),

        // 7. Headphones Plugged In (Test Case 7)
        RoutineTemplate(
            id = "template_headphones_connected",
            name = "Headphones Music Flow",
            category = "Audio",
            description = "Comfortable listening volume and immediate playback feedback when headphones are connected.",
            iconName = "Headphones",
            colorHex = "#10B981",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("Audio", "Headphones", "Music", "Flagship"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_head_1",
                    routineId = "",
                    type = ConditionType.HEADSET_CONNECTED,
                    title = "Headphones Connected",
                    summary = "Headset plugged in or paired",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_head_1",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Set Volume to 50%",
                    summary = "Safe audio level 50%",
                    configJson = "{\"volume\":50}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_head_2",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Headphones Connected",
                    summary = "Safe listening volume applied",
                    configJson = "{\"title\":\"Headphones Connected\",\"message\":\"Media set to 50%. Enjoy your audio!\"}"
                )
            )
        ),

        // 8. Study Deep Focus
        RoutineTemplate(
            id = "template_study_focus",
            name = "Study Deep Focus",
            category = "Study",
            description = "Quiet evenings for focused reading and exam preparation (18:00 - 21:00).",
            iconName = "School",
            colorHex = "#818CF8",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            tags = listOf("Study", "Focus", "Silence"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_study_1",
                    routineId = "",
                    type = ConditionType.TIME_RANGE,
                    title = "18:00 to 21:00",
                    summary = "Evening study hours",
                    configJson = "{\"startHour\":18,\"startMinute\":0,\"endHour\":21,\"endMinute\":0}"
                ),
                AutomationCondition(
                    id = "c_study_2",
                    routineId = "",
                    type = ConditionType.DAYS_OF_WEEK,
                    title = "Monday to Thursday",
                    summary = "School days",
                    configJson = "{\"days\":[2,3,4,5]}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_study_1",
                    routineId = "",
                    type = ActionType.SET_RINGER_MODE,
                    title = "Silent Ringer",
                    summary = "Silent mode",
                    configJson = "{\"mode\":\"SILENT\"}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_study_2",
                    routineId = "",
                    type = ActionType.SET_DND_MODE,
                    title = "Priority DND",
                    summary = "Block distractions",
                    configJson = "{\"mode\":\"PRIORITY\"}",
                    restoreOnExit = true
                )
            )
        ),

        // 9. Late Night Reading
        RoutineTemplate(
            id = "template_night_reading",
            name = "Late Night Reading",
            category = "Lifestyle",
            description = "Soft warm lighting and low volume for comfortable late night reading.",
            iconName = "MenuBook",
            colorHex = "#F59E0B",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("Reading", "Night", "Dim"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_read_1",
                    routineId = "",
                    type = ConditionType.TIME_RANGE,
                    title = "23:00 to 02:00",
                    summary = "Late night reading",
                    configJson = "{\"startHour\":23,\"startMinute\":0,\"endHour\":2,\"endMinute\":0}"
                ),
                AutomationCondition(
                    id = "c_read_2",
                    routineId = "",
                    type = ConditionType.SCREEN_ON,
                    title = "Screen Turned On",
                    summary = "Device in active use",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_read_1",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Dim to 15%",
                    summary = "Eye comfort brightness",
                    configJson = "{\"brightness\":15}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_read_2",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Soft Audio 25%",
                    summary = "Gentle ambient volume",
                    configJson = "{\"volume\":25}",
                    restoreOnExit = true
                )
            )
        ),

        // 10. Morning Sunrise
        RoutineTemplate(
            id = "template_morning_sunrise",
            name = "Morning Sunrise",
            category = "Daily",
            description = "Energizing brightness and morning summary alert right at wake-up time.",
            iconName = "WbSunny",
            colorHex = "#F59E0B",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("Morning", "Alarm", "Daily"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_morn_1",
                    routineId = "",
                    type = ConditionType.TIME_SPECIFIC,
                    title = "07:00 AM",
                    summary = "Wakeup schedule",
                    configJson = "{\"hour\":7,\"minute\":0}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_morn_1",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Brighten to 65%",
                    summary = "Daylight readiness",
                    configJson = "{\"brightness\":65}"
                ),
                AutomationAction(
                    id = "a_morn_2",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Media Volume 50%",
                    summary = "Normal volume",
                    configJson = "{\"volume\":50}"
                ),
                AutomationAction(
                    id = "a_morn_3",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Good Morning!",
                    summary = "FlowModes Morning Routine",
                    configJson = "{\"title\":\"Good Morning!\",\"message\":\"Have a productive and focused day ahead.\"}"
                )
            )
        ),

        // 11. Workout Energy Boost
        RoutineTemplate(
            id = "template_workout",
            name = "Workout Energy Boost",
            category = "Fitness",
            description = "Turn up the audio and get moving when workout Bluetooth earphones connect.",
            iconName = "FitnessCenter",
            colorHex = "#10B981",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("Fitness", "Gym", "Audio"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_worko_1",
                    routineId = "",
                    type = ConditionType.BLUETOOTH_CONNECTED,
                    title = "Connected to Bluetooth Audio",
                    summary = "Sports earphones connected",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_worko_1",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "High Energy Volume 85%",
                    summary = "Pump up volume",
                    configJson = "{\"volume\":85}"
                ),
                AutomationAction(
                    id = "a_worko_2",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Workout Motivation Pulse",
                    summary = "Rhythmic haptic tap",
                    configJson = "{}"
                ),
                AutomationAction(
                    id = "a_worko_3",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Workout Started",
                    summary = "FlowModes Fitness",
                    configJson = "{\"title\":\"Workout Mode Active\",\"message\":\"Volume boosted for maximum performance!\"}"
                )
            )
        ),

        // 12. Cinema & Theater Mode
        RoutineTemplate(
            id = "template_cinema",
            name = "Cinema Dark",
            category = "Entertainment",
            description = "Dim display, mute sounds, and silence ringtones while watching films.",
            iconName = "Movie",
            colorHex = "#A78BFA",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            tags = listOf("Movie", "Quiet", "Dim"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_cine_1",
                    routineId = "",
                    type = ConditionType.APP_OPENED,
                    title = "Streaming App Opened",
                    summary = "Video app active",
                    configJson = "{\"packageName\":\"com.google.android.youtube\"}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_cine_1",
                    routineId = "",
                    type = ActionType.SET_RINGER_MODE,
                    title = "Mute Ringtone",
                    summary = "Silent mode",
                    configJson = "{\"mode\":\"SILENT\"}",
                    restoreOnExit = true
                ),
                AutomationAction(
                    id = "a_cine_2",
                    routineId = "",
                    type = ActionType.SET_BRIGHTNESS,
                    title = "Lower Brightness 20%",
                    summary = "Theater dark brightness",
                    configJson = "{\"brightness\":20}",
                    restoreOnExit = true
                )
            )
        ),

        // 13. Public Wi-Fi Security Warning
        RoutineTemplate(
            id = "template_wifi_security",
            name = "Public Wi-Fi Alert",
            category = "Security",
            description = "Trigger vibration warning and security notice when connecting to public networks.",
            iconName = "Security",
            colorHex = "#F43F5E",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("Security", "Wi-Fi", "Alert"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_sec_1",
                    routineId = "",
                    type = ConditionType.WIFI_CONNECTED,
                    title = "Connected to Wi-Fi",
                    summary = "Public network active",
                    configJson = "{\"ssid\":\"Guest|Public\"}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_sec_1",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Security Alert Haptic",
                    summary = "Double pulse",
                    configJson = "{}"
                ),
                AutomationAction(
                    id = "a_sec_2",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "Public Network Connected",
                    summary = "FlowModes Security Guard",
                    configJson = "{\"title\":\"Public Wi-Fi Warning\",\"message\":\"Connected to an untrusted Wi-Fi. Ensure VPN is active.\"}"
                )
            )
        ),

        // 14. Device Reboot Status Check
        RoutineTemplate(
            id = "template_boot_check",
            name = "Reboot Health Check",
            category = "System",
            description = "Confirm FlowModes background engine is armed immediately after phone restarts.",
            iconName = "RestartAlt",
            colorHex = "#38BDF8",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.NORMAL,
            tags = listOf("System", "Reboot", "Health"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_boot_1",
                    routineId = "",
                    type = ConditionType.DEVICE_BOOT,
                    title = "Device Restart Completed",
                    summary = "Phone restarted",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_boot_1",
                    routineId = "",
                    type = ActionType.SHOW_NOTIFICATION,
                    title = "FlowModes Active",
                    summary = "Engine initialized",
                    configJson = "{\"title\":\"FlowModes Ready\",\"message\":\"Automation engine armed and ready after reboot.\"}"
                )
            )
        ),

        // 15. Screen Off Standby Protection
        RoutineTemplate(
            id = "template_screen_off_saver",
            name = "Screen-Off Mute",
            category = "Power",
            description = "Prevent rogue loud video audio from playing when screen turns off.",
            iconName = "PhonelinkErase",
            colorHex = "#10B981",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.LOW,
            tags = listOf("Standby", "Screen", "Audio"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_scroff_1",
                    routineId = "",
                    type = ConditionType.SCREEN_OFF,
                    title = "Screen Turned Off",
                    summary = "Device display sleeping",
                    configJson = "{}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_scroff_1",
                    routineId = "",
                    type = ActionType.SET_MEDIA_VOLUME,
                    title = "Mute Rogue Audio",
                    summary = "Media 0%",
                    configJson = "{\"volume\":0}",
                    restoreOnExit = true
                )
            )
        ),

        // 16. VIP Message Flash Alert
        RoutineTemplate(
            id = "template_vip_message",
            name = "VIP Sender Flash Alert",
            category = "Alerts",
            description = "Turn on flashlight when a message arrives from a VIP contact.",
            iconName = "Star",
            colorHex = "#F59E0B",
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            tags = listOf("VIP", "Notification", "Contacts"),
            conditions = listOf(
                AutomationCondition(
                    id = "c_vip_1",
                    routineId = "",
                    type = ConditionType.NOTIFICATION_RECEIVED,
                    title = "Message containing 'VIP' or 'Important'",
                    summary = "Key notification match",
                    configJson = "{\"keyword\":\"VIP|Important\",\"operator\":\"REGEX\",\"field\":\"ANY\",\"caseSensitive\":false}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = "a_vip_1",
                    routineId = "",
                    type = ActionType.TOGGLE_FLASHLIGHT,
                    title = "Flashlight Alert",
                    summary = "Turn Flashlight ON",
                    configJson = "{\"enable\":true}"
                ),
                AutomationAction(
                    id = "a_vip_2",
                    routineId = "",
                    type = ActionType.TRIGGER_VIBRATION,
                    title = "Distinct Vibration",
                    summary = "Triple pulse alert",
                    configJson = "{}"
                )
            )
        )
    )
}

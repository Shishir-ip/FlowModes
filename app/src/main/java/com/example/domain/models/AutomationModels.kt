package com.example.domain.models

enum class ConditionLogic {
    ALL, // AND
    ANY  // OR
}

enum class RoutinePriority(val level: Int, val label: String) {
    LOW(1, "Low"),
    NORMAL(2, "Normal"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");

    companion object {
        fun fromLevel(level: Int): RoutinePriority =
            entries.firstOrNull { it.level == level } ?: NORMAL
    }
}

enum class ConditionType(val category: String, val displayName: String, val iconName: String) {
    TIME_SPECIFIC("Time", "Specific Time", "Schedule"),
    TIME_RANGE("Time", "Time Window", "AccessTime"),
    DAYS_OF_WEEK("Time", "Days of the Week", "DateRange"),
    BATTERY_LEVEL_BELOW("Battery", "Battery Level Below", "BatteryAlert"),
    BATTERY_LEVEL_ABOVE("Battery", "Battery Level Above", "BatteryChargingFull"),
    CHARGING_STARTED("Battery", "Charging Started", "Power"),
    CHARGING_STOPPED("Battery", "Charging Stopped", "PowerOff"),
    WIFI_CONNECTED("Connectivity", "Wi-Fi Connected", "Wifi"),
    WIFI_DISCONNECTED("Connectivity", "Wi-Fi Disconnected", "WifiOff"),
    BLUETOOTH_CONNECTED("Connectivity", "Bluetooth Connected", "Bluetooth"),
    BLUETOOTH_DISCONNECTED("Connectivity", "Bluetooth Disconnected", "BluetoothDisabled"),
    SCREEN_ON("Device State", "Screen Turned On", "Smartphone"),
    SCREEN_OFF("Device State", "Screen Turned Off", "PhonelinkErase"),
    HEADSET_CONNECTED("Device State", "Headphones Connected", "Headphones"),
    HEADSET_DISCONNECTED("Device State", "Headphones Disconnected", "HeadsetOff"),
    DEVICE_BOOT("Device State", "Device Restart / Boot", "RestartAlt"),
    APP_OPENED("Apps", "App Opened", "Apps"),
    NOTIFICATION_RECEIVED("Notifications", "Notification Received", "NotificationsActive"),
    LOCATION_GEOFENCE("Location", "Location Geofence", "LocationOn"),
    MANUAL_TRIGGER("Manual", "Manual Activation", "TouchApp")
}

enum class ActionType(
    val category: String,
    val displayName: String,
    val iconName: String,
    val requiresSpecialAccess: Boolean = false,
    val requiredAccessName: String? = null
) {
    SET_MEDIA_VOLUME("Audio", "Set Media Volume", "VolumeUp"),
    SET_RINGER_MODE("Audio", "Set Ringer Mode", "Notifications"),
    SET_BRIGHTNESS("Display", "Set Brightness", "BrightnessMedium", requiresSpecialAccess = true, requiredAccessName = "Write Settings / Guidance"),
    SHOW_NOTIFICATION("Alerts", "Show Notification", "NotificationImportant"),
    LAUNCH_APP("Apps", "Open Application", "Launch"),
    OPEN_URL("Web", "Open Web Link", "Language"),
    TOGGLE_FLASHLIGHT("Utilities", "Toggle Flashlight", "FlashlightOn"),
    TRIGGER_VIBRATION("Haptics", "Haptic Vibration", "Vibration"),
    OPEN_SETTINGS("Quick Settings", "Open System Settings", "Settings"),
    SET_DND_MODE("System", "Do Not Disturb Mode", "DoNotDisturb", requiresSpecialAccess = true, requiredAccessName = "DND Policy Access"),
    ACCESSIBILITY_NAV("Accessibility", "Accessibility Navigation", "Accessibility", requiresSpecialAccess = true, requiredAccessName = "Accessibility Access")
}

data class AutomationCondition(
    val id: String,
    val routineId: String,
    val type: ConditionType,
    val title: String,
    val summary: String,
    val configJson: String = "{}",
    val isNegated: Boolean = false
)

data class AutomationAction(
    val id: String,
    val routineId: String,
    val type: ActionType,
    val title: String,
    val summary: String,
    val configJson: String = "{}",
    val restoreOnExit: Boolean = false
)

enum class NotificationMatchOperator(val label: String) {
    CONTAINS("Contains"),
    EQUALS("Equals"),
    STARTS_WITH("Starts with"),
    ENDS_WITH("Ends with"),
    DOES_NOT_CONTAIN("Does not contain"),
    REGEX("Regular Expression")
}

enum class NotificationField(val label: String) {
    ANY("Any Field (Title or Content)"),
    TITLE("Notification Title"),
    TEXT("Notification Message Text"),
    SUBTEXT("Subtext / Conversation Name")
}

data class InterceptedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Routine(
    val id: String,
    val name: String,
    val description: String = "",
    val iconName: String = "AutoAwesome",
    val colorHex: String = "#38BDF8", // Flow Glass Blue
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val conditionLogic: ConditionLogic = ConditionLogic.ALL,
    val priority: RoutinePriority = RoutinePriority.NORMAL,
    val cooldownSeconds: Long = 30, // Loop prevention cooldown
    val conditions: List<AutomationCondition> = emptyList(),
    val actions: List<AutomationAction> = emptyList(),
    val lastTriggeredTime: Long? = null,
    val triggerCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Mode(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val isActive: Boolean = false,
    val actions: List<AutomationAction> = emptyList(),
    val activatedAt: Long? = null
)

data class ExecutionLog(
    val id: Long = 0,
    val routineId: String,
    val routineName: String,
    val triggerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean,
    val conditionsSummary: String,
    val actionsSummary: String,
    val resultSummary: String,
    val errorMessage: String? = null
)

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(
        val reason: String,
        val canOpenSettings: Boolean = false,
        val settingsIntentAction: String? = null
    ) : ActionResult()
    data class Skipped(val reason: String) : ActionResult()
}

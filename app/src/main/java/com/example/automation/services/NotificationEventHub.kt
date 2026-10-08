package com.example.automation.services

import com.example.domain.models.InterceptedNotification
import com.example.domain.models.NotificationField
import com.example.domain.models.NotificationMatchOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object NotificationEventHub {
    private const val MAX_HISTORY = 50

    private val _notifications = MutableStateFlow<List<InterceptedNotification>>(emptyList())
    val recentNotifications: StateFlow<List<InterceptedNotification>> = _notifications.asStateFlow()

    fun recordNotification(notification: InterceptedNotification) {
        val current = _notifications.value.toMutableList()
        current.add(0, notification)
        if (current.size > MAX_HISTORY) {
            _notifications.value = current.take(MAX_HISTORY)
        } else {
            _notifications.value = current
        }
    }

    fun clearHistory() {
        _notifications.value = emptyList()
    }

    fun simulateNotification(
        packageName: String = "com.whatsapp",
        appName: String = "WhatsApp",
        title: String = "John Doe",
        text: String = "Urgent: Please call me back right now!",
        subText: String = "Family Group"
    ): InterceptedNotification {
        val notification = InterceptedNotification(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            timestamp = System.currentTimeMillis()
        )
        recordNotification(notification)
        return notification
    }

    fun evaluateMatch(
        notification: InterceptedNotification,
        targetPackage: String,
        field: NotificationField,
        operator: NotificationMatchOperator,
        query: String
    ): Boolean {
        // 1. Package check (if targetPackage specified and not empty)
        if (targetPackage.isNotBlank() && !notification.packageName.equals(targetPackage.trim(), ignoreCase = true)) {
            return false
        }

        // If query is empty, any notification from that package matches
        if (query.isBlank()) {
            return true
        }

        val sourceText = when (field) {
            NotificationField.ANY -> "${notification.title} ${notification.text} ${notification.subText}".trim()
            NotificationField.TITLE -> notification.title
            NotificationField.TEXT -> notification.text
            NotificationField.SUBTEXT -> notification.subText
        }

        return matchesOperator(sourceText, operator, query.trim())
    }

    fun matchesOperator(source: String, operator: NotificationMatchOperator, target: String): Boolean {
        if (target.isEmpty()) return true
        return try {
            when (operator) {
                NotificationMatchOperator.CONTAINS -> source.contains(target, ignoreCase = true)
                NotificationMatchOperator.EQUALS -> source.equals(target, ignoreCase = true)
                NotificationMatchOperator.STARTS_WITH -> source.startsWith(target, ignoreCase = true)
                NotificationMatchOperator.ENDS_WITH -> source.endsWith(target, ignoreCase = true)
                NotificationMatchOperator.DOES_NOT_CONTAIN -> !source.contains(target, ignoreCase = true)
                NotificationMatchOperator.REGEX -> Regex(target, RegexOption.IGNORE_CASE).containsMatchIn(source)
            }
        } catch (_: Exception) {
            // If regex is malformed, fallback to contains
            source.contains(target, ignoreCase = true)
        }
    }
}

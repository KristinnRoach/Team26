package com.example.team26.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.notification.NotificationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationDao: NotificationDao) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadNotificationCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationDao.markNotificationAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllNotificationsAsRead()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationDao.clearAllNotifications()
        }
    }
}
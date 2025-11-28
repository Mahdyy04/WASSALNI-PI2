package com.example.notification.service;

import com.example.notification.dto.CreateNotificationRequest;
import com.example.notification.entities.Notification;

import java.util.List;

public interface NotificationService {
    
    Notification createNotification(CreateNotificationRequest request);
    
    List<Notification> getNotificationsByUser(String userId);
    
    List<Notification> getUnreadNotifications(String userId);
    
    long getUnreadCount(String userId);
    
    void markAsRead(String notificationId);
    
    void markAllAsRead(String userId);
}

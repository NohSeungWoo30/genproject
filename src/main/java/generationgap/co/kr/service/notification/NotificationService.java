package generationgap.co.kr.service.notification;

import generationgap.co.kr.domain.notification.Notification;
import generationgap.co.kr.dto.notification.NotificationDto;

import java.util.List;

public interface NotificationService {
    void sendNotification(NotificationDto dto);

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notiIdx, Long userId);

    void delete(Long notiIdx, Long userId);
}

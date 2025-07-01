package generationgap.co.kr.service.notification;

import generationgap.co.kr.domain.notification.Notification;
import generationgap.co.kr.dto.notification.NotificationDto;
import generationgap.co.kr.mapper.notification.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public void sendNotification(NotificationDto dto) {

        System.out.println("DTO: " + dto); // 객체 자체 로그
        if (dto == null) {
            System.out.println("dto가 null입니다!");
            return;
        }
        try {
            String rawTemplate = notificationMapper.getTemplateByType(dto.getNotiTypeIdx());

            String message = applyTemplate(rawTemplate, dto.getVariables());

            Notification notification = new Notification();
            notification.setUserIdx(dto.getRecipientId());
            notification.setNotiMessage(message);
            notification.setNotiTypeIdx(dto.getNotiTypeIdx());
            notification.setNotiUrl(dto.getNotiUrl());

            notificationMapper.insertNotification(notification);

            Map<String, String> payload = new HashMap<>();
            payload.put("notiIdx", notification.getNotiIdx().toString()); // 자동 생성된 notiIdx
            payload.put("notiMessage", message);
            payload.put("notiUrl", notification.getNotiUrl());
            messagingTemplate.convertAndSend("/topic/notifications/" + dto.getRecipientId(), payload);
        }catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String applyTemplate(String template, Map<String, String>variables){
        if(variables == null) return template;
        for(Map.Entry<String, String> entry : variables.entrySet()){
            template = template.replace("{" + entry.getKey()+"}", entry.getValue());
        }
        return template;
    }


    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.getNotificationsByUser(userId);
    }

    @Override
    public void markAsRead(Long notiIdx, Long userId) {
        notificationMapper.markNotificationAsRead(notiIdx, userId);
    }

    @Override
    public void delete(Long notiIdx, Long userId) {
        notificationMapper.deleteNotification(notiIdx, userId);
    }



}

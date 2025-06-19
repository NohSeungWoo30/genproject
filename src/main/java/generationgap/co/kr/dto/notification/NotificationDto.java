package generationgap.co.kr.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationDto {
    private Long recipientId;
    private Long senderId;
    private Long notiTypeIdx;
    private Map<String, String> variables;    // {title}, {groupName} 등
    private String notiUrl;                   // 실제 이동할 URL
}

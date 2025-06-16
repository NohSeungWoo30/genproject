package generationgap.co.kr.domain.notification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Notification {
    private Long notiIdx;
    private Long userIdx;
    private String notiMessage;
    private Long notiTypeIdx;
    private String notiUrl;
    private String isRead; //Y or N
    private String isDeleted; //Y or N
    private LocalDateTime sentAt;
}

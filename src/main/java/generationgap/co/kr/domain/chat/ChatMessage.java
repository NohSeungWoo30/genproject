package generationgap.co.kr.domain.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private Long messagesIdx;
    private String groupChatIdx;
    private Integer senderIdx;
    private String nickname;
    private String content;
    private LocalDateTime sentAt;
    private String isDeleted; // 'Y' 또는 'N'
    private LocalDateTime deletedAt;

}

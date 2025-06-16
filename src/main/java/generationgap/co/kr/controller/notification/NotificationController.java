package generationgap.co.kr.controller.notification;

import generationgap.co.kr.domain.notification.Notification;
import generationgap.co.kr.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import generationgap.co.kr.security.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ✅ 알림 목록 조회
    @GetMapping
    public List<Notification> getNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        return notificationService.getUserNotifications(user.getUserIdx());
    }

    // ✅ 알림 읽음 처리
    @PostMapping("/{notiIdx}/read")
    public void markAsRead(@PathVariable Long notiIdx,
                           @AuthenticationPrincipal CustomUserDetails user) {
        notificationService.markAsRead(notiIdx, user.getUserIdx());
    }

    // ✅ 알림 삭제 처리
    @DeleteMapping("/{notiIdx}")
    public void delete(@PathVariable Long notiIdx,
                       @AuthenticationPrincipal CustomUserDetails user) {
        notificationService.delete(notiIdx, user.getUserIdx());
    }
}


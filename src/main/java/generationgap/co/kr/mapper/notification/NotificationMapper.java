package generationgap.co.kr.mapper.notification;

import generationgap.co.kr.domain.notification.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    //템플릿 조회
    String getTemplateByType(@Param("notiTypeIdx")Long notiTypeIdx);

    //알림 저장
    /*void insertNotification(@Param("recipientId") Long recipientId,
                            @Param("message") String message,
                            @Param("notiTypeIdx") Long notiTypeIdx,
                            @Param("notiUrl") String notiUrl);*/
    void insertNotification(Notification notification); // ✅ 도메인 객체 기반으로


    List<Notification> getNotificationsByUser(@Param("userId") Long userId);

    void markNotificationAsRead(@Param("notiIdx") Long notiIdx, @Param("userId") Long userId);

    void deleteNotification(@Param("notiIdx") Long notiIdx, @Param("userId") Long userId);
}

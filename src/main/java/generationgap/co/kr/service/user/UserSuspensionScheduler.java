package generationgap.co.kr.service.user;

import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.service.report.UserSuspensionService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class UserSuspensionScheduler {

    private final UserMapper userMapper;

    private final DataSource dataSource; // ✅ 추가


    private final UserSuspensionService userSuspensionService;


    public void testManualSelect() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT user_id, is_suspended, suspend_until, SYSTIMESTAMP AS now " +
                             "FROM users " +
                             "WHERE is_suspended = 1"
             );
             ResultSet rs = ps.executeQuery()) {

            System.out.println("📥 JDBC 직접 비교 테스트 ↓↓↓");

            while (rs.next()) {
                String userId = rs.getString("user_id");
                int isSuspended = rs.getInt("is_suspended");
                Timestamp until = rs.getTimestamp("suspend_until");
                Timestamp now = rs.getTimestamp("now"); // SYSTIMESTAMP 비교용

                System.out.println("🔹 userId=" + userId + ", until=" + until + ", now=" + now);
                System.out.println("➡️ 비교 결과: " + until.before(now));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostConstruct  // ✅ 애플리케이션 시작 시 자동 실행됨
    public void printCurrentDbUrl() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("🔗 현재 연결된 DB 사용자: " + conn.getMetaData().getUserName());
            System.out.println("🔗 현재 연결된 DB URL: " + conn.getMetaData().getURL());



            testManualSelect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Scheduled(cron = "0 */1 * * * *")   // 매 1분마다
    public void scheduledRelease() {
        userSuspensionService.releaseSuspendedUsers(); // 트랜잭션은 여기서 처리
    }



//    //매일 새벽 1시 실행
//    @Transactional
//    //@Scheduled(cron = "0 0 1 * * *")
//    @Scheduled(cron = "0 */1 * * * *")   // 매 1분마다 (테스트용)
//    public void releaseSuspendedUsers(){
//
//        try (Connection conn = dataSource.getConnection()) {
//            System.out.println("📌 연결된 DB 사용자: " + conn.getMetaData().getUserName());
//            System.out.println("📌 연결된 DB URL: " + conn.getMetaData().getURL());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        System.out.println("⏰ [SuspensionScheduler] 실행됨");
//
//        List<UserDTO> targets = userMapper.findSuspendCandidates();
//        System.out.println("🟡 해제 대상 수(SELECT): " + targets.size());
//        targets.forEach(u -> System.out.println("→ " + u.getUserId()));
//
//        int releasedCount = userMapper.releaseExpiredSuspensions();
//        System.out.println("[SuspensionScheduler] 해제된 유저 수: " + releasedCount);
//
//        List<UserDTO> suspended = userMapper.findSuspendCandidates();
//        System.out.println("현재 해제 대상 유저 수: " + suspended.size());
//        suspended.forEach(u -> System.out.println("해제 대상: " + u.getUserId()));
//
//        System.out.println("Java LocalDateTime.now() = " + LocalDateTime.now());
//        System.out.println("Java ZonedDateTime.now() = " + ZonedDateTime.now());
//    }
}

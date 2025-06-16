package generationgap.co.kr.service.report;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class UserSuspensionService {

    private final UserMapper userMapper;


    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;      // ✅ 시간 확인용



    // ① 현재 자바가 바라보는 users 테이블의 owner를 확인
    @PostConstruct
    public void checkTableOwner() {
        System.out.println("📋 [테이블 소유자 확인]");
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT owner, table_name FROM all_tables WHERE table_name = 'USERS'"
        );
        for (Map<String, Object> row : result) {
            System.out.println("📂 테이블 소유자: " + row.get("OWNER") + ", 테이블명: " + row.get("TABLE_NAME"));
        }
    }

    // ② 현재 자바가 연결된 DB의 시간 확인
    @PostConstruct
    public void printDbTime() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT SYSTIMESTAMP FROM dual");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("🕒 자바에서 보는 현재 DB 시간: " + rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }









    // 별도의 트랜잭션으로 실행 (신고 처리 트랜잭션과 분리) -> 정지처리만 담당
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void suspendUserManually(Long userId) {
        UserDTO user = userMapper.findByUserIdx(userId);
        if(user.getIsSuspended() == 1){
            System.out.println("⚠️ 이미 정지된 유저입니다. 중복 정지 생략");
            return;
        }
        System.out.println("⏳ suspendUserManually() 시작");
        try {
            userMapper.suspendUser(userId);
            System.out.println("🧪 suspendUser() 실행 직후 - 중간 확인 로그");
            System.out.println("✅ suspendUserManually() 성공");
        } catch (Exception e) {
            System.err.println("❌ suspendUserManually() 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Transactional
    public void releaseSuspendedUsers() {

        System.out.println("⏰ [1] 쿼리 실행 전 상태");
        List<UserDTO> before = userMapper.findSuspendCandidates();
        System.out.println("🔍 실행 전 대상 수: " + before.size());

        for (UserDTO u : before) {
            System.out.println("→ [전] userId: " + u.getUserId());
            System.out.println("     └ isSuspended: " + u.getIsSuspended());
            System.out.println("     └ suspendUntil: " + u.getSuspendUntil());
        }

        int releasedCount = userMapper.releaseExpiredSuspensions();
        System.out.println("✅ 실제 업데이트된 유저 수: " + releasedCount);

        System.out.println("⏰ [2] 쿼리 실행 후 상태");
        List<UserDTO> after = userMapper.findSuspendCandidates();
        System.out.println("🔍 실행 후 남은 대상 수: " + after.size());

        for (UserDTO u : after) {
            System.out.println("→ [후] userId: " + u.getUserId());
            System.out.println("     └ isSuspended: " + u.getIsSuspended());
            System.out.println("     └ suspendUntil: " + u.getSuspendUntil());
        }
    }
}

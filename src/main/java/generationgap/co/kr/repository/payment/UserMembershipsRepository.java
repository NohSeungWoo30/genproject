
package generationgap.co.kr.repository.payment;

import generationgap.co.kr.domain.payment.UserMemberships;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // java.util.Optional을 import 해야 합니다.


public interface UserMembershipsRepository extends JpaRepository<UserMemberships, Long> {

    /**
     * [수정] user_idx로 멤버십 정보를 찾는 메소드를 추가해야 합니다.
     * 이 메소드가 없으면 UserMembershipsService에서 해당 메소드를 찾지 못해 오류가 발생합니다.
     */
    Optional<UserMemberships> findByUserIdx(Long userIdx);
}
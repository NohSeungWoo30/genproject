// src/main/java/generationgap/co/kr/service/payment/UserMembershipsService.java
package generationgap.co.kr.service.payment;

import generationgap.co.kr.domain.payment.Payment;
import generationgap.co.kr.domain.payment.UserMemberships;
import generationgap.co.kr.repository.payment.UserMembershipsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserMembershipsService {

    private final UserMembershipsRepository userMembershipsRepository;

    /**
     * 사용자의 멤버십 레코드를 찾고, 없으면 새로 생성하여 반환하는 private 헬퍼 메소드
     * @param userIdx 사용자의 고유 ID
     * @return 찾아오거나 새로 생성한 UserMemberships 객체
     */
    private UserMemberships findOrCreateMembership(Long userIdx) {
        return userMembershipsRepository.findByUserIdx(userIdx)
                .orElseGet(() -> {
                    UserMemberships newMembership = new UserMemberships();
                    newMembership.setUserIdx(userIdx);
                    newMembership.setRemainingUses(0); // 최초 생성 시 횟수는 0으로 초기화
                    newMembership.setStatus("ACTIVE");
                    return newMembership;
                });
    }

    /**
     * [무료 제공 로직] 신규 회원 가입 시 호출
     * 사용자의 멤버십 레코드에 무료 이용 횟수 3회를 '추가'합니다.
     * @param userIdx 사용자의 고유 ID
     */
    @Transactional
    public void grantInitialFreeUses(Long userIdx) {
        UserMemberships membership = findOrCreateMembership(userIdx);
        membership.setRemainingUses(membership.getRemainingUses() + 3);
        userMembershipsRepository.save(membership);
    }

    /**
     * [추가 구매 로직] 결제 성공 시 호출
     * 사용자의 단일 멤버십 레코드에 구매한 상품의 가치를 '누적'합니다.
     * @param payment 성공한 결제 정보 객체
     */
    @Transactional
    public void grantMembershipFromPayment(Payment payment) {
        // 1. 사용자의 유일한 멤버십 레코드를 찾거나 새로 생성합니다.
        UserMemberships membership = findOrCreateMembership(payment.getUserIdx());
        membership.setPaymentIdx(payment.getPaymentIdx()); // 마지막 결제 ID를 기록

        String productName = payment.getProductName();

        if (productName.contains("회 이용권")) {
            // 2-1. 횟수권: 기존 횟수에 구매 횟수를 더합니다.
            int usesToAdd = Integer.parseInt(productName.replaceAll("[^0-9]", ""));
            membership.setRemainingUses(membership.getRemainingUses() + usesToAdd);

        } else if (productName.contains("달 무제한") || productName.contains("년 무제한")) {
            // 2-2. 구독권: 만료일을 연장합니다.
            LocalDate today = LocalDate.now();

            // 기준일 = max(오늘, 기존 만료일). 만료일이 과거면 오늘부터, 유효하면 만료일부터 연장합니다.
            LocalDate baseDate = (membership.getEndDate() != null && membership.getEndDate().isAfter(today))
                    ? membership.getEndDate()
                    : today;

            // 구독을 처음 시작하거나, 만료 후 재시작하는 경우 시작일을 오늘로 설정
            if (membership.getStartDate() == null || membership.getEndDate() == null || membership.getEndDate().isBefore(today)) {
                membership.setStartDate(today);
            }

            LocalDate newEndDate;
            if (productName.contains("달")) {
                int monthsToAdd = Integer.parseInt(productName.replaceAll("[^0-9]", ""));
                newEndDate = baseDate.plusMonths(monthsToAdd);
            } else { // '년'
                int yearsToAdd = Integer.parseInt(productName.replaceAll("[^0-9]", ""));
                newEndDate = baseDate.plusYears(yearsToAdd);
            }
            membership.setEndDate(newEndDate);
        } else {
            System.err.println("알 수 없는 상품명으로 이용권 부여 실패: " + productName);
            return;
        }

        // 3. 변경된 멤버십 정보를 데이터베이스에 저장합니다.
        userMembershipsRepository.save(membership);
    }

    /**
     * [횟수 차감 로직] 사용자가 모임 생성/참여 등 서비스를 이용할 때 호출
     * @param userIdx 사용자의 고유 ID
     * @return 사용 가능한 이용권이 있어 차감에 성공하거나, 유효한 구독권이 있으면 true, 없으면 false
     */
    @Transactional
    public boolean deductUsage(Long userIdx) {
        Optional<UserMemberships> optMembership = userMembershipsRepository.findByUserIdx(userIdx);

        // 멤버십 레코드 자체가 없는 경우
        if (optMembership.isEmpty()) {
            return false;
        }

        UserMemberships membership = optMembership.get();

        // 1순위: 횟수제 이용권이 있으면 차감하고 true 반환
        if (membership.getRemainingUses() > 0) {
            membership.setRemainingUses(membership.getRemainingUses() - 1);
            userMembershipsRepository.save(membership);
            return true;
        }

        // 2순위: 횟수권이 없을 경우, 무제한 구독권 기간이 유효한지 확인. 유효하면 true 반환
        if (membership.getEndDate() != null && membership.getEndDate().isAfter(LocalDate.now().minusDays(1))) {
            return true;
        }

        // 모든 이용 수단이 없는 경우
        return false;
    }
}
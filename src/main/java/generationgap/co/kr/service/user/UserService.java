package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger; // Logger 임포트
import org.slf4j.LoggerFactory; // LoggerFactory 임포트
import java.util.Optional;
import java.time.LocalDate; // LocalDate 임포트 (birthDate, signupDate, lastLoginAt, updateAt, ghost)

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class); // Slf4j Logger 사용

    @Autowired
    private UserMapper userMapper;

    @Autowired // PasswordEncoder 주입
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserDTO user) {

        log.info("회원 등록 요청: userId={}", user.getUserId());

        // 비밀번호 해싱 (필수)
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordHash(hashedPassword);

        // provider 필드 기본값 설정
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            user.setProvider("LOCAL");
        }

        // profileName 기본값 설정 (null이거나 비어있을 경우)
        if (user.getProfileName() == null || user.getProfileName().isEmpty()) {
            user.setProfileName("default_profile.jpg"); // 또는 ""
        }

        // signupDate는 DB DEFAULT SYSDATE가 있으므로 여기서 설정 안 해도 됨
        // if (user.getSignupDate() == null) {
        //     user.setSignupDate(LocalDateTime.now());
        // }

        try {
            userMapper.insertUser(user);
            log.info("회원 등록 성공: userIdx={}, userId={}", user.getUserIdx(), user.getUserId());
        } catch (Exception e) {
            log.error("회원 등록 실패: userId={}, Error: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("회원 등록 중 오류 발생", e); // 예외를 던져 트랜잭션 롤백 유도
        }
    }

    // 2. userIdx로 사용자 정보 조회 (프로필 수정 폼 로드용)
    public UserDTO getUserProfile(Long userIdx) {
        log.debug("사용자 프로필 조회 요청: userIdx={}", userIdx);
        UserDTO user = userMapper.findByUserIdx(userIdx);
        if (user == null) {
            log.warn("사용자 프로필 조회 실패: userIdx {}에 해당하는 사용자를 찾을 수 없습니다.", userIdx);
        }
        return user;
    }

    // 3. 일반 회원 정보 수정
    @Transactional
    public boolean updateUserInfo(UserDTO user) {
        log.info("회원 정보 업데이트 요청: userIdx={}", user.getUserIdx());

        // 현재 DB에 저장된 사용자 정보를 가져와 유효성 확인 및 기존 값 보존
        UserDTO existingUser = userMapper.findByUserIdx(user.getUserIdx());
        if (existingUser == null) {
            log.warn("회원 정보 업데이트 실패: userIdx {}에 해당하는 사용자를 찾을 수 없습니다.", user.getUserIdx());
            return false;
        }

        // ✅ 이메일 중복 확인 로직 (변경된 이메일이 다른 사용자에게 이미 사용 중인지 확인)
        // 사용자가 새 이메일을 입력했고, 그 이메일이 기존 이메일과 다를 경우에만 검사
        if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                !existingUser.getEmail().equalsIgnoreCase(user.getEmail())) { // 대소문자 무시 비교
            UserDTO duplicateEmailUser = userMapper.findByEmail(user.getEmail());
            if (duplicateEmailUser != null) {
                log.warn("회원 정보 업데이트 실패: 이메일 '{}'는 이미 사용 중입니다.", user.getEmail());
                // 컨트롤러에서 처리할 수 있도록 IllegalArgumentException 발생
                throw new IllegalArgumentException("입력하신 이메일은 이미 사용 중입니다.");
            }
        }

        // 폼에서 넘어온 user 객체의 필드들을 기존 사용자 정보에 업데이트
        // DTO 필드명에 맞게 userName, nickname 등 정확히 사용
        existingUser.setUserName(user.getUserName());
        existingUser.setNickname(user.getNickname());
        existingUser.setBirthDate(user.getBirthDate());
        existingUser.setGender(user.getGender());
        existingUser.setEmail(user.getEmail()); // 이메일은 위에서 중복 확인 후 설정
        existingUser.setPhone(user.getPhone());
        existingUser.setProfileName(user.getProfileName());
        existingUser.setIntroduction(user.getIntroduction());
        // updateAt은 Mapper에서 CURRENT_DATE/TIMESTAMP로 설정되므로 여기서 직접 설정할 필요 없음

        try {
            userMapper.updateUserInfo(existingUser); // userIdx를 포함한 UserDTO 전체를 넘겨 업데이트
            log.info("회원 정보 업데이트 성공: userIdx={}", user.getUserIdx());
            return true;
        } catch (Exception e) {
            log.error("회원 정보 업데이트 실패: userIdx={}, Error: {}", user.getUserIdx(), e.getMessage(), e);
            throw new RuntimeException("회원 정보 업데이트 중 오류 발생", e);
        }
    }

    // 4. 비밀번호 변경
    @Transactional
    public boolean updatePassword(Long userIdx, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 요청: userIdx={}", userIdx);

        UserDTO user = userMapper.findByUserIdx(userIdx);
        if (user == null) {
            log.warn("비밀번호 변경 실패: userIdx {}에 해당하는 사용자를 찾을 수 없습니다.", userIdx);
            return false; // 사용자 없음
        }

        // 현재 비밀번호 일치 여부 확인 (passwordHash와 비교)
        // passwordEncoder.matches(평문 비밀번호, 암호화된 비밀번호)
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("비밀번호 변경 실패: userIdx {}의 현재 비밀번호가 일치하지 않습니다.", userIdx);
            return false; // 현재 비밀번호 불일치
        }

        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        try {
            // UserMapper의 updateUserPassword 메서드 시그니처에 맞게 호출
            userMapper.updateUserPassword(userIdx, encodedNewPassword);
            log.info("비밀번호 변경 성공: userIdx={}", userIdx);
            return true;
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: userIdx={}, Error: {}", userIdx, e.getMessage(), e);
            throw new RuntimeException("비밀번호 변경 중 오류 발생", e);
        }
    }

    /**
     * 이름과 전화번호로 사용자 아이디를 찾습니다.
     * 찾은 아이디는 마스킹 처리하여 반환합니다.
     * @param userName 사용자 이름
     * @param phone 사용자 전화번호
     * @return 마스킹된 사용자 아이디 (찾지 못하면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<String> findUserIdByUserNameAndPhone(String userName, String phone) {
        log.info("아이디 찾기 시도: 이름={}, 전화번호={}", userName, phone);

        // ✅ UserDTO 객체를 생성하고 userName과 phone 필드만 설정
        UserDTO userDto = new UserDTO();
        userDto.setUserName(userName);
        userDto.setPhone(phone);
        // ✅ userMapper의 findByUserNameAndPhone 메서드가 이제 Optional<String>을 반환한다고 가정 (원래대로)
        Optional<String> userIdOptional = userMapper.findByUserNameAndPhone(userDto);
        // 💡 주의: userMapper의 파라미터가 UserDTO로 변경되었다면 이렇게 UserDTO를 생성하여 넘겨야 합니다.
        // 만약 userMapper가 여전히 개별 String 파라미터를 받는다면 (UserMapper.java의 findByUserNameAndPhone 시그니처를 확인하세요):
        // Optional<String> userIdOptional = userMapper.findByUserNameAndPhone(userName, phone);

        log.debug("User ID Optional 결과: {}", userIdOptional); // 진단용 로그는 이제 필요없다면 제거 가능

        if (userIdOptional.isPresent()) {
            String userId = userIdOptional.get();
            String maskedUserId = maskUserId(userId);
            log.info("아이디 찾기 성공: 원본 아이디={}, 마스킹된 아이디={}", userId, maskedUserId);
            return Optional.of(maskedUserId);
        } else {
            log.warn("아이디 찾기 실패: 이름={}, 전화번호={}에 해당하는 사용자 없음. (매퍼 반환 Optional 비어있음)", userName, phone);
            return Optional.empty();
        }
    }

    /**
     * 사용자 아이디를 마스킹 처리합니다. (예: userid -> u*erid)
     * 이 마스킹 로직은 '첫 글자 + * + 나머지 문자열(두 번째 글자부터)' 형태입니다.
     * 아이디가 너무 짧은 경우 (1글자 이하)는 마스킹하지 않고 그대로 반환합니다.
     *
     * @param userId 원본 사용자 아이디
     * @return 마스킹된 사용자 아이디
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() < 2) {
            return userId; // 1글자 이하는 마스킹하지 않음 (또는 오류 처리)
        }
        // 예: "userid" -> "u*erid"
        // 첫 글자 + '*' + 두 번째 글자부터 끝까지
        return userId.substring(0, 1) + "*" + userId.substring(2);
    }
}

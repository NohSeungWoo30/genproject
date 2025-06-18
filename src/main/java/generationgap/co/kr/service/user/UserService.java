package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.payment.UserMemberships;
import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.repository.payment.UserMembershipsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class); // Slf4j Logger 사용

    @Autowired
    private UserMapper userMapper;

    @Autowired // PasswordEncoder 주입
    private PasswordEncoder passwordEncoder;

    @Autowired // 횟수를 위한
    private UserMembershipsRepository userMembershipsRepository;

    @Value("${file.upload-dir.profile}") // application.properties에서 설정한 경로 주입
    private String uploadDir;

    @Transactional // <--- @Transactional 어노테이션 확인
    public void registerUser(UserDTO user, MultipartFile profileImageFile) { // ⭐ MultipartFile 파라미터 추가
        log.info("회원 등록 요청: userId={}", user.getUserId());

        // 비밀번호 해싱 (필수)
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordHash(hashedPassword);

        // provider 필드 기본값 설정
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            user.setProvider("LOCAL");
        }

        // ⭐ 프로필 사진 처리 로직 추가 (회원가입 시)
        try {
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                // 파일 유효성 검사 (updateProfilePicture와 동일한 로직 사용)
                validateProfileImageFile(profileImageFile);

                String originalFilename = profileImageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path uploadPath = Paths.get(uploadDir);
                String dbPath = "/profile_images/" + uniqueFileName; // 웹 접근 경로

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path targetPath = uploadPath.resolve(uniqueFileName);
                Files.copy(profileImageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                user.setProfileName(dbPath); // 업로드된 파일 경로로 profileName 설정
                log.info("회원가입 시 프로필 이미지 업로드 성공: originalName={}, savedPath={}", originalFilename, dbPath);

            } else {
                // 파일이 업로드되지 않았다면 기본 이미지 설정
                user.setProfileName("/images/profile_upload/default_profile.jpg");
                log.info("회원가입 시 프로필 이미지 없음. 기본 이미지로 설정.");
            }
        } catch (IOException e) {
            log.error("회원가입 시 프로필 이미지 파일 저장 실패: userId={}, Error: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 저장 중 오류 발생", e);
        } catch (IllegalArgumentException e) {
            log.error("회원가입 시 프로필 이미지 유효성 검사 실패: userId={}, Error: {}", user.getUserId(), e.getMessage());
            throw e; // 유효성 검사 실패 예외 다시 던지기
        }

        try {
            userMapper.insertUser(user);
            // UserMemberships 정보 생성 및 저장
            user = userMapper.findByUserId(user.getUserId());
            UserMemberships userMembership = new UserMemberships();
            userMembership.setUserIdx(user.getUserIdx()); // 저장된 유저의 user_idx를 가져와 설정
            userMembership.setRemainingUses(1); // remainingUses를 1로 설정
            userMembershipsRepository.save(userMembership); // UserMemberships 저장
            log.info("회원 등록 성공: userIdx={}, userId={}", user.getUserIdx(), user.getUserId());
        } catch (Exception e) {
            log.error("회원 등록 실패: userId={}, Error: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("회원 등록 중 오류 발생", e);
        }
    }

    // ⭐ 파일 유효성 검사 로직 (재사용을 위해 분리)
    private void validateProfileImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        long size = file.getSize();

        // 이미지 파일 형식 검증 (JPEG, PNG, GIF, WEBP만 허용)
        if (contentType == null || !contentType.matches("image/(jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("이미지 파일 (JPG, PNG, GIF, WEBP)만 업로드할 수 있습니다.");
        }

        // 파일 크기 검증 (10MB 이하)
        if (size > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("파일은 10MB 이하만 업로드 가능합니다.");
        }
    }

    // ⭐ OAuth2 사용자 정보 저장 또는 업데이트 메서드 추가 ⭐
    @Transactional
    public UserDTO saveOrUpdateOAuthUser(UserDTO userDTO) { // ✅ 파라미터를 UserDTO로 변경
        log.info("UserService.saveOrUpdateOAuthUser 진입: Email={}", userDTO.getEmail());

        if (userMapper == null) {
            log.error("!!!! UserService 내부 userMapper가 NULL입니다. (saveOrUpdateOAuthUser) !!!!");
            throw new IllegalStateException("UserMapper is null inside UserService (OAuth)!");
        }

        // userDTO에서 필요한 정보 추출
        String provider = userDTO.getProvider();
        String userId = userDTO.getUserId(); // UserDTO의 userId는 Google의 'sub' 값입니다.

        // 기존 사용자 조회
        UserDTO existingUser = userMapper.findByProviderAndUserId(provider, userId);
        log.info("userId 값 확인");
        if (existingUser == null) {
            // 새로운 사용자 등록
            // userDTO는 CustomOAuth2UserService에서 이미 필요한 모든 정보 (이름, 이메일, 전화번호, 성별, 생년월일 등)를 포함하고 있습니다.
            // OAuthAttributes.toDto()에서 기본값 (ROLE, STATUS, signupDate 등)이 이미 설정되었거나, 여기서 추가 설정합니다.

            // 현재 시간을 기준으로 날짜 정보 설정 (toDto에서 했다면 스킵 가능)
            if (userDTO.getSignupDate() == null) {
                userDTO.setSignupDate(LocalDate.now());
            }
            if (userDTO.getLastLoginAt() == null) {
                userDTO.setLastLoginAt(LocalDate.now());
            }
            if (userDTO.getUpdateAt() == null) {
                userDTO.setUpdateAt(LocalDate.now());
            }
            if (userDTO.getUserStatus() == null || userDTO.getUserStatus().isEmpty()) {
                userDTO.setUserStatus("ACTIVE"); // 기본 상태 설정
            }
            if (userDTO.getProfileName() == null || userDTO.getProfileName().isEmpty()) {
                userDTO.setProfileName("/images/profile_upload/default_profile.jpg"); // 기본 프로필 이미지 설정
            }
            userDTO.setPasswordHash(""); // OAuth 사용자는 비밀번호 해시가 필요 없음

            try {
                userMapper.insertOAuthUser(userDTO); // Mybatis insertOAuthUser 메서드 호출
                // UserMemberships 정보 생성 및 저장
                userDTO = userMapper.findByOAuth2UserId(userDTO.getUserId());
                UserMemberships userMembership = new UserMemberships();
                userMembership.setUserIdx(userDTO.getUserIdx()); // 저장된 유저의 user_idx를 가져와 설정
                userMembership.setRemainingUses(1); // remainingUses를 1로 설정
                userMembershipsRepository.save(userMembership); // UserMemberships 저장
                log.info("새로운 {} 사용자 등록: userId={}, email={}", provider, userDTO.getUserId(), userDTO.getEmail());
                return userDTO; // 삽입된 DTO 반환
            } catch (Exception e) {
                log.error("새로운 {} 사용자 등록 실패: userId={}, Error: {}", provider, userDTO.getUserId(), e.getMessage(), e);
                throw new RuntimeException("새로운 OAuth 사용자 등록 중 오류 발생", e);
            }

        } else {
            // 기존 사용자 정보 업데이트
            // CustomOAuth2UserService에서 People API를 통해 가져온 최신 정보를 existingUser에 반영
            existingUser.setUserName(userDTO.getUserName());
            existingUser.setEmail(userDTO.getEmail());
            //existingUser.setNickname(userDTO.getNickname());
            existingUser.setProfileName(userDTO.getProfileName()); // ✅ 프로필 이미지 URL 업데이트
            //existingUser.setPhone(userDTO.getPhone());             // ✅ 전화번호 업데이트
            existingUser.setGender(userDTO.getGender());           // ✅ 성별 업데이트
            existingUser.setBirthDate(userDTO.getBirthDate());     // ✅ 생년월일 업데이트
            existingUser.setLastLoginAt(LocalDate.now());          // 마지막 로그인 시간 업데이트
            existingUser.setUpdateAt(LocalDate.now());             // 업데이트 시간 업데이트

            try {
                userMapper.updateUserInfo(existingUser); // 기존 updateUserInfo 재사용
                userMapper.findByProviderAndUserId(provider, userId);
                log.info("기존 {} 사용자 정보 업데이트: userId={}, email={}", provider, existingUser.getUserId(), existingUser.getEmail());
                return existingUser;
            } catch (Exception e) {
                log.error("기존 {} 사용자 정보 업데이트 실패: userId={}, Error: {}", provider, existingUser.getUserId(), e.getMessage(), e);
                throw new RuntimeException("기존 OAuth 사용자 정보 업데이트 중 오류 발생", e);
            }
        }

    }



    public UserDTO findByProviderAndUserId (String provider, String userId){
        return userMapper.findByProviderAndUserId(provider, userId);
    }


    // 2. userIdx로 사용자 정보 조회 (프로필 수정 폼 로드용)
    public UserDTO getUserProfile (Long userIdx){
        log.debug("사용자 프로필 조회 요청: userIdx={}", userIdx);
        UserDTO user = userMapper.findByUserIdx(userIdx);
        if (user == null) {
            log.warn("사용자 프로필 조회 실패: userIdx {}에 해당하는 사용자를 찾을 수 없습니다.", userIdx);
        }
        return user;
    }
    // userId로 사용자 정보 조회 (프로필 수정 폼 로드용)
    public UserDTO getOAuth2UserProfile (String userId){
        log.debug("사용자 프로필 조회 요청: userId={}", userId);
        UserDTO user = userMapper.findByOAuth2UserId(userId);
        if (user == null) {
            log.warn("사용자 프로필 조회 실패: userIdx {}에 해당하는 사용자를 찾을 수 없습니다.", userId);
        }
        return user;
    }

    // 3. 일반 회원 정보 수정
    @Transactional
    public boolean updateUserInfo (UserDTO user){
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
    public boolean updatePassword (Long userIdx, String currentPassword, String newPassword){
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
    public Optional<String> findUserIdByUserNameAndPhone (String userName, String phone){
        log.info("아이디 찾기 시도: 이름={}, 전화번호={}", userName, phone);

        UserDTO userDto = new UserDTO();
        userDto.setUserName(userName);
        userDto.setPhone(phone);

        // MyBatis가 Optional을 지원하지 않으므로 반환값은 String
        String userId = userMapper.findByUserNameAndPhone(userDto);

        if (userId != null) {
            String maskedUserId = maskUserId(userId);
            log.info("아이디 찾기 성공: 원본 아이디={}, 마스킹된 아이디={}", userId, maskedUserId);
            return Optional.of(maskedUserId);
        } else {
            log.warn("아이디 찾기 실패: 이름={}, 전화번호={}에 해당하는 사용자 없음.", userName, phone);
            return Optional.empty();
        }
    }

    //사용자 아이디를 마스킹 처리(예: userid -> u*erid)
    //아이디가 너무 짧은 경우 (1글자 이하)는 마스킹하지 않고 그대로 반환
    //@param userId 원본 사용자 아이디, @return 마스킹된 사용자 아이디
    private String maskUserId (String userId){
        if (userId == null || userId.length() < 2) {
            return userId; // 1글자 이하는 마스킹하지 않음 (또는 오류 처리)
        }

        // 첫 글자 + '*' + 두 번째 글자부터 끝까지
        return userId.substring(0, 1) + "*" + userId.substring(2);
    }

    // --- ⭐⭐ 소프트 삭제 메서드 구현 ⭐⭐ ---
    /**
     * 일반 (로컬) 사용자 계정을 소프트 삭제합니다.
     * 비밀번호 검증이 필요합니다.
     *
     * @param userId        삭제할 사용자의 ID
     * @param passwordConfirm 사용자가 입력한 현재 비밀번호
     */
    @Transactional
    public void softDeleteLocalUser(String userId, String passwordConfirm) {
        // 1. userId로 사용자 정보 조회 (userStatus 관계없이, 비밀번호 검증 위함)
        UserDTO user = userMapper.findByUserIdForAuthentication(userId);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 2. 이미 삭제된 계정인지 확인 (선택 사항이지만 유용)
        if ("DELETED".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("이미 삭제된 계정입니다.");
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(passwordConfirm, user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 4. 소프트 삭제를 위한 UserDTO 업데이트 (공통 로직 메서드 호출)
        updateUserForSoftDeletion(user);

        // 5. Mapper를 통해 DB 업데이트
        userMapper.softDeleteUser(user); // softDeleteUser 대신 updateUser로 통합
        // userMapper.softDeleteUser(user); // DTO 전체를 넘겨서 XML에서 업데이트
        // 참고: softDeleteUser라는 메서드가 userStatus 변경 외에 추가적인 로직을 한다면 그대로 사용.
        // 그렇지 않다면, 단순히 user 객체를 받아 업데이트하는 일반적인 updateUser 메서드로 충분합니다.
        // XML 쿼리가 softDeleteUser에 맞춰져 있다면 그대로 두세요.
        // log.info("로컬 사용자 소프트 삭제 완료: userId={}", userId);
    }

    /**
     * 구글 간편 로그인 사용자 계정을 소프트 삭제합니다.
     * 비밀번호 검증 없이 바로 삭제를 진행합니다.
     *
     * @param userId 삭제할 구글 간편 로그인 사용자의 ID (이메일 등 고유 식별자)
     */
    @Transactional
    public void softDeleteGoogleUser(String userId) {
        // 1. userId로 사용자 정보 조회 (userStatus 관계없이, 삭제 대상 확인 위함)
        UserDTO user = userMapper.findByUserIdForAuthentication(userId); // 또는 findByEmail 등 Google 유저 식별자에 맞는 메서드 사용
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 구글 간편 로그인 사용자입니다.");
        }

        // 2. 이미 삭제된 계정인지 확인 (선택 사항이지만 유용)
        if ("DELETED".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("이미 삭제된 계정입니다.");
        }

        // ⭐ 비밀번호 검증 단계 생략 ⭐

        // 3. 소프트 삭제를 위한 UserDTO 업데이트 (공통 로직 메서드 호출)
        updateUserForSoftDeletion(user);

        // 4. Mapper를 통해 DB 업데이트
        userMapper.softDeleteUser(user); // 또는 userMapper.softDeleteUser(user);
        // log.info("구글 간편 로그인 사용자 소프트 삭제 완료: userId={}", userId);
    }

    /**
     * 소프트 삭제 시 공통으로 적용되는 UserDTO 업데이트 로직
     * (UNIQUE 제약 조건 회피 및 상태 변경)
     */
    private void updateUserForSoftDeletion(UserDTO user) {
        user.setUserStatus("DELETED"); // 상태를 'DELETED'로 변경
        user.setGhost(LocalDate.now()); // 현재 날짜로 삭제 일시 기록

        String uuid = UUID.randomUUID().toString(); // 고유값 생성을 위한 UUID

        // UNIQUE 제약 조건이 있는 필드들을 고유한 값으로 변경
        // 이렇게 해야 나중에 다른 사용자가 이메일이나 닉네임을 재사용
        // 그리고 삭제된 사용자가 재로그인 시 새로운 계정으로 인식될 수 있음.

        // userId는 PK나 고유 식별자로 사용될 수 있으므로, 변경 시 주의가 필요합니다.
        // DB 테이블 설계에 따라 userId가 UNIQUE면 변경하고, 아니면 그대로 둘 수 있습니다.
        // 만약 user.getUserId()가 Google OAuth2의 sub 값이라면, 다시 로그인 시 동일한 sub 값이
        // 들어올 것이므로, Unique 제약이 있다면 변경해야 합니다.
        if (user.getUserId() != null && !user.getUserId().isEmpty()) {
            // "deleted_" 접두사와 UUID를 조합하여 더 고유하게 만듭니다.
            user.setUserId("del_" + user.getUserId() + uuid);
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            user.setEmail("del_" + user.getEmail() + uuid);
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            user.setPhone("del_" + user.getPhone() + uuid);
        }
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            user.setNickname("del_" + user.getNickname() + uuid);
        }
    }

    // 아이디 중복 확인
    public boolean isUserIdDuplicated (String userId){
        return userMapper.countByUserId(userId) > 0;
    }

    // 닉네임 중복 확인
    public boolean isNicknameDuplicated (String nickname){
        return userMapper.countByNickname(nickname) > 0;
    }

    // 이메일 중복 확인
    public boolean isEmailDuplicated (String email){
        return userMapper.countByEmail(email) > 0;
    }

    // 전화번호 중복 확인
    public boolean isPhoneDuplicated (String phone){
        return userMapper.countByPhone(phone) > 0;
    }

    // Ci 확인
    public boolean isUserCiDuplicated (String userCi){
        return userMapper.countByUserCi(userCi) > 0;
    }

    public UserDTO getUserProfileByEmail (String email){
        return userMapper.findByEmail(email);
    }

    // ⭐ 프로필 수정 처리 메서드 (이미지 첨부 및 삭제 기능 포함) ⭐
    @Transactional
    public void updateUserProfile(UserDTO user, MultipartFile profileImageFile, boolean deleteProfileImage) {
        log.info("회원 프로필 업데이트 요청: userIdx={}, userId={}", user.getUserIdx(), user.getUserId());

        // 1. 기존 사용자 정보 조회 (현재 프로필 이미지 경로를 얻기 위해)
        UserDTO existingUser = userMapper.findByUserIdx(user.getUserIdx());
        if (existingUser == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 2. 프로필 이미지 처리 로직
        try {
            if (deleteProfileImage) {
                // 이미지 삭제 요청이 있을 경우
                deleteExistingProfileImage(existingUser.getProfileName()); // 기존 이미지 파일 삭제
                user.setProfileName("/images/profile_upload/default_profile.jpg"); // 기본 이미지로 설정
                log.info("프로필 이미지 삭제 및 기본 이미지 설정: userIdx={}", user.getUserIdx());
            } else if (profileImageFile != null && !profileImageFile.isEmpty()) {
                // 새로운 이미지 파일이 첨부된 경우 (기존 이미지 대체)
                validateProfileImageFile(profileImageFile); // 파일 유효성 검사

                // 기존 이미지 파일이 default가 아니면 삭제
                if (existingUser.getProfileName() != null && !existingUser.getProfileName().equals("/images/profile_upload/default_profile.jpg")) {
                    deleteExistingProfileImage(existingUser.getProfileName());
                }

                String originalFilename = profileImageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path uploadPath = Paths.get(uploadDir);
                String dbPath = "/profile_images/" + uniqueFileName; // 웹 접근 경로

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path targetPath = uploadPath.resolve(uniqueFileName);
                Files.copy(profileImageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                user.setProfileName(dbPath); // 업로드된 파일 경로로 profileName 설정
                log.info("프로필 이미지 업로드 성공: userIdx={}, originalName={}, savedPath={}", user.getUserIdx(), originalFilename, dbPath);
            } else {
                // 파일이 첨부되지 않았고 삭제 요청도 없는 경우: 기존 이미지 유지
                user.setProfileName(existingUser.getProfileName());
                log.info("프로필 이미지 변경 없음. 기존 이미지 유지: userIdx={}", user.getUserIdx());
            }
        } catch (IOException e) {
            log.error("프로필 이미지 파일 저장/삭제 실패: userIdx={}, Error: {}", user.getUserIdx(), e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 처리 중 오류 발생", e);
        } catch (IllegalArgumentException e) {
            log.error("프로필 이미지 유효성 검사 실패: userIdx={}, Error: {}", user.getUserIdx(), e.getMessage());
            throw e;
        }

        // 3. 사용자 정보 업데이트 (Mapper 호출)
        try {
            userMapper.updateUserInfo(user); // UserMapper에 updateUser 메서드가 있다고 가정
            log.info("회원 정보(이미지 포함) 업데이트 성공: userIdx={}", user.getUserIdx());
        } catch (Exception e) {
            log.error("회원 정보 업데이트 실패: userIdx={}, Error: {}", user.getUserIdx(), e.getMessage(), e);
            throw new RuntimeException("회원 정보 업데이트 중 오류 발생", e);
        }
    }
    // ⭐ 기존 프로필 이미지 파일 삭제 로직
    private void deleteExistingProfileImage(String profilePath) throws IOException {
        // 기본 이미지는 삭제하지 않음
        if (profilePath == null || profilePath.equals("/images/profile_upload/default_profile.jpg")) {
            return;
        }

        // 웹 접근 경로를 실제 파일 시스템 경로로 변환
        // 예: "/profile_images/abc.jpg" -> "uploadDir/profile_images/abc.jpg"
        Path filePath = Paths.get(uploadDir + profilePath); // 이 경로가 정확해야 합니다.
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("기존 프로필 이미지 파일 삭제 성공: {}", filePath);
        } else {
            log.warn("삭제하려는 프로필 이미지 파일이 존재하지 않습니다: {}", filePath);
        }
    }

    /**
     * 사용자 프로필 사진을 업로드하고 DB에 경로를 업데이트합니다.
     * 로컬 유저에게만 허용됩니다.
     *
     * @param userIdx              사용자 고유 인덱스
     * @param profileImageFile 업로드할 파일
     * @return DB에 저장된 프로필 이미지 경로 (웹 접근 가능 경로)
     * @throws IllegalArgumentException 파일 유효성 검사 실패 시
     * @throws RuntimeException          파일 저장 중 IO 오류 발생 시
     */
    @Transactional
    public String updateProfilePicture(Long userIdx, MultipartFile profileImageFile) {
        // userIdx로 현재 사용자 정보 조회 (이전에 profileName이 있었는지 확인 및 provider 확인 위함)
        UserDTO currentUser = userMapper.findByUserIdx(userIdx);
        if (currentUser == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        // 로컬 유저만 업로드 가능하도록 추가 검증 (컨트롤러에서도 하지만 서비스에서 한번 더)
        if (!"LOCAL".equals(currentUser.getProvider())) {
            throw new IllegalArgumentException("로컬 회원만 프로필 사진을 업로드할 수 있습니다.");
        }

        // 파일 유효성 검사
        if (profileImageFile.isEmpty()) {
            // 파일을 선택하지 않고 제출한 경우, 기본 이미지로 리셋하는 로직을 호출하거나
            // 그냥 현재 상태 유지 (여기서는 그냥 예외 발생)
            throw new IllegalArgumentException("업로드할 프로필 이미지 파일을 선택해주세요.");
        }

        String originalName = profileImageFile.getOriginalFilename();
        String contentType = profileImageFile.getContentType();
        long size = profileImageFile.getSize();

        // 이미지 파일 형식 검증 (JPEG, PNG, GIF, WEBP만 허용)
        if (contentType == null || !contentType.matches("image/(jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("이미지 파일 (JPG, PNG, GIF, WEBP)만 업로드할 수 있습니다.");
        }

        // 파일 크기 검증 (10MB 이하)
        if (size > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("파일은 10MB 이하만 업로드 가능합니다.");
        }

        // 기존 프로필 이미지 삭제 로직 추가
        // 단, 기본 이미지는 삭제하지 않음 (default_profile.jpg)
        String oldProfileName = currentUser.getProfileName();
        // startsWith("/images/profile_upload/") 이 경로가 로컬 업로드된 이미지임을 의미한다고 가정
        if (oldProfileName != null && !oldProfileName.isEmpty() &&
                !oldProfileName.equals("/images/profile_upload/default_profile.jpg") && // 기본 이미지 아니면
                oldProfileName.startsWith("/images/profile_upload/")) { // 로컬에 저장된 이미지라면
            try {
                Path oldFilePath = Paths.get(uploadDir, Paths.get(oldProfileName).getFileName().toString());
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                    log.info("기존 프로필 이미지 삭제 성공: {}", oldFilePath);
                }
            } catch (IOException e) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", oldProfileName, e);
                // 파일 삭제 실패는 업로드 전체를 막을 정도는 아닐 수 있으므로 경고만 남김
            }
        }

        // 새로운 파일 저장
        String fileExtension = "";
        if (originalName != null && originalName.contains(".")) {
            fileExtension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + fileExtension;
        Path uploadPath = Paths.get(uploadDir); // application.properties에서 주입된 경로
        String dbPath = "/profile_images/" + storedName; // 웹 접근 경로 (C:/uploads/ 가 / 로 매핑됨)

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetPath = uploadPath.resolve(storedName);
            Files.copy(profileImageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // DB에 업데이트
            userMapper.updateProfileName(userIdx, dbPath);

            log.info("프로필 이미지 업로드 및 DB 업데이트 성공: userIdx={}, originalName={}, storedPath={}", userIdx, originalName, dbPath);
            return dbPath; // 새로 저장된 DB 경로 반환
        } catch (IOException e) {
            log.error("프로필 이미지 파일 저장 실패: userIdx={}, Error: {}", userIdx, e.getMessage(), e);
            throw new RuntimeException("프로필 이미지 파일 저장 중 오류 발생", e);
        }
    }

    /**
     * 프로필 사진을 기본 이미지로 재설정합니다.
     * @param userIdx 사용자 고유 인덱스
     * @throws IllegalArgumentException 사용자 없음 예외
     * @throws RuntimeException 기존 이미지 삭제 실패 예외
     */
    @Transactional
    public void resetProfilePictureToDefault(Long userIdx) {
        UserDTO currentUser = userMapper.findByUserIdx(userIdx);
        if (currentUser == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        // 로컬 유저만 초기화 가능하도록 검증 (컨트롤러에서도 하지만 서비스에서 한번 더)
        if (!"LOCAL".equals(currentUser.getProvider())) {
            throw new IllegalArgumentException("로컬 회원만 프로필 사진을 초기화할 수 있습니다.");
        }

        // 기존 프로필 이미지 삭제 (기본 이미지가 아닌 경우만)
        String oldProfileName = currentUser.getProfileName();
        // startsWith("/images/profile_upload/") 이 경로가 로컬 업로드된 이미지임을 의미한다고 가정
        if (oldProfileName != null && !oldProfileName.isEmpty() &&
                !oldProfileName.equals("/images/profile_upload/default_profile.jpg") &&
                oldProfileName.startsWith("/images/profile_upload/")) {
            try {
                Path oldFilePath = Paths.get(uploadDir, Paths.get(oldProfileName).getFileName().toString());
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                    log.info("기존 프로필 이미지 삭제 성공 (기본값으로 재설정 시): {}", oldFilePath);
                }
            } catch (IOException e) {
                log.warn("기존 프로필 이미지 삭제 실패 (기본값으로 재설정 시): {}", oldProfileName, e);
                throw new RuntimeException("기존 프로필 이미지 삭제 중 오류 발생", e); // 실패 시 트랜잭션 롤백
            }
        }

        // DB에 기본 이미지 경로로 업데이트
        String defaultDbPath = "/images/profile_upload/default_profile.jpg";
        userMapper.updateProfileName(userIdx, defaultDbPath);
        log.info("프로필 이미지 기본값으로 재설정 및 DB 업데이트 완료: userIdx={}", userIdx);
    }
    public UserDTO getUserId_Nick(int hostIndex){
        return userMapper.getUserId_Nick(hostIndex);
    }
}

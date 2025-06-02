package generationgap.co.kr.config.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "testpassword123!"; // 해싱할 원문 비밀번호
        String hashedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Hashed Password: " + hashedPassword);
        // 이 해시값을 Oracle INSERT 쿼리에 사용합니다.

        String rawPassword2 = "adminpassword!"; // 두 번째 비밀번호
        String hashedPassword2 = passwordEncoder.encode(rawPassword2);
        System.out.println("Raw Password2: " + rawPassword2);
        System.out.println("Hashed Password2: " + hashedPassword2);
    }
}
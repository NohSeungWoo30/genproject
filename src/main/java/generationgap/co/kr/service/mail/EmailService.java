package generationgap.co.kr.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage; // 간단한 텍스트 이메일
import org.springframework.mail.javamail.MimeMessageHelper; // HTML 이메일 또는 첨부파일을 보낼 때 사용
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender; // Spring Boot가 application.properties 설정을 기반으로 자동으로 주입합니다.

    /**
     * 단순 텍스트 이메일을 발송합니다.
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param text 이메일 내용 (평문)
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom("your_email@example.com"); // 발신자 이메일을 명시적으로 설정할 수도 있습니다.
            // 설정하지 않으면 application.properties의 spring.mail.username이 기본값으로 사용됩니다.
            message.setTo(to);      // 수신자 설정
            message.setSubject(subject); // 제목 설정
            message.setText(text);  // 내용 설정

            mailSender.send(message); // 이메일 전송 실행
            log.info("이메일 전송 성공! To: {}, Subject: {}", to, subject);
        } catch (MailException e) {
            log.error("이메일 전송 실패! To: {}, Subject: {}, Error: {}", to, subject, e.getMessage(), e); // 스택 트레이스도 로깅
            // 이메일 전송 실패는 사용자 경험에 큰 영향을 줄 수 있으므로,
            // 실제 서비스에서는 적절한 오류 처리(예: 재시도, 관리자 알림)가 필요합니다.
            throw new RuntimeException("이메일 전송 중 오류 발생: " + e.getMessage(), e); // 예외를 다시 던져 상위 로직에서 처리
        }
    }

    /*
    // HTML 형식의 이메일을 보내고 싶다면 아래와 같은 메서드를 추가할 수 있습니다.
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true는 multipart 메시지 활성화 (HTML, 첨부파일)

            // helper.setFrom("your_email@example.com"); // 발신자 설정
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // 두 번째 인자를 true로 설정하여 HTML 내용임을 알림

            mailSender.send(message);
            log.info("HTML 이메일 전송 성공! To: {}, Subject: {}", to, subject);
        } catch (MessagingException | MailException e) {
            log.error("HTML 이메일 전송 실패! To: {}, Subject: {}, Error: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("HTML 이메일 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }
    */
}
package generationgap.co.kr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true) // ✅ 이 옵션 추가
@SpringBootApplication
@MapperScan("generationgap.co.kr.mapper") // ✅ 정확한 매퍼 위치 지정
public class GenprojectApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(GenprojectApplication.class, args);

		// 임시 코드: UserMapper 빈이 등록되었는지 확인
		try {
			generationgap.co.kr.mapper.user.UserMapper userMapperBean =
					ctx.getBean(generationgap.co.kr.mapper.user.UserMapper.class);
			System.out.println("✅ UserMapper 빈이 성공적으로 등록되었습니다: " + userMapperBean.getClass().getName());
		} catch (Exception e) {
			System.err.println("❌ UserMapper 빈을 찾을 수 없습니다: " + e.getMessage());
		}
	}

}

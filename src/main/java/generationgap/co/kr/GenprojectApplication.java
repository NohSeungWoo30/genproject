package generationgap.co.kr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("generationgap.co.kr.mapper") // 정확한 매퍼 위치 지정
@EnableScheduling
public class GenprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(GenprojectApplication.class, args);
	}

}

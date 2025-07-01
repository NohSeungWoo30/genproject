# GenProject 2nd project

# GenProject

Spring Boot + Thymeleaf + MyBatis + Oracle 연동 웹 프로젝트입니다.  
회원(User)과 그룹(Group) CRUD 예제를 포함하며 Thymeleaf 템플릿 엔진을 사용해 View를 처리합니다.

---

## Stack

- **Framework:** Spring Boot
- **View:** Thymeleaf
- **ORM:** MyBatis
- **DB:** Oracle XE
- **빌드:** Gradle (또는 Maven)

---

## DB 설정 (application.properties)

```properties
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=system
spring.datasource.password=12345

mybatis.mapper-locations=classpath:mapper/*.xml
```

## 실행 방법

- ./gradlew bootRun

---


## 접속 주소 

- http://localhost:8080/

---
package generationgap.co.kr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 요청 → C:/uploads/ 경로에서 파일 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/uploads/");

    }

    /*@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** URL로 요청이 오면, uploadPath 경로에서 파일을 찾아 웹에 노출
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + uploadPath);
    }*/
}

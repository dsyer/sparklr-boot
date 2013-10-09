package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;

@ComponentScan
@EnableAutoConfiguration
@ImportResource("classpath:/spring-servlet.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint() {
    	OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
    	entryPoint.setRealmName("sparklr2");
		return entryPoint;
    }

}

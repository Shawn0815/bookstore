package com.shawnyu.springbootmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 獨立成一個沒有其他依賴的 Configuration class，避免跟 SecurityConfig 形成循環依賴：
// SecurityConfig 依賴 JwtFilter -> UserService -> PasswordEncoder，
// 如果 PasswordEncoder 這個 @Bean 放在 SecurityConfig 裡面，會變成
// SecurityConfig 在建立過程中又需要等自己建立完成，造成 BeanCurrentlyInCreationException。
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

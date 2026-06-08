package com.example.my_admin_backend.config;

import com.example.my_admin_backend.entity.User;
import com.example.my_admin_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 如果没有任何用户，创建默认管理员账户
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .build();
            userRepository.save(admin);
            log.info("========================================");
            log.info("已创建默认管理员账户:");
            log.info("  用户名: admin");
            log.info("  密  码: admin123");
            log.info("  请登录后及时修改密码");
            log.info("========================================");
        }
    }
}
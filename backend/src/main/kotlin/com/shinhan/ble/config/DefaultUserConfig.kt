package com.shinhan.ble.config

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
class DefaultUserConfig {

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin: UserDetails = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .authorities("ROLE_ADMIN")
            .build()

        return InMemoryUserDetailsManager(admin)
    }
}
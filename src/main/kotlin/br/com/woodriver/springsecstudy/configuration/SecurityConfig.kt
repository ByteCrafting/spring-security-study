package br.com.woodriver.springsecstudy.configuration

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec
import javax.sql.DataSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    val exceptionHandlerFilter: ExceptionHandlerFilter
) {

    @Value("\${jwt.secret-key.private}")
    lateinit var privateSecretKeyValue: String

    @Bean
    fun createSecurityUsers(dataSource: DataSource): UserDetailsService {
        val manager = JdbcUserDetailsManager(dataSource)
        if (!manager.userExists("yan")) {
            manager.createUser(
                User.withUsername("yan")
                    .password(passwordEncoder().encode("123"))
                    .roles("ADMIN")
                    .build()
            )
        }
        return manager
    }

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.addFilterBefore(exceptionHandlerFilter, BearerTokenAuthenticationFilter::class.java)
        http.authorizeHttpRequests { requests ->
            requests
                .requestMatchers("/hello").permitAll()
                .requestMatchers("/api/signup").permitAll()
                .requestMatchers("/api/password/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/oauth2/token").permitAll()
                .anyRequest().authenticated()
        }
        http.csrf { csrf -> csrf.disable() }
        http.sessionManagement { session ->
            session.sessionCreationPolicy(STATELESS)
        }
        http.oauth2ResourceServer { it.jwt {  }  }
        return http.build()
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        authenticationConfiguration: AuthenticationConfiguration
    ): AuthenticationManager = authenticationConfiguration.authenticationManager

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secretKey = SecretKeySpec(privateSecretKeyValue.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secretKey).build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val hmacKey = OctetSequenceKey.Builder(privateSecretKeyValue.toByteArray())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.HS256)
            .keyID("my-key-id") // Provide a unique key ID
            .build()

        val jwkSet = JWKSet(hmacKey)
        return NimbusJwtEncoder(ImmutableJWKSet(jwkSet))
    }
}

package br.com.woodriver.springsecstudy.controller

import br.com.woodriver.springsecstudy.controller.request.PasswordRequest
import br.com.woodriver.springsecstudy.controller.request.SignupRequest
import br.com.woodriver.springsecstudy.controller.response.TokenResponse
import br.com.woodriver.springsecstudy.repository.AuthoritiesRepository
import br.com.woodriver.springsecstudy.repository.UserRepository
import br.com.woodriver.springsecstudy.repository.entity.AuthoritiesEntity
import br.com.woodriver.springsecstudy.repository.entity.UserEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
class AuthController(
    val userRepository: UserRepository,
    val authoritiesRepository: AuthoritiesRepository,
    val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    val jwtEncoder: JwtEncoder
) {

    @PostMapping("/api/signup")
    fun signup(@RequestBody signupRequest: SignupRequest): ResponseEntity<String> {
        if (userRepository.findByUsername(signupRequest.username) != null) {
            return ResponseEntity.badRequest().body("Username already exists")
        }

        val user = UserEntity(
            username = signupRequest.username,
            password = passwordEncoder.encode(signupRequest.password),
            email = signupRequest.email
        )
        val authority = AuthoritiesEntity(
            username = signupRequest.username,
            authority = "ROLE_USER"
        )
        userRepository.save(user)
        authoritiesRepository.save(authority)
        return ResponseEntity("User registered successfully", HttpStatus.CREATED)
    }

    @PostMapping("/api/password/{username}")
    fun changePassword(@PathVariable username: String, @RequestBody passwordRequest: PasswordRequest): ResponseEntity<String> {
        val user = userRepository.findByUsername(username)

        user?.let {
             it.password = passwordEncoder.encode(passwordRequest.value)
            userRepository.save(it)
        }


        return ResponseEntity("Password updated successfully", HttpStatus.CREATED)
    }

    @PostMapping(
        path = ["/oauth2/token"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun getToken(@RequestParam paramMap: MultiValueMap<String, String>): ResponseEntity<TokenResponse> {
        val username = paramMap["username"]?.get(0)
        val password = paramMap["password"]?.get(0)
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )

        val user = userRepository.findByUsername(username.toString())
        val authority = authoritiesRepository.findById(username.toString())

        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(authentication.name)
            .claim("scope", authority.get().authority)
            .claim("email", user?.email)
            .build()

        val headers = JwsHeader.with(MacAlgorithm.HS256).build()
        val parameters = JwtEncoderParameters.from(headers, claims)

        val token = jwtEncoder.encode(parameters).tokenValue

        return ResponseEntity.ok(TokenResponse(
            accessToken = token,
            expiresIn = ChronoUnit.HOURS.duration.toSeconds(),
            refreshToken = token,
            scope = authority.get().authority,
            tokenType = "Bearer"
        ))
    }
}

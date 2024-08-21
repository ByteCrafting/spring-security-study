package br.com.woodriver.springsecstudy.service

import br.com.woodriver.springsecstudy.repository.UserRepository
import br.com.woodriver.springsecstudy.repository.entity.UserEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class TokenService(
    val userRepository: UserRepository
) {

    fun getUserUsingToken(): UserEntity{
        val token = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        return userRepository.findByUsername(token.name)!!
    }
}

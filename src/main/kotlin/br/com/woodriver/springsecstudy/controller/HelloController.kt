package br.com.woodriver.springsecstudy.controller

import br.com.woodriver.springsecstudy.service.TokenService
import br.com.woodriver.springsecstudy.utils.objectToJson
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    val tokenService: TokenService
) {

    @GetMapping("/helloByToken")
    fun hello(): ResponseEntity<String> {
        val user = tokenService.getUserUsingToken()
        return ResponseEntity.ok("Hello ${user.username}")
    }
}

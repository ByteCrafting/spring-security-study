package br.com.woodriver.springsecstudy.repository.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.logging.log4j.util.Strings.EMPTY

@Entity
@Table(name = "authorities")
data class AuthoritiesEntity(
    @Id
    val username: String = EMPTY,
    val authority: String = EMPTY,
)

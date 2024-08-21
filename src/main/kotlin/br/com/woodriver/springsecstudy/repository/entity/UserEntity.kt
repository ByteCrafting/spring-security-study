package br.com.woodriver.springsecstudy.repository.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.logging.log4j.util.Strings.EMPTY

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = Long.MIN_VALUE,
    @Column(unique = true)
    val username: String = EMPTY,
    var password: String = EMPTY,
    @Column(unique = true)
    val email: String = EMPTY,
    val enabled: Boolean = true,
)

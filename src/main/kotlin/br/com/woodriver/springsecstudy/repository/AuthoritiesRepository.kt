package br.com.woodriver.springsecstudy.repository

import br.com.woodriver.springsecstudy.repository.entity.AuthoritiesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthoritiesRepository : JpaRepository<AuthoritiesEntity, String> {
}

package org.xr4good.datasetarchive.repository

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository
import org.xr4good.datasetarchive.entity.User


@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean
}
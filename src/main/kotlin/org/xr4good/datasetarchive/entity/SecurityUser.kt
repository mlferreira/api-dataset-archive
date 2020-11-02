package org.xr4good.datasetarchive.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class SecurityUser(val id: Long,
                   private val username: String = "",
                   val email: String = "",
                   @JsonIgnore private val password: String = "",
                   private val authorities: Collection<GrantedAuthority> = mutableSetOf()
): UserDetails {

        constructor(
                user: User
        ): this(
                user.id!!,
                user.username,
                user.email,
                user.password,
                user.roles.map{role -> SimpleGrantedAuthority(role.name)}
        )

        fun toUser() = User(this)


        override fun getAuthorities(): Collection<GrantedAuthority> = authorities
        override fun getPassword() = password
        override fun getUsername() = username


        override fun isAccountNonExpired() = true
        override fun isAccountNonLocked() = true
        override fun isCredentialsNonExpired() = true
        override fun isEnabled() = true


        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || javaClass != other.javaClass) return false
                val user = other as SecurityUser
                return id == user.id
        }

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + username.hashCode()
                result = 31 * result + email.hashCode()
                result = 31 * result + password.hashCode()
                result = 31 * result + authorities.hashCode()
                return result
        }
}
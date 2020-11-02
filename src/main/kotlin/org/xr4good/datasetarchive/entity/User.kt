package org.xr4good.datasetarchive.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


@Entity
@Table(	name = "users",
        uniqueConstraints = [
                UniqueConstraint(columnNames = ["username"]),
                UniqueConstraint(columnNames = ["email"])
        ])
class User (
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @NotBlank
        @Size(max = 50)
        var username: String,

        @NotBlank
        @Size(max = 50)
        @Email
        var email: String,

        @NotBlank
        @Size(max = 50)
        @JsonIgnore
        var password: String,

        @ElementCollection
        @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
        @Column(name = "role", nullable = false)
        @Enumerated(EnumType.STRING)
        var roles: MutableSet<RoleEnum> = mutableSetOf()
) {
        constructor(
                securityUser: SecurityUser
        ): this(
                securityUser.id,
                securityUser.username,
                securityUser.email,
                securityUser.password,
                securityUser.authorities.map{a -> RoleEnum.valueOf(a.authority) }.toSet() as MutableSet<RoleEnum>
        )

        fun toSecurityUser() = SecurityUser(this)
}

enum class RoleEnum {
        ROLE_USER, ROLE_ADMIN
}
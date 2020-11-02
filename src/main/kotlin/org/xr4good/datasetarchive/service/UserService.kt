package org.xr4good.datasetarchive.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.xr4good.datasetarchive.entity.RoleEnum
import org.xr4good.datasetarchive.entity.User
import org.xr4good.datasetarchive.entity.SecurityUser
import org.xr4good.datasetarchive.exception.BadRequestException
import org.xr4good.datasetarchive.exception.NotFoundException
import org.xr4good.datasetarchive.repository.UserRepository
import org.xr4good.datasetarchive.vo.SignupRequest


@Service
class UserService (
        @Autowired var encoder: PasswordEncoder,
        @Autowired val userRepository: UserRepository
) {

    fun getById(id: Long) = userRepository.findById(id).orElseThrow{NotFoundException()}

    fun me() = User(SecurityContextHolder.getContext().authentication.principal as SecurityUser)

    fun create(signUpRequest: SignupRequest): User {
        if (userRepository.existsByUsername(signUpRequest.username))
            throw BadRequestException("Error: Username is already taken!")
        if (userRepository.existsByEmail(signUpRequest.email))
            throw BadRequestException("Error: Email is already in use!")

        var user = User(null,
                signUpRequest.username,
                signUpRequest.email,
                encoder.encode(signUpRequest.password)
                ,mutableSetOf(RoleEnum.ROLE_USER)
        )

        user = userRepository.save(user)

        return user
    }

}

@Service
class UserSecurityService (
        @Autowired val userRepository: UserRepository
): UserDetailsService {


    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepository.findByUsername(username)
                ?: throw NotFoundException("User Not Found with username: $username")
        return SecurityUser(user)
    }




}

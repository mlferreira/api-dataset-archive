package org.xr4good.datasetarchive.controller

import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.xr4good.datasetarchive.configuration.JwtUtils
import org.xr4good.datasetarchive.entity.User
import org.xr4good.datasetarchive.entity.SecurityUser
import org.xr4good.datasetarchive.service.UserService
import org.xr4good.datasetarchive.vo.*
import java.util.stream.Collectors
import javax.validation.Valid


@CrossOrigin(origins = ["*"])
@Api("User CRUD")
@RestController
@RequestMapping("/api/user")
class UserController (
    @Autowired var authenticationManager: AuthenticationManager,
    @Autowired var userService: UserService,
    @Autowired var jwtUtils: JwtUtils
) : ControllerExceptionHandler() {

    @CrossOrigin
    @ApiOperation("Login with existent account")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns the authentication token."),
        ApiResponse(code=403, message="Invalid username/password combination.")])
    @PostMapping("/login")
    fun authenticateUser(
            @RequestBody loginRequest: @Valid LoginRequest
    ): ResponseEntity<JwtResponse> {
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as SecurityUser
        val roles = userDetails.authorities.stream()
                .map { item: GrantedAuthority -> item.authority }
                .collect(Collectors.toList())
        return ResponseEntity.ok(JwtResponse(jwt,
                userDetails.id,
                userDetails.username,
                userDetails.email,
                roles))
    }

    @CrossOrigin
    @ApiOperation("Create new account")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Account created successfully."),
        ApiResponse(code=403, message="Username/email already registered.")])
    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: @Valid SignupRequest): ResponseEntity<User>
        = ResponseEntity.ok(userService.create(signUpRequest))

    @CrossOrigin
    @ApiOperation("Get logged user")
    @ApiResponses(value = [
        ApiResponse(code=200, message="Returns logged user information."),
        ApiResponse(code=401, message="Unauthenticated.")])
    @GetMapping("/me")
    fun me() : ResponseEntity<User>  = ResponseEntity.ok(userService.me())

    @ApiOperation("Get user by ID")
    @ApiImplicitParam(name="id", value="User's id", required=true, dataType="String", paramType="path", example="2")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) : ResponseEntity<User> {
        return ResponseEntity.ok(userService.getById(id))
    }

}


package org.xr4good.datasetarchive.vo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel(value = "Login Form")
class LoginRequest (
        @ApiModelProperty(required = true, example = "username", position = 0)
        var username: String,
        @ApiModelProperty(required = true, example = "password", position = 1)
        var password: String
)

@ApiModel(value = "Registration Form")
class SignupRequest (
    @ApiModelProperty(required = true, example = "username", position = 0)
    var username: String,
    @ApiModelProperty(required = true, example = "test@email.com", position = 1)
    var email: String,
    @ApiModelProperty(required = true, example = "password", position = 2)
    var password: String
)
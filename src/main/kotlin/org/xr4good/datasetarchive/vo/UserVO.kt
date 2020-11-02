package org.xr4good.datasetarchive.vo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Size


@ApiModel(value = "User")
data class UserVO(
        @ApiModelProperty(value = "User's ID", example = "10", position = 0, dataType = "String", required = true)
        val id: Long,
        @ApiModelProperty(value = "User's Name", example = "Arthur Fleck", position = 1, dataType = "String", required = false)
        val name: String?,
        @ApiModelProperty(value = "User's Email", example = "joker@wayneenterprises.com", position = 2, dataType = "String", required = false)
        val email: String?,
        @ApiModelProperty(value = "User's Roles", example = "[\"r:user\",\"r:admin\"]", dataType = "List", position = 3, required = false)
        val roles: List<String>? = null
)


class UserDto (
        @Size(min = 1, message = "{Size.userDto.firstName}")
        var firstName: String? = null,

        @Size(min = 1, message = "{Size.userDto.lastName}")
        var lastName: String? = null,

        var password: String? = null,

        @Size(min = 1)
        var matchingPassword: String? = null,

        @Size(min = 1, message = "{Size.userDto.email}")
        var email: String? = null,

        var isUsing2FA: Boolean = false,

        var role: Int? = null
) {

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("UserDto [firstName=").append(firstName).append(", lastName=").append(lastName).append(", password=").append(password).append(", matchingPassword=").append(matchingPassword).append(", email=").append(email).append(", isUsing2FA=")
                .append(isUsing2FA).append(", role=").append(role).append("]")
        return builder.toString()
    }
}

class PasswordDto (
        var oldPassword: String,
        var token: String,
        var newPassword: String
)
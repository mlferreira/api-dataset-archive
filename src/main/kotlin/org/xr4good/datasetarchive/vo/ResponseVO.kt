package org.xr4good.datasetarchive.vo

class JwtResponse (
        var token: String,
        var id: Long,
        var username: String,
        var email: String,
        val roles: MutableList<String>,
        var type: String = "Bearer"
) {

    fun getAccessToken() = token;

    fun setAccessToken(accessToken: String) {
        token = accessToken
    }

    fun getTokenType() = type

    fun setTokenType(tokenType: String) {
        this.type = tokenType;
    }

}

class MessageResponse(var message: String)
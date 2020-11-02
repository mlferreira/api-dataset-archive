package org.xr4good.datasetarchive.exception

class NotFoundException(message: String? = null, t: Throwable? = null) : RuntimeException(message, t) {
    constructor(resourceType: String, resourceId: Long) : this("$resourceType [ $resourceId ] not found.", null)
}

class BadRequestException(message : String? = null, t : Throwable? = null) : RuntimeException(message, t)

class ServerErrorException(message : String? = null, t : Throwable? = null) : RuntimeException(message, t)

class CouldNotCreateSecurityACL(message : String?, t : Throwable?)  : RuntimeException(message, t) {
}
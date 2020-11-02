package org.xr4good.datasetarchive.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.ExceptionHandler
import org.xr4good.datasetarchive.exception.BadRequestException
import org.xr4good.datasetarchive.exception.CouldNotCreateSecurityACL
import org.xr4good.datasetarchive.exception.NotFoundException
import org.xr4good.datasetarchive.exception.ServerErrorException


@CrossOrigin(origins = ["*"])
open class ControllerExceptionHandler {

    @ExceptionHandler(value = [(NotFoundException::class)])
    fun handleNotFoundException(ex: NotFoundException) : ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [(BadRequestException::class)])
    fun handleBadRequestException(ex: BadRequestException) : ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [(CouldNotCreateSecurityACL::class)])
    fun handleACLException(ex: CouldNotCreateSecurityACL) : ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [(ServerErrorException::class)])
    fun handleServerErrorException(ex: ServerErrorException) : ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [(Exception::class)])
    fun genericExceptionHandler(ex: java.lang.Exception) : ResponseEntity<String?> {
        return ResponseEntity(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

}



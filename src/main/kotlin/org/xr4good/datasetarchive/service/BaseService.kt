package org.xr4good.datasetarchive.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.xr4good.datasetarchive.entity.SecurityUser


@Service
class BaseService {

    @Autowired lateinit var aclService: AclService

    fun getLoggedUser(): SecurityUser = SecurityContextHolder.getContext().authentication.principal as SecurityUser

}
package org.xr4good.datasetarchive.configuration

import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

@Suppress("unused", "PropertyName")
class CustomPermission(mask: Int, code: Char): BasePermission(mask, code) {
    companion object {
        val PUBLISH = CustomPermission(1 shl 5, 'P') // 32
        // next custom permission mask: 1 shl 6, next 1 shl 7 ...

        fun allPermissions() : Map<String, Permission> = mapOf(
                "READ" to READ, // 1
                "WRITE" to WRITE, // 2
                "CREATE" to CREATE, // 4
                "DELETE" to DELETE, // 8
                "PUBLISH" to PUBLISH // 32
        )
    }
}
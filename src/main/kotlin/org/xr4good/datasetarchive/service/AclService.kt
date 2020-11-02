package org.xr4good.datasetarchive.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.acls.domain.*
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.*
import org.xr4good.datasetarchive.configuration.CustomPermission
import org.xr4good.datasetarchive.entity.*
import javax.sql.DataSource


class AclService(
        private val dataSource: DataSource,
        private val lookupStrategy: LookupStrategy,
        private val aclCache: AclCache,
        @Value("\${spring.security.oauth2.resourceserver.jwt.authority-prefix}") private val authorityPrefix: String,
        @Value("\${spring.security.oauth2.custom.access-authority}") private val accessAuthority: String,
        @Value("\${spring.security.oauth2.custom.main-authority}") private val mainAuthority: String
):  JdbcMutableAclService(dataSource, lookupStrategy, aclCache) {

    val publicRoleName = authorityPrefix + accessAuthority
    val adminRoleName = authorityPrefix + mainAuthority

    fun createAcl(entry: Entry) {
        val acl = createAcl(ObjectIdentityImpl(entry::class.qualifiedName, entry.id))
        addAdminAccess(acl)
        if (entry.datasetId != null && entry.status == StatusEnum.PENDING) attachEntryToDataset(entry)
    }

    fun createAcl(dataset: Dataset) {
        val acl = createAcl(ObjectIdentityImpl(dataset::class.qualifiedName, dataset.id))
        if (dataset.status == StatusEnum.OPEN) {
            acl.insertAce(acl.entries.size, BasePermission.READ, GrantedAuthoritySid(publicRoleName), true)
            acl.insertAce(acl.entries.size, BasePermission.CREATE, GrantedAuthoritySid(publicRoleName), true)
        } else if (dataset.status == StatusEnum.CLOSED) {
            acl.insertAce(acl.entries.size, BasePermission.READ, GrantedAuthoritySid(publicRoleName), true)
        }
        addAdminAccess(acl)
    }

    fun updateAcl(entry: Entry) {
        if (entry.datasetId != null && entry.status == StatusEnum.PENDING)
            attachEntryToDataset(entry)
        else if (entry.datasetId == null || entry.status == StatusEnum.DRAFT || entry.status == StatusEnum.REJECTED)
            unattachEntryToDataset(entry)
    }

    fun updateAcl(dataset: Dataset) {
        val acl = readAclById(ObjectIdentityImpl(Dataset::class.qualifiedName, dataset.id)) as MutableAcl

        acl.entries.removeIf{ ace ->
            ace.sid is GrantedAuthoritySid && (ace.sid as GrantedAuthoritySid).grantedAuthority == publicRoleName
        }

        if (dataset.status == StatusEnum.OPEN) {
            acl.insertAce(acl.entries.size, BasePermission.READ, GrantedAuthoritySid(publicRoleName), true)
            acl.insertAce(acl.entries.size, BasePermission.CREATE, GrantedAuthoritySid(publicRoleName), true)
        } else if (dataset.status == StatusEnum.CLOSED) {
            acl.insertAce(acl.entries.size, BasePermission.READ, GrantedAuthoritySid(publicRoleName), true)
        }

        updateAcl(acl)
        aclCache.putInCache(acl)
    }

    fun attachEntryToDataset(entry: Entry) {
        val acl = readAclById(ObjectIdentityImpl(Entry::class.qualifiedName, entry.id)) as MutableAcl
        aclCache.evictFromCache(acl.id)
        val parentAcl = readAclById(ObjectIdentityImpl(Dataset::class.qualifiedName, entry.datasetId))
        acl.setParent(parentAcl)
        updateObjectIdentity(acl)
        updateAcl(acl)
        aclCache.putInCache(acl)
    }

    fun unattachEntryToDataset(entry: Entry) {
        val acl = readAclById(ObjectIdentityImpl(Entry::class.qualifiedName, entry.id)) as MutableAcl
        aclCache.evictFromCache(acl.id)
        acl.setParent(null)
        updateObjectIdentity(acl)
        updateAcl(acl)
        aclCache.putInCache(acl)
    }

    private fun addAdminAccess(acl: MutableAcl) {
        CustomPermission.allPermissions().forEach { s, p ->
            acl.insertAce(
                    acl.entries.size,
                    p,
                    GrantedAuthoritySid(adminRoleName),
                    true
            )
            acl.insertAce(
                    acl.entries.size,
                    p,
                    acl.owner,
                    true
            )
        }
        updateAcl(acl)
        aclCache.putInCache(acl)
    }

}

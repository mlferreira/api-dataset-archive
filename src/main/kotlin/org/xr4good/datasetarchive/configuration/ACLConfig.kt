package org.xr4good.datasetarchive.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.acls.domain.*
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.*
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.xr4good.datasetarchive.exception.NotFoundException
import org.xr4good.datasetarchive.service.AclService
import java.io.Serializable
import java.time.Duration
import javax.sql.DataSource
import kotlin.reflect.full.memberProperties


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class AclMethodSecurityConfiguration(
        @Autowired val dataSource: DataSource
        ,@Value("\${redis.name}") private val cacheName: String
        ,@Value("\${redis.host}") private val cacheHost: String
        ,@Value("\${redis.port}") private val cachePort: Int
        ,@Value("\${spring.security.oauth2.resourceserver.jwt.authority-prefix}") private val authorityPrefix: String
        ,@Value("\${spring.security.oauth2.custom.main-authority}") private val mainAuthority: String
        ,@Value("\${spring.security.oauth2.custom.access-authority}") private val accessAuthority: String
) : GlobalMethodSecurityConfiguration() {

    @Bean
    @Primary
    fun defaultMethodSecurityExpressionHandler() : MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        val aclPermissionEvaluator = AclPermissionEvaluator(aclService())
        aclPermissionEvaluator.setPermissionFactory(permissionFactory())
        aclPermissionEvaluator.setObjectIdentityGenerator(CustomObjectIdentityGenerator())
        aclPermissionEvaluator.setObjectIdentityRetrievalStrategy(CustomObjectIdentityGenerator())
        expressionHandler.setPermissionEvaluator(aclPermissionEvaluator)
        return expressionHandler
    }

    @Bean
    @Primary
    fun aclService(): AclService {
        val aclService = AclService(dataSource, lookupStrategy(), aclCache(), authorityPrefix, accessAuthority, mainAuthority)
        aclService.setClassIdentityQuery("SELECT @@IDENTITY")
        aclService.setSidIdentityQuery("SELECT @@IDENTITY")
        return aclService
    }

    @Bean
    @Primary
    fun aclAuthorizationStrategy() : AclAuthorizationStrategy = AclAuthorizationStrategyImpl(SimpleGrantedAuthority(authorityPrefix + mainAuthority))

    @Bean
    @Primary
    fun permissionGrantingStrategy() : PermissionGrantingStrategy = DefaultPermissionGrantingStrategy(ConsoleAuditLogger())

    @Bean
    @Primary
    fun permissionFactory() : PermissionFactory = DefaultPermissionFactory(CustomPermission.allPermissions())

    @Bean
    @Primary
    fun aclCache(): AclCache {
        return SpringCacheBasedAclCache(cacheManager().getCache(cacheName), permissionGrantingStrategy(), aclAuthorizationStrategy())
    }

    @Bean
    @Primary
    fun cacheManager(): CacheManager {
        return RedisCacheManager.builder(redisConnectionFactory())
//                .initialCacheNames(mutableSetOf(cacheName))
                .build()
    }

    @Bean
    @Primary
    fun redisConnectionFactory() : RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(cacheHost)

        val jedisConnectionFactory = JedisConnectionFactory(redisStandaloneConfiguration, JedisClientConfiguration
                .builder()
                .connectTimeout(Duration.ofSeconds(120))
                .build())
        jedisConnectionFactory.afterPropertiesSet()
        return jedisConnectionFactory
    }

    @Bean
    @Primary
    fun lookupStrategy(): LookupStrategy {
        val bls = BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), permissionGrantingStrategy())
        bls.setPermissionFactory(permissionFactory())
        return bls
    }

}


class CustomObjectIdentityGenerator: ObjectIdentityRetrievalStrategy, ObjectIdentityGenerator {
    fun createObjectIdentity(id: Long, type: String): ObjectIdentity = ObjectIdentityImpl(type, id)
    fun createObjectIdentity(id: String, type: String): ObjectIdentity = ObjectIdentityImpl(type, id)
    override fun createObjectIdentity(id: Serializable, type: String): ObjectIdentity = ObjectIdentityImpl(type, id)
    fun createObjectIdentity(any: Any?): ObjectIdentity = getObjectIdentity(any)

    override fun getObjectIdentity(any: Any?): ObjectIdentity {
        if (any == null) throw NotFoundException()
        val id = any::class.members.find { it.name == "id" }?.call(any) ?:
            any::class.memberProperties.find { it.name == "id" }?.call(any)
        return ObjectIdentityImpl(any::class.qualifiedName!!, id.toString())
    }
}



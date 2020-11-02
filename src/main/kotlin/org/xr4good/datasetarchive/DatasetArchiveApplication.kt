package org.xr4good.datasetarchive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.context.request.RequestContextListener


@SpringBootApplication(
        scanBasePackages = [ "org.xr4good.datasetarchive" ],
        exclude = [
            SecurityAutoConfiguration::class,
            RedisRepositoriesAutoConfiguration::class
        ]
)
@EnableJpaRepositories
@EntityScan("org.xr4good.datasetarchive")
class DatasetArchiveApplication {

    @Bean
    fun requestContextListener(): RequestContextListener = RequestContextListener()

}

fun main(args: Array<String>) {
    runApplication<DatasetArchiveApplication>(*args)
}

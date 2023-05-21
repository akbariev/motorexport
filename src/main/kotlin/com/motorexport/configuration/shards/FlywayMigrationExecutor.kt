package com.motorexport.configuration.shards

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.PostMapping

/*
@Configuration
@ConditionalOnProperty("db-configuration", havingValue = "flyway", matchIfMissing = false)
*/
class FlywayMigrationExecutor(private val shardConfigurationHolder: ShardConfigurationHolder) {
    @PostMapping
    fun executeMigration() {
        for (i in 1..shardConfigurationHolder.getMaxShards()) {
            val flyway = Flyway.configure()
                .locations("classpath:db/migration")
                .dataSource(
                    shardConfigurationHolder.getUrl(i, true),
                    shardConfigurationHolder.getUserName(i),
                    shardConfigurationHolder.getPassword(i)
                )
                .load()
            flyway.migrate()
        }
    }
}
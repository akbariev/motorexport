package com.motorexport.configuration.shards

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration

//@Configuration
class R2dbcWithShardsConfiguration(
    private val shardConfigurationHolder: ShardConfigurationHolder
) : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val routingConnectionFactory = R2dbcRoutingConnectionFactory(shardConfigurationHolder)
        routingConnectionFactory.setTargetConnectionFactories(
            (1..shardConfigurationHolder.getMaxShards()).associateWith { ::createConnectionPool }
        )
        return routingConnectionFactory
    }

    private fun createConnectionPool(shardNumber: Int) = PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(shardConfigurationHolder.getHost(shardNumber))
            .port(shardConfigurationHolder.getPort(shardNumber))
            .database(shardConfigurationHolder.getDatabaseName(shardNumber))
            .username(shardConfigurationHolder.getUserName(shardNumber))
            .password(shardConfigurationHolder.getPassword(shardNumber))
            .preparedStatementCacheQueries(0)
            .build()
    )
}


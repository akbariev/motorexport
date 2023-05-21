package com.motorexport.configuration.shards

import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono

class R2dbcRoutingConnectionFactory(private val shardConfigurationHolder: ShardConfigurationHolder) :
    AbstractRoutingConnectionFactory() {
    override fun determineCurrentLookupKey(): Mono<Any> {
        return Mono.deferContextual { ct ->
            if (ct.hasKey("USER_ID")) {
                Mono.just(shardConfigurationHolder.getShardByAccountId(ct.get("USER_ID")))
            } else {
                Mono.empty()
            }
        }
    }
}
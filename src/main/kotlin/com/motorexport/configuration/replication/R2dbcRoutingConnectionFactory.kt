package com.motorexport.configuration.replication

import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import reactor.core.publisher.Mono

class R2dbcRoutingConnectionFactory : AbstractRoutingConnectionFactory() {
    override fun determineCurrentLookupKey(): Mono<Any> {
        return Mono.deferContextual { ct ->
            val isReadOnly: Boolean = ct.getOrDefault(READ_ONLY_TX_KEY, false) ?: false
            val database = if (isReadOnly) DATABASE.REPLICA else DATABASE.MASTER
            Mono.just(database)
        }
    }
}
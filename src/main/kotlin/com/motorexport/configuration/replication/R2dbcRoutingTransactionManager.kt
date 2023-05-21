package com.motorexport.configuration.replication

import io.r2dbc.spi.ConnectionFactory
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono

class R2dbcRoutingTransactionManager(connectionFactory: ConnectionFactory) :
    R2dbcTransactionManager(connectionFactory) {
    override fun doBegin(
        synchronizationManager: TransactionSynchronizationManager,
        transaction: Any,
        definition: TransactionDefinition
    ): Mono<Void> {
        val mono = super.doBegin(synchronizationManager, transaction, definition)
        return mono.contextWrite { ct -> ct.put(READ_ONLY_TX_KEY, definition.isReadOnly) }
    }
}
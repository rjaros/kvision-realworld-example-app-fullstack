package io.realworld.utils

import io.r2dbc.spi.ConnectionFactory
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.mapping.SettableValue
import org.springframework.transaction.ReactiveTransaction
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

suspend fun <T : Any> withTransaction(
    connectionFactory: ConnectionFactory,
    block: suspend (ReactiveTransaction) -> T?
): T? {
    val tm = R2dbcTransactionManager(connectionFactory)
    return TransactionalOperator.create(tm).executeAndAwait(block)
}

fun DatabaseClient.GenericExecuteSpec.bindMap(parameters: Map<String, Any?>): DatabaseClient.GenericExecuteSpec {
    return parameters.entries.fold(this) { spec, entry ->
        if (entry.value == null) {
            spec.bindNull(entry.key, String::class.java)
        } else {
            spec.bind(entry.key, SettableValue.fromOrEmpty(entry.value, entry.value!!::class.java))
        }
    }
}

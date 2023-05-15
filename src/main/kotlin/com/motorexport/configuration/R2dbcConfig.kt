package com.motorexport.configuration

import com.motorexport.controller.dto.BodyTypeGroup
import com.motorexport.controller.dto.EngineGroup
import com.motorexport.controller.dto.GearType
import com.motorexport.controller.dto.InStock
import com.motorexport.controller.dto.Transmission
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.spi.ConnectionFactoryOptions
import java.lang.reflect.ParameterizedType
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions.*
import org.springframework.data.r2dbc.dialect.DialectResolver

@Configuration
class R2dbcConfig(private val properties: R2dbcProperties) {

    /**
     * Postgresql enum types map, where:
     *
     * key - postgres enum type name
     * value - kotlin enum class
     */
    val customEnumTypes = listOf(
        // register enums here
        pgEnum<EngineGroup>("engine_group"),
        pgEnum<GearType>("gear_type"),
        pgEnum<Transmission>("transmission"),
        pgEnum<BodyTypeGroup>("body_type_group"),
        pgEnum<InStock>("in_stock"),
    )

    @Bean(destroyMethod = "dispose")
    fun connectionPool(): ConnectionPool {
        // ConnectionFactoryOptions это класс, что просто содержит конфигурацию
        val options: ConnectionFactoryOptions = io.r2dbc.spi.ConnectionFactoryOptions
            .parse(properties.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, properties.username)
            .option(ConnectionFactoryOptions.PASSWORD, properties.password)
            .build()
        // Еще есть один конфигурационный класс, у которого мы отключаем кэш запросов и присваиваем codec
        val configuration: PostgresqlConnectionConfiguration = PostgresqlConnectionFactoryProvider.builder(options)
            // pgBouncer has issues with prepared statements - https://github.com/pgjdbc/r2dbc-postgresql/issues/223
            .preparedStatementCacheQueries(0)
            .apply {
                // Only register enum codec when enum types is not empty.
                // Otherwise, r2dbc fails to connect with syntax error: empty brackets ("... IN ()")
                if (customEnumTypes.isNotEmpty()) {
                    val builder = EnumCodec.builder()
                    customEnumTypes.forEach {
                        val type = (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                        builder.withEnum(it.pgEnumName, type as Class<out Enum<*>>)
                    }
                    codecRegistrar(builder.build())
                }
            }
            .build()

        val builder = ConnectionPoolConfiguration.builder(PostgresqlConnectionFactory(configuration))
        builder.maxIdleTime(properties.pool.maxIdleTime) // максимальное время idle коннекшена
        properties.pool.maxLifeTime?.let { builder.maxLifeTime(it) } // максимальное время жизни коннекшена
        properties.pool.maxAcquireTime?.let { builder.maxAcquireTime(it) } // время ожидания получения коннекшена из пула
        properties.pool.maxCreateConnectionTime?.let { builder.maxCreateConnectionTime(it) } // максимальное время создания коннкешена к бд
        builder.initialSize(properties.pool.initialSize) // изначальное число коннекшенов в пуле
        builder.maxSize(properties.pool.maxSize) // максимальный размер коннекшенов в пуле
        properties.pool.validationQuery?.let { builder.validationQuery(it) } // пингует бд для проверки, что коннекшен живой. пример: SELECT 1
        builder.validationDepth(properties.pool.validationDepth) // странный параметр
        // и все, кладем этот ConnectionPoolConfiguration в ConnectionPool.
        return builder.build().let { ConnectionPool(it) }
    }

    // этот бин помогает в маппинге енумок котлина в постргес енумки
    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions = of(DialectResolver.getDialect(connectionPool()), customEnumTypes)

    private inline fun <reified E : Enum<E>> pgEnum(pgEnumName: String) = object : PgEnumSupport<E>(pgEnumName) {}

    abstract class PgEnumSupport<E : Enum<E>>(val pgEnumName: String) : EnumWriteSupport<E>()
}
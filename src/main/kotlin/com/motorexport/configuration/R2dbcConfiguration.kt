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
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import java.lang.reflect.ParameterizedType
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver

@Configuration
class R2dbcConfiguration(private val r2dbcProperties: R2dbcProperties) : AbstractR2dbcConfiguration() {

    val r2dbcEnums = listOf(
        r2dbcEnum<EngineGroup>("engine_group"),
        r2dbcEnum<GearType>("gear_type"),
        r2dbcEnum<Transmission>("transmission"),
        r2dbcEnum<BodyTypeGroup>("body_type_group"),
        r2dbcEnum<InStock>("in_stock"),
    )

    private inline fun <reified E : Enum<E>> r2dbcEnum(enumType: String) = object : R2dbcEnumConvertor<E>(enumType) {}

    abstract class R2dbcEnumConvertor<E : Enum<E>>(val enumType: String) : EnumWriteSupport<E>()

    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory()), r2dbcEnums)
    }

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val connectionFactoryOptions = ConnectionFactoryOptions
            .parse(r2dbcProperties.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, r2dbcProperties.username)
            .option(ConnectionFactoryOptions.PASSWORD, r2dbcProperties.password)
            .build()

        val connectionConfiguration: PostgresqlConnectionConfiguration = PostgresqlConnectionFactoryProvider
            .builder(connectionFactoryOptions)
            .preparedStatementCacheQueries(0)
            .apply {
                if (r2dbcEnums.isNotEmpty()) {
                    val enumCodecBuilder = EnumCodec.builder()
                    r2dbcEnums.forEach {
                        val argumentEnumType =
                            (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                        enumCodecBuilder.withEnum(it.enumType, argumentEnumType as Class<out Enum<*>>)
                    }
                    codecRegistrar(enumCodecBuilder.build())
                }
            }.build()
        val connectionPoolConfiguration =
            ConnectionPoolConfiguration.builder(PostgresqlConnectionFactory(connectionConfiguration))
        r2dbcProperties.pool.maxIdleTime?.let { connectionPoolConfiguration.maxIdleTime(it) }
        r2dbcProperties.pool.maxLifeTime?.let { connectionPoolConfiguration.maxLifeTime(it) }
        r2dbcProperties.pool.maxAcquireTime?.let { connectionPoolConfiguration.maxAcquireTime(it) }
        r2dbcProperties.pool.maxCreateConnectionTime?.let { connectionPoolConfiguration.maxCreateConnectionTime(it) }
        r2dbcProperties.pool.initialSize.let { connectionPoolConfiguration.initialSize(it) }
        r2dbcProperties.pool.maxSize.let { connectionPoolConfiguration.maxSize(it) }
        r2dbcProperties.pool.validationQuery?.let { connectionPoolConfiguration.validationQuery(it) }
        r2dbcProperties.pool.validationDepth?.let { connectionPoolConfiguration.validationDepth(it) }
        return ConnectionPool(connectionPoolConfiguration.build())
    }
}

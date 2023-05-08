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
import io.r2dbc.postgresql.extension.CodecRegistrar
import io.r2dbc.spi.ConnectionFactoryOptions
import java.lang.reflect.ParameterizedType
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver

/*todo
    NEED TO DO MYSELF because is copy from Ali
    */
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

        val urlOptions = ConnectionFactoryOptions.parse(properties.url)
        val optionsBuilder = urlOptions.mutate()
            .option(ConnectionFactoryOptions.USER, properties.username)
            .option(ConnectionFactoryOptions.PASSWORD, properties.password)


        val options: ConnectionFactoryOptions = optionsBuilder.build()

        val configuration: PostgresqlConnectionConfiguration = PostgresqlConnectionFactoryProvider.builder(options)
            // pgBouncer has issues with prepared statements - https://github.com/pgjdbc/r2dbc-postgresql/issues/223
            .preparedStatementCacheQueries(0)
            .apply {
                // Only register enum codec when enum types is not empty.
                // Otherwise, r2dbc fails to connect with syntax error: empty brackets ("... IN ()")
                if (customEnumTypes.isNotEmpty()) {
                    codecRegistrar(buildEnumCodecRegistrar(customEnumTypes))
                }
            }
            .build()

        return ConnectionPoolConfiguration.builder(PostgresqlConnectionFactory(configuration))
            .buildConfigurationBy(properties.pool)
            .let { ConnectionPool(it) }
    }

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions = R2dbcCustomConversions.of(
        DialectResolver.getDialect(connectionPool()), customEnumTypes
    )

    private fun ConnectionPoolConfiguration.Builder.buildConfigurationBy(
        props: R2dbcProperties.Pool
    ): ConnectionPoolConfiguration {

        maxIdleTime(props.maxIdleTime)

        props.maxLifeTime?.let { maxLifeTime(it) }
        props.maxAcquireTime?.let { maxAcquireTime(it) }
        props.maxCreateConnectionTime?.let { maxCreateConnectionTime(it) }

        initialSize(props.initialSize)
        maxSize(props.maxSize)

        props.validationQuery?.let { validationQuery(it) }
        validationDepth(props.validationDepth)

        return build()
    }

    private fun buildEnumCodecRegistrar(list: List<PgEnumSupport<*>>): CodecRegistrar {
        val builder = EnumCodec.builder()
        list.forEach {
            val type = (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
            builder.withEnum(it.pgEnumName, type as Class<out Enum<*>>)
        }
        return builder.build()
    }


    private inline fun <reified E : Enum<E>> pgEnum(pgEnumName: String) = object : PgEnumSupport<E>(pgEnumName) {}

    abstract class PgEnumSupport<E : Enum<E>>(val pgEnumName: String) : EnumWriteSupport<E>()
}
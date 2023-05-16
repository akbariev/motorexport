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
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.EnumWriteSupport
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver

@Configuration
class R2dbcConfiguration(private val r2dbcProperties: R2dbcProperties) : AbstractR2dbcConfiguration() {

    // лист объектов
    // что хранит стринговое название типа енумки в бд и котлин тип енумки, что будет маппится из объекта в сущность бд.
    val r2dbcEnums = listOf(
        r2dbcEnum<EngineGroup>("engine_group"),
        r2dbcEnum<GearType>("gear_type"),
        r2dbcEnum<Transmission>("transmission"),
        r2dbcEnum<BodyTypeGroup>("body_type_group"),
        r2dbcEnum<InStock>("in_stock"),
    )

    /*
    * spring.r2dbc.url = “r2dbc:${database.url}”
    * spring.r2dbc.username = ${database.username}
    * spring.r2dbc.password = ${database.password}
    * spring.r2dbc.pool.max-size = 5
    * spring.r2dbc.pool.initial-size = 3
    * */
    @Bean
    @ConfigurationProperties("spring.r2dbc")
    fun r2dbcProperties(): R2dbcProperties {
        return R2dbcProperties()
    }


    /*
    * Создаем бин, что будет хранить наши соединения к базе данных - ConnectionPool()
    * Это по сути интерфейс ConnectionFactory, который имплементируется ConnectionPool-ом. Поэтому это одно и то же.
    * Еще. SPI - это service provider interface.
    * SPI - это абстракция, набор методов, что в реализации будут уникальны для каждого конкретного драйвера.
    * */
    @Bean
    override fun connectionFactory(): ConnectionFactory { // здесь иногда прописывают io.r2dbc.ConnectionPool
        // ConnectionFactoryOptions - это моделька, что хранит в себе креды к базе данных
        val connectionFactoryOptions = ConnectionFactoryOptions
            .parse(r2dbcProperties.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, r2dbcProperties.name)
            .option(ConnectionFactoryOptions.PASSWORD, r2dbcProperties.password)
            .build()

        // PostgresqlConnectionFactoryProvider
        val connectionConfiguration: PostgresqlConnectionConfiguration = PostgresqlConnectionFactoryProvider
            // помещаем модельку с кредами
            .builder(connectionFactoryOptions)
            //отключаем возможность кэширования запросов
            .preparedStatementCacheQueries(0)
            // Регистрируем kotlin енумки, которые будут конвертироваться в енумки базы данных.
            .apply {
                // проверяем на пустоту, т.к. нельзя зарегистрировать пустой масив в codecRegistrar, он будет падать с ошибкой
                if (r2dbcEnums.isNotEmpty()) {
                    val enumCodecBuilder = EnumCodec.builder()
                    r2dbcEnums.forEach {
                        // ParametrizedType или параметризированный тип - это рефлексивный тип в Java
                        // он по сути представляет тип объекта, что мы указали в угловых скобках.
                        // возьмем пример ArrayList<String>
                        // javaClass - получаем Class объект, что представляет сам класс List
                        // genericSuperclass - возвращает Type, этот объект представляет суперкласс нашего List. это AbstractList<String>
                        // далее мы кастим этот объект в ParametrizedType и пытаемся узнать, есть ли аргументы типа у нашего массива
                        // для этого мы вызываем actualTypeArguments[0] - это вернет нам тип String
                        // ParametrizedType важный тип, т.к. он позволяет нам работать с
                        // типами дженериков в runtime.
                        val argumentEnumType =
                            (it.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
                        //  здесь мы кастим объект, т.к. знаем, что он действительно относится к типу енум.
                        val castedEnum = argumentEnumType as Class<out Enum<*>>
                        // кладем в билдер, что отвечает за синхронизацию, название енума бд и котлин тип енумки.
                        enumCodecBuilder.withEnum(it.enumType, argumentEnumType)
                    }
                    // регистрируем билдер в регистре.
                    codecRegistrar(enumCodecBuilder.build())
                }
                // создаем объект конфигурации.
            }.build()
        /*
        ConnectionPoolConfiguration - от него мы вызовем билдер и в него мы
        1) поместим PostgresqlConnectionFactory
        2) установим всю конфигурацию будем устанавливать все конфигурационные параметры по пулу коннекшенов из R2dbcProperties.
        */
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
        // далее все настройки, что мы прописали выше мы присвоим в объект ConnectionPool.
        return ConnectionPool(connectionPoolConfiguration.build())
    }

    // это метод содержит reified.
    // reified позволяет получить истинную, актуальную информацию о классе, типе T джинерика в рантайме
    // По сути это нам особо не нужно :) Но некоторые библиотеки требуют информации по конкретному дженерик типу
    // поэтому нас спасает этот синтаксис.
    private inline fun <reified E : Enum<E>> r2dbcEnum(enumType: String) = object : R2dbcEnumConvertor<E>(enumType) {}

    // создали этот класс по сути просто, чтобы он хранил в себе название типа енумки базы данных.
    abstract class R2dbcEnumConvertor<E : Enum<E>>(val enumType: String) : EnumWriteSupport<E>()


    /*
    Для конвертации kotlin енумок в пострес енумки мы указали конфигурацию в PostgresConnectionConfiguration
    Это первая часть конфигурации конвертации енумок :)
    Для второй части конфигурации нам будет нужен отдельный бин
    R2dbcCustomConversions - он отвечает за маппинг объектов в бд типы.

    Этому объекту нужен
    - DialectResolver.getDialect(connectionPool)
    и
    - лист из kotlin енумок с названием енум типа в базе данных.
    */
    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory()), r2dbcEnums)
    }
}

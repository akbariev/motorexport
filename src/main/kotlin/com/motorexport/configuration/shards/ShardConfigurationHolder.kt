package com.motorexport.configuration.shards

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

//@Component
//@EnableConfigurationProperties(ShardsProperties::class)
//@ConditionalOnProperty("db-configuration", havingValue = "dev_yaml", matchIfMissing = false)
class ShardConfigurationHolder(val properties: ShardsProperties) {
    fun getHost(shardNumber: Int): String = properties.hosts[shardNumber - 1]
    fun getPort(shardNumber: Int): Int = properties.ports[shardNumber - 1]
    fun getDatabaseName(shardNumber: Int): String = properties.dbNames[shardNumber - 1]
    fun getUserName(shardNumber: Int): String = properties.userNames[shardNumber - 1]
    fun getPassword(shardNumber: Int): String = properties.passwords[shardNumber - 1]
    fun getMaxShards(): Int = properties.hosts.size

    fun getUrl(shardNumber: Int, migration: Boolean): String {
        val port = getPort(shardNumber)
        val host = getHost(shardNumber)
        val dbName = getDatabaseName(shardNumber)
        val url = StringBuilder()
        if (migration) {
            url.append("jdbc")
        } else {
            url.append("r2dbc")
        }
        return url.append("postgresql://$host:$port/$dbName?prepareThreshold=0").toString()
    }

    /**
    Пример:
    Есть 10 шард
    Приходит идентификатор аккаунта - 9
    10 идентификатор % 10 (количество шард) = 0 + 1
    В мы прибавляет единицу, т.к. отсчет шардирования начинается с единицы
     */
    fun getShardByAccountId(accountId: Int): Int {
        return (accountId % getMaxShards().toLong()).toInt() + 1
    }
}
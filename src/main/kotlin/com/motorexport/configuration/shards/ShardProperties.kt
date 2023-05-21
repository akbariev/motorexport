package com.motorexport.configuration.shards

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

//@ConstructorBinding
//@ConfigurationProperties("shards")
//@ConditionalOnProperty("db-configuration", havingValue = "master_yaml", matchIfMissing = false)
data class ShardsProperties(
    val hosts: List<String>,
    val ports: List<Int>,
    val dbNames: List<String>,
    val userNames: List<String>,
    val passwords: List<String>,
)
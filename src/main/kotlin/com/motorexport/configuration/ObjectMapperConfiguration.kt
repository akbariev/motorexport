package com.motorexport.configuration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean


@Bean
fun objectMapper() = ObjectMapper().findAndRegisterModules()
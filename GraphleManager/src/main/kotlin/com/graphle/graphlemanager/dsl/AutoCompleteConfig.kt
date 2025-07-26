package com.graphle.graphlemanager.dsl

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AutoCompleteProperties::class)
open class AutoCompleteConfig
package com.graphle.graphlemanager.dsl

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration for properties related to AutoCompleteProperties
 * @see AutoCompleteProperties
 */
@Configuration
@EnableConfigurationProperties(AutoCompleteProperties::class)
open class AutoCompleteConfig
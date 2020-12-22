package org.koil.schema

import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.mapper.RowMapperFactory

fun factory(type: Class<*>, prefix: String): RowMapperFactory {
    return RowMapperFactory.of(type, KotlinMapper(type, prefix))
}

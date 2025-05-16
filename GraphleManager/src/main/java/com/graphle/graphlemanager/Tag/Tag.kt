package com.graphle.graphlemanager.Tag

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import java.util.*

@JvmRecord
data class Tag(
    @field:GeneratedValue @field:Id val id: UUID?,
    @field:NonNull @param:NonNull val name: String,
    @field:Nullable @param:Nullable val value: String?,
    @field:Nullable @param:Nullable val numericValue: Double?
) {
    constructor(@NonNull name: String, @Nullable value: String?, @Nullable numericValue: Double?) : this(
        null,
        name,
        value,
        numericValue
    )
}

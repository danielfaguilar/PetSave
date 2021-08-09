package com.raywenderlich.android.petsave.common.data.cache.model.cachedorganization

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalWithDetails

class CachedOrganizationAggregate(
    @Embedded
    val organization: CachedOrganization,
    @Relation(
        parentColumn = "organizationId",
        entityColumn = "organizationId"
    )
    val animals: List<CachedAnimalWithDetails>
)
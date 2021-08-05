package com.raywenderlich.android.petsave.common.domain.model.animal.details

import com.raywenderlich.android.petsave.common.domain.model.animal.AdoptionStatus
import com.raywenderlich.android.petsave.common.domain.model.animal.Id
import com.raywenderlich.android.petsave.common.domain.model.animal.Media
import org.threeten.bp.LocalDateTime

data class AnimalWithDetails(
    val id: Id,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
)
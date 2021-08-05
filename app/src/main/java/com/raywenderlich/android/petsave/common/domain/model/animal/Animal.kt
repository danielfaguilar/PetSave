package com.raywenderlich.android.petsave.common.domain.model.animal

import org.threeten.bp.LocalDateTime
import java.lang.Exception

data class Animal(
    val id: Id,
    val name: String,
    val type: String,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
)

@JvmInline
value class Id(private val value: Long) {
    init {
        validate(value)
    }
    private fun validate(id: Long): Either<IdException, Boolean> {
        return when {
            id.hasInvalidValue() -> Either.Left(IdException.InvalidIdException(id))
            id.exceedsLength() -> Either.Left(IdException.InvalidIdLengthException(id))
            else -> Either.Right(true)
        }
    }
}

private fun Long.hasInvalidValue() =
    this == 0L || this == -1L
private fun Long.exceedsLength() =
    this > Long.MAX_VALUE - 1

sealed class Either<out A, out B> {
    class Left<A>(val value: A): Either<A, Nothing>()
    class Right<B>(val value: B): Either<Nothing, B>()
}
sealed class IdException(message: String): Exception(message) {
    data class InvalidIdException(val id: Long): IdException("$id")
    data class InvalidIdLengthException(val id: Long): IdException("$id")
}
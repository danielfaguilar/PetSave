package com.raywenderlich.android.petsave.common.domain.model.animal
import org.junit.Assert.*
import org.junit.Test

class MediaTest {
    private val mediumPhoto = "mediumPhoto"
    private val largePhoto = "largePhoto"
    private val invalidPhoto = ""

    @Test
    fun photo_getSmallestAvailable_hasMediumPhoto() {
        // Given
        val photo = Media.Photo(mediumPhoto, largePhoto)
        val expectedPhoto = mediumPhoto

        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // Then
        assertEquals(smallestPhoto, expectedPhoto)
    }

    @Test
    fun photo_getSmallestAvailable_noMediumPhoto() {
        // Given
        val photo = Media.Photo(invalidPhoto, largePhoto)
        val expectedPhoto = largePhoto

        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // Then
        assertEquals(smallestPhoto, expectedPhoto)
    }

    @Test
    fun photo_getSmallestAvailable_noPhotos() {
        // Given
        val photo = Media.Photo(invalidPhoto, invalidPhoto)
        val expectedPhoto = invalidPhoto

        // When
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // Then
        assertEquals(smallestPhoto, expectedPhoto)
    }

}
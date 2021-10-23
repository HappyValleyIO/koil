package org.koil.image

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class FileBackedImageNormalizerTest {
    val imageNormalizer = FileBackedImageNormalizer()
    val png = this::class.java.getResourceAsStream("/img/large.png")!!
    val jpg = this::class.java.getResourceAsStream("/img/large.jpg")!!
    val scaledPng = this::class.java.getResourceAsStream("/img/scaled-png.png")!!
    val scaledJpg = this::class.java.getResourceAsStream("/img/scaled-jpg.png")!!

    @Test
    internal fun `convert jpg to png and resize`() {
        val result = imageNormalizer.normalize(jpg, ImageNormalizationParameters(outputImageType = ImageType.PNG))

        assertThat(result.readAllBytes().toTypedArray()).isEqualTo(
            scaledJpg.readAllBytes().toTypedArray()
        )
    }

    @Test
    internal fun `resize png file`() {
        val result = imageNormalizer.normalize(png, ImageNormalizationParameters(outputImageType = ImageType.PNG))

        assertThat(result.readAllBytes().toTypedArray()).isEqualTo(
            scaledPng.readAllBytes().toTypedArray()
        )
    }

}

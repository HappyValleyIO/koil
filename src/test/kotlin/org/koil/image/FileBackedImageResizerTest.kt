package org.koil.image

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class FileBackedImageResizerTest {
    val resizer = FileBackedImageResizer()
    val png = this::class.java.getResourceAsStream("/img/large.png")!!
    val jpg = this::class.java.getResourceAsStream("/img/large.jpg")!!
    val scaledPng = this::class.java.getResourceAsStream("/img/scaled-png.png")!!
    val scaledJpg = this::class.java.getResourceAsStream("/img/scaled-jpg.png")!!

    @Test
    internal fun `convert jpg to png`() {
        val result = resizer.resize(jpg)

        assertThat(result.readAllBytes().toTypedArray()).isEqualTo(
            scaledJpg.readAllBytes().toTypedArray()
        )
    }

    @Test
    internal fun `resize png file`() {
        val result = resizer.resize(png)

        assertThat(result.readAllBytes().toTypedArray()).isEqualTo(
            scaledPng.readAllBytes().toTypedArray()
        )
    }

}

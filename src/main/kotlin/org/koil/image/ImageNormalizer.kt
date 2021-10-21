package org.koil.image

import org.imgscalr.Scalr
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Image type. Format name corresponds to the format names recognized by java's ImageIO class.
 */
enum class ImageType(val formatName: String) {
    PNG("png")
}

data class ImageNormalizationParameters(
    val width: Int = 150,
    val outputImageType: ImageType = ImageType.PNG
) {
    val format = outputImageType.formatName
}

interface ImageNormalizer {
    fun normalize(
        img: InputStream,
        parameters: ImageNormalizationParameters = ImageNormalizationParameters()
    ): InputStream
}

@Component
class FileBackedImageNormalizer : ImageNormalizer {
    override fun normalize(img: InputStream, parameters: ImageNormalizationParameters): InputStream {
        val scaled = Scalr.resize(ImageIO.read(img), parameters.width)
        val temp = File.createTempFile("image", parameters.format)
        ImageIO.write(scaled, "png", temp)
        return FileInputStream(temp)
    }
}

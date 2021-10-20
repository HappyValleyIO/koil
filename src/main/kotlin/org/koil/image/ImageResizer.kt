package org.koil.image

import org.imgscalr.Scalr
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.imageio.ImageIO

interface ImageResizer {
    fun resize(img: InputStream, width: Int = 150): InputStream
}

@Component
class FileBackedImageResizer : ImageResizer {
    override fun resize(img: InputStream, width: Int): InputStream {
        val scaled = Scalr.resize(ImageIO.read(img), width)
        val temp = File.createTempFile("image", "png")
        ImageIO.write(scaled, "png", temp)
        return FileInputStream(temp)
    }
}

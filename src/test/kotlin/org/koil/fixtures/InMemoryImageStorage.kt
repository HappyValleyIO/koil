package org.koil.fixtures

import org.koil.image.Storage
import java.io.InputStream
import java.util.*

class InMemoryImageStorage : Storage {
    override fun saveImage(id: UUID, file: InputStream, contentType: String) {
        println("Saved image!")
    }

    override fun getPresignedImageUrl(id: UUID): String = id.toString()
}

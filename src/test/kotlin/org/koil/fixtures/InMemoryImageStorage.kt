package org.koil.fixtures

import org.koil.image.Storage
import java.io.InputStream
import java.util.*

class InMemoryImageStorage : Storage {
    override fun saveObject(id: UUID, file: InputStream, contentType: String) {
        println("Saved image!")
    }

    override fun getPresignedObjectUrl(id: UUID): String = id.toString()
}

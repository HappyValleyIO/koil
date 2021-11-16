package org.koil.view.extensions

import assertk.assertThat
import assertk.assertions.contains
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AssetHashTest {
    private val mix = AssetHash()

    @Test
    internal fun `successfully return asset file from the manifest`() {
        val result = mix("/js/packs/application.js")

        assertThat(result).contains("application.js")
    }

    @Test
    internal fun `throw exception if the asset doesn't exist`() {
        assertThrows<MissingAssetResource> {
            mix("/js/packs/missing.js")
        }
    }

    @Test
    internal fun `throw exception is invalid path provided`() {
        assertThrows<IllegalArgumentException> {
            mix("application.js")
        }
    }
}

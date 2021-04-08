package org.koil.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Function
import org.koil.view.extensions.MixFunction
import org.springframework.stereotype.Component

/**
 * The pebble starter will auto-wire this extension.
 */
@Component
class PebbleExtensions : AbstractExtension() {
    override fun getFunctions(): MutableMap<String, Function> {
        return mutableMapOf(
            "mix" to MixFunction()
        )
    }
}

package org.koil.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

data class MissingAssetResource(val name: String) : RuntimeException() {
    override val message: String = "Could not find asset [$name] in the resources"
}

internal class AssetHash {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AssetHash::class.java)
    }

    private val hashCache: MutableMap<String, String> = ConcurrentHashMap()

    internal operator fun invoke(path: String): String {
        require(path.startsWith("/")) {
            "Asset path must start with '/'. You passed $path."
        }

        if (hashCache[path] == null) {
            val file: InputStream =
                this::class.java.getResourceAsStream("/static/assets$path") ?: throw MissingAssetResource(path)
            val hash = file.readAllBytes().contentHashCode()

            logger.debug("Caching asset [$path] hash value as [$hash]")
            hashCache[path] = "$path?id=$hash"
        }

        return hashCache[path] ?: throw RuntimeException("Unexpectedly missing resource after asset cache load: [$path]")
    }
}

/**
 * To use this in pebble, use as a function in a template like so:
 *
 * <script type=module" src="{{ mix("/js/packs/applications.js") }}"></script>
 *
 * Which will be re-written at render into something like:
 *
 * <script type=module" src="/assets/js/packs/applications.js"></script>
 */
internal class MixFunction(private val mix: AssetHash = AssetHash()) : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("name")
    }

    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        val name = args?.get("name") as String?
        requireNotNull(name) {
            "Mix requires the name of the asset you're trying to bundle."
        }

        return "/assets${mix(name)}"
    }
}

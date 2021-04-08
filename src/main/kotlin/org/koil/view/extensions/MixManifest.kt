package org.koil.view.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class MissingMixAsset(val name: String) : RuntimeException() {
    override val message: String = "Could not find $name in the manifest file"
}

internal class MixManifest {
    companion object {
        const val manifestFile = "/static/assets/mix-manifest.json"
        private val logger: Logger = LoggerFactory.getLogger(MixManifest::class.java)
    }

    private val manifestMap by lazy { loadManifestMap() }

    @Synchronized
    internal operator fun invoke(path: String): String {
        require(path.startsWith("/")) {
            "Asset path must start with '/'. You passed $path."
        }
        val manifest = manifestMap[path] ?: throw MissingMixAsset(path)
        logger.debug("Loading mix file {}", manifest)

        return manifest
    }

    private fun loadManifestMap(): Map<String, String> {
        val text: String = this::class.java.getResource(manifestFile)?.readText()
            ?: throw RuntimeException("Mix manifestMap file doesn't exist! This error is completely unexpected and likely indicates and issue with the build")

        return ObjectMapper().readValue(text)
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
internal class MixFunction(private val mix: MixManifest = MixManifest()) : Function {
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

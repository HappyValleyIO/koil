const purgecss = require('@fullhuman/postcss-purgecss')

const isProd = process.env.NODE_ENV === "production"

const prodPlugins = [purgecss({
    content: ['../**/*.peb', 'js/**/*.js', "../../**/*.peb"]
})]

const plugins = isProd ? prodPlugins : []

module.exports = {
    plugins: [
        ...plugins
    ]
}

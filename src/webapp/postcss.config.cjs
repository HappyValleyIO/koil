const purgeCSSPlugin = require('@fullhuman/postcss-purgecss');
const autoprefixer = require('autoprefixer')
const isProd = process.env.NODE_ENV === "production"

const prodPlugins = [purgeCSSPlugin({
    content: ['../**/*.peb', 'js/**/*.js', "../../**/*.peb"]
})]

const plugins = isProd ? prodPlugins : []

module.exports = {
    plugins: [
        ...plugins
    ]
}

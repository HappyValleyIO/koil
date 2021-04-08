const purgecss = require('@fullhuman/postcss-purgecss')

module.exports = {
    plugins: [
        require('autoprefixer'),
        purgecss({
            content: ['../**/*.peb', 'js/**/*.js']
        })
    ]
}
const mix = require('laravel-mix')

mix.setPublicPath('dist')
    .setResourceRoot('/assets')
    .css("css/packs/app.css", "css/packs/app.css")
    .sourceMaps(true)

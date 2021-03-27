const mix = require('laravel-mix')
const path = require("path");
const glob = require("glob");


glob.sync("css/packs/*.scss")
    .forEach(file => {
        mix
            .sass(file, file.replace('scss', 'css'))
            .setResourceRoot('/assets')
    });

glob.sync("js/packs/*.js")
    .forEach(file => {
        mix
            .setPublicPath('dist')
            .js(file, file)
    });

mix.webpackConfig({
    cache: {
        type: 'filesystem',
        cacheDirectory: path.resolve(__dirname, '.temp_cache'),
    },
});

mix.version()

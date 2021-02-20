const mix = require('laravel-mix')
const path = require("path");
const glob = require("glob");

glob.sync("css/packs/*.css")
  .forEach(file => {
    mix
      .css(file, file)
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

// // apply versioning only in production
if (mix.inProduction()) {
  mix.version()
}

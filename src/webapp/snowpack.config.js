// Snowpack Configuration File
// See all supported options: https://www.snowpack.dev/reference/configuration

const multi = require('@rollup/plugin-multi-entry');

const plugins = [
  [
    "snowpack-plugin-rollup-bundle",
    {
      emitHtmlFiles: false,
      preserveSourceFiles: false,
      preserveEntrySignatures: false,
      entrypoints: ['js/index.js', 'css/index.css'],
    },
  ],
]

module.exports = {
  plugins: plugins,
};

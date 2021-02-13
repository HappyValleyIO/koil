const plugins = [
  [
    "snowpack-plugin-rollup-bundle",
    {
      emitHtmlFiles: false,
      preserveSourceFiles: false,
      preserveEntrySignatures: false,
      // entrypoints: ['js/application.js', 'css/index.css'],
    },
  ],
]

module.exports = {
  plugins: [],
};

const esbuild = require('esbuild')
const {stimulusPlugin} = require('esbuild-plugin-stimulus');

esbuild.build({
    entryPoints: ['js/packs/application.js'],
    bundle: true,
    format: 'esm',
    minify: true,
    sourcemap: true,
    target: ['es6'],
    plugins: [stimulusPlugin()],
    outfile: 'dist/js/packs/application.js',
}).catch((e) => {
    console.error(e)
    process.exit(1)
})

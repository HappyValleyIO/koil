import esbuild from 'esbuild'
import {stimulusPlugin} from "esbuild-plugin-stimulus";
import postcssPlugin from '@chialab/esbuild-plugin-postcss';

esbuild.build({
    entryPoints: ['js/packs/application.js'],
    bundle: true,
    format: 'esm',
    minify: true,
    sourcemap: true,
    target: ['es6'],
    plugins: [stimulusPlugin()],
    outfile: 'dist/js/packs/application.js',
}).then(() => {
    console.log(`[+] Esbuild application.js succeeded.`)
}).catch((e) => {
    console.error(e)
    process.exit(1)
})

esbuild.build({
    entryPoints: ['css/packs/app.css'],
    bundle: true,
    minify: true,
    sourcemap: true,
    loader: { // built-in loaders: js, jsx, ts, tsx, css, json, text, base64, dataurl, file, binary
        '.ttf': 'file',
        '.otf': 'file',
        '.svg': 'file',
        '.eot': 'file',
        '.woff': 'file',
        '.woff2': 'file'
    },
    plugins: [postcssPlugin()],
    outdir: 'dist/css/packs',
})
    .then(() => {
        console.log(`[+] Esbuild app.css succeeded.`)
    })
    .catch((e) => {
        console.error(e)
        process.exit(1)
    })

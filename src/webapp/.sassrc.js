const path = require('path')

const CWD = process.cwd()

module.exports = {
    "includePaths": [
        path.resolve(CWD, 'node_modules'),
        path.resolve(CWD, 'styles')
    ]
};

// See https://github.com/parcel-bundler/parcel/issues/39 for why this is required.

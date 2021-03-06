require('shelljs/global')
let webpack = require('webpack')
let conf = require('./webpack.prod')
let ora = require('ora')
let spinner = ora('building for production...')

spinner.start()

rm('-rf', 'dist')
mkdir('-p', 'dist/bower_components', 'dist/partials')
cp('-r', 'bower_components/', 'partials/', 'dist/')

webpack(conf, function (err, stats) {
  spinner.stop()

  if (err) {
    throw err
  }

  process.stdout.write(stats.toString({
    colors: true,
    modules: false,
    children: false,
    chunks: false,
    chunkModules: false
  }) + '\n')
})

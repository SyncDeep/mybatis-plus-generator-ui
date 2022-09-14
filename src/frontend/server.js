const {
  Nuxt,
  Builder
} = require('nuxt')
const app = require('express')()
const port = process.env.PORT || 3000
var proxyMiddleware = require('http-proxy-middleware')
// Initialize Nuxt.js instance by passing in configuration
let config = require('./nuxt.config.js')
const nuxt = new Nuxt(config)

// proxy api requests here is the setting of the added proxyTable middle price
var proxyTable = config.proxy
Object.keys(proxyTable).forEach(function (context) {
  var options = proxyTable[context]
  if (typeof options === 'string') {
    options = {
      target: options
    }
  }
  app.use(proxyMiddleware(options.filter || context, options))
})
app.use(nuxt.render) //Here is the middleware that adds the nuxt rendering layer service

// Compile in development mode

new Builder(nuxt).build()
  .catch((error) => {
    console.error(error)
    process.exit(1)
  })


// listen on specified port
app.listen(port, '0.0.0.0')
console.log('The server runs on localhost:' + port)
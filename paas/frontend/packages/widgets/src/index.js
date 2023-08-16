import Property from './Property'
let SRE_builtInWidgets = {}
let SRE_builtInWidgetMetaMapping = {}
const SRE_widgetsContext = require.context(
  './srewidgets/',
  true,
  // /^\.\/srewidgets\/((?!\/)[\s\S])+\/index\.js$/,
  /index\.js$/,
)
SRE_widgetsContext.keys().forEach((key) => {
  // let widgetName = key.split('/')[2]
  let reg = new RegExp('index.js', 'g')
  let widgetName = key.split('/')[1].replace(reg, '')
  SRE_builtInWidgets[widgetName] = SRE_widgetsContext(key)
})
const SRE_widgetMetasContext = require.context(
  './srewidgets/',
  true,
  // /^\.\/srewidgets\/((?!\/)[\s\S])+\/meta\.js$/,
  /meta\.js$/,
)
SRE_widgetMetasContext.keys().forEach((key) => {
  // let widgetName = key.split('/')[2]
  let reg = new RegExp('index.js', 'g')
  let widgetName = key.split('/')[1].replace(reg, '')
  const meta = SRE_widgetMetasContext(key)
  if (!meta.type) {
    meta.type = widgetName
  }
  SRE_builtInWidgetMetaMapping[meta.type] = meta
})

export { SRE_builtInWidgets, SRE_builtInWidgetMetaMapping, Property }

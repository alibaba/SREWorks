import Property from './Property'
let SRE_builtInWidgets = {}
let SRE_builtInWidgetMetaMapping = {}
const SRE_widgetsContext = require.context(
  './',
  true,
  /^\.\/srewidgets\/((?!\/)[\s\S])+\/index\.js$/,
)
SRE_widgetsContext.keys().forEach((key) => {
  let widgetName = key.split('/')[2]
  SRE_builtInWidgets[widgetName] = SRE_widgetsContext(key)
})
const SRE_widgetMetasContext = require.context(
  './',
  true,
  /^\.\/srewidgets\/((?!\/)[\s\S])+\/meta\.js$/,
)
SRE_widgetMetasContext.keys().forEach((key) => {
  let widgetName = key.split('/')[2]
  const meta = SRE_widgetMetasContext(key)
  if (!meta.type) {
    meta.type = widgetName
  }
  SRE_builtInWidgetMetaMapping[meta.type] = meta
})

export { SRE_builtInWidgets, SRE_builtInWidgetMetaMapping, Property }

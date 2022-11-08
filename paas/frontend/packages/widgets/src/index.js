let SRE_builtInWidgets = {}
let SRE_builtInWidgetMetaMapping = {}
const widgetsContext = require.context('./', true, /^\.\/srewidgets\/((?!\/)[\s\S])+\/index\.js$/)
widgetsContext.keys().forEach((key) => {
  let widgetName = key.split('/')[2]
  SRE_builtInWidgets[widgetName] = widgetsContext(key)
})
const widgetMetasContext = require.context(
  './',
  true,
  /^\.\/srewidgets\/((?!\/)[\s\S])+\/meta\.js$/,
)
widgetMetasContext.keys().forEach((key) => {
  let widgetName = key.split('/')[2]
  const meta = widgetMetasContext(key)
  if (!meta.type) {
    meta.type = widgetName
  }
  SRE_builtInWidgetMetaMapping[meta.type] = meta
})

export { SRE_builtInWidgets, SRE_builtInWidgetMetaMapping }

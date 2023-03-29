import Vue from 'vue'
import App from './App.vue'
import element from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false
Vue.use(element)
new Vue({
  render: (h) => h(App),
}).$mount('#app')

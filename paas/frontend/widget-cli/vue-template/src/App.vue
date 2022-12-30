<template>
  <div id="app">
    <el-button @click="changeDate" type='primary'>改变日期</el-button>
    <vuetimeline />
  </div>
</template>

<script>
import vuetimeline from './components/vuetimeline/index.vue'

export default {
  name: 'App',
  components: {
    vuetimeline
  },
  data() {
    return {
      widgetConfig: {currentValue:'2019-03-24'}
    }
  },
  methods: {
    changeDate() {
      this.$set(this.widgetConfig,'currentValue','2019-03-25')
      console.log(this.widgetConfig,'widgetConfig-current')
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>

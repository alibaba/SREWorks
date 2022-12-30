<template>
<div>
  <el-timeline :reverse="widgetConfig.reverse">
    <el-timeline-item
      :size="widgetConfig.size"
      v-for="(activity, index) in (widgetData || activities)"
      :key="index"
      :timestamp="activity.timestamp">
      {{activity.content}}
    </el-timeline-item>
  </el-timeline>
</div>
</template>
<script>
  export default {
      data() {
      return {
        // reverse:true,
        activities: [{
          content: '活动按期开始',
          timestamp: '2018-04-15'
        }, {
          content: '通过审核',
          timestamp: '2018-04-13'
        }, {
          content: '创建成功',
          timestamp: '2018-04-11'
        }]
      };
    },
    props: {
        widgetConfig: {
            type: Object,
            default: () => {
                return {
                    reverse: false,
                    size: 'normal'
                }
            }
        },
        widgetData:{
          default:()=> {
            return [{
          content: '活动按期开始',
          timestamp: '2018-04-15'
        }, {
          content: '通过审核',
          timestamp: '2018-04-13'
        }, {
          content: '创建成功',
          timestamp: '2018-04-11'
        }]
          }
        },
        nodeParams: {}
    },
    mounted() {
      console.log(this.widgetData,'widgetProps-widgetProps')
    },
    watch: {
      'widgetConfig.reverse': {
        handler(value) {
          console.log(this.widgetConfig,value,"widgetProps-widgetProps-reverse")
        }
      },
      'widgetConfig.size': {
        handler(value) {
          console.log(this.widgetConfig,value,"widgetProps-widgetProps-size")
        }
      },
    }
  };
</script>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import http from '@/utils/http'

// 数据统计
const statistics = ref({
  heritageCount: 0,
  videoCount: 0,
  userCount: 0,
  activityCount: 0
})

// 图表实例
const lineChart = ref(null)
const barChart = ref(null)
const pieChart = ref(null)
const hotChart = ref(null)

// 获取统计数据
const getStatistics = async () => {
  try {
    const response = await http.get('/statistics/overview')
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取折线图数据
const getLineChartData = async () => {
  try {
    const response = await http.get('/statistics/line-chart')
    if (response.code === 200) {
      initLineChart(response.data)
    }
  } catch (error) {
    console.error('获取折线图数据失败:', error)
  }
}

// 获取柱状图数据
const getBarChartData = async () => {
  try {
    const response = await http.get('/statistics/bar-chart')
    if (response.code == 200) {
      initBarChart(response.data)
    }
  } catch (error) {
    console.error('获取柱状图数据失败:', error)
  }
}

// 获取饼图数据
const getPieChartData = async () => {
  try {
    const response = await http.get('/statistics/pie-chart')
    if (response.code === 200) {
      initPieChart(response.data)
    }
  } catch (error) {
    console.error('获取饼图数据失败:', error)
  }
}

// 获取热门非遗数据
const getHotHeritageData = async () => {
  try {
    const response = await http.get('/statistics/hot-heritage')
    if (response.code === 200) {
      initHotChart(response.data)
    }
  } catch (error) {
    console.error('获取热门非遗数据失败:', error)
  }
}

// 初始化折线图
const initLineChart = (data) => {
  const chart = echarts.init(lineChart.value)
  const option = {
    title: {
      text: '最近7天新增趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['非遗文物', '宣传视频', '注册用户', '活动数量'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '非遗文物',
        type: 'line',
        data: data.heritageData,
        smooth: true,
        itemStyle: { color: '#5470c6' }
      },
      {
        name: '宣传视频',
        type: 'line',
        data: data.videoData,
        smooth: true,
        itemStyle: { color: '#91cc75' }
      },
      {
        name: '注册用户',
        type: 'line',
        data: data.userData,
        smooth: true,
        itemStyle: { color: '#fac858' }
      },
      {
        name: '活动数量',
        type: 'line',
        data: data.activityData,
        smooth: true,
        itemStyle: { color: '#ee6666' }
      }
    ]
  }
  chart.setOption(option)
}

// 初始化柱状图
const initBarChart = (data) => {
  const chart = echarts.init(barChart.value)
  const option = {
    title: {
      text: '各类型数据总量对比',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.categories
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: data.data,
        barWidth: '60%'
      }
    ]
  }
  chart.setOption(option)
}

// 初始化饼图
const initPieChart = (data) => {
  const chart = echarts.init(pieChart.value)
  const option = {
    title: {
      text: '各分类非遗占比',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [
      {
        name: '非遗分布',
        type: 'pie',
        radius: '50%',
        center: ['60%', '50%'],
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

// 初始化热门非遗图表
const initHotChart = (data) => {
    console.log("data", data);
    
  const chart = echarts.init(hotChart.value)
  const option = {
    title: {
      text: '热门非遗文物排行',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.names
    },
    series: [
      {
        name: '浏览量',
        type: 'bar',
        data: data.viewCounts,
        barWidth: '60%'
      }
    ]
  }
  chart.setOption(option)
}

// 页面挂载时获取数据
onMounted(() => {
  getStatistics()
  getLineChartData()
  getBarChartData()
  getPieChartData()
  getHotHeritageData()
})
</script>

<template>
  <div class="chart-page">
    <!-- 数据统计卡片 -->
    <div class="statistics-cards">
      <div class="card">
        <div class="card-icon heritage">
          <i class="el-icon-collection"></i>
        </div>
        <div class="card-content">
          <div class="card-title">非遗文物</div>
          <div class="card-value">{{ statistics.heritageCount }}</div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-icon video">
          <i class="el-icon-video-camera"></i>
        </div>
        <div class="card-content">
          <div class="card-title">宣传视频</div>
          <div class="card-value">{{ statistics.videoCount }}</div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-icon user">
          <i class="el-icon-user"></i>
        </div>
        <div class="card-content">
          <div class="card-title">注册用户</div>
          <div class="card-value">{{ statistics.userCount }}</div>
        </div>
      </div>
      
      <div class="card">
        <div class="card-icon activity">
          <i class="el-icon-calendar"></i>
        </div>
        <div class="card-content">
          <div class="card-title">活动数量</div>
          <div class="card-value">{{ statistics.activityCount }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="chart-row">
        <div class="chart-item">
          <div ref="lineChart" class="chart"></div>
        </div>
        <div class="chart-item">
          <div ref="pieChart" class="chart"></div>
        </div>
      </div>
      
      <div class="chart-row">
        <div class="chart-item">
          <div ref="barChart" class="chart"></div>
        </div>
        <div class="chart-item">
          <div ref="hotChart" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chart-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
  
  .card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-2px);
    }
    
    .card-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      
      i {
        font-size: 24px;
        color: white;
      }
      
      &.heritage {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.video {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.user {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      &.activity {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }
    
    .card-content {
      flex: 1;
      
      .card-title {
        font-size: 14px;
        color: #666;
        margin-bottom: 5px;
      }
      
      .card-value {
        font-size: 28px;
        font-weight: bold;
        color: #333;
      }
    }
  }
}

.charts-container {
  .chart-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
    
    .chart-item {
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .chart {
        width: 100%;
        height: 400px;
      }
    }
  }
}

@media (max-width: 1200px) {
  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-container .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: 1fr;
  }
  
  .chart-page {
    padding: 10px;
  }
}
</style>
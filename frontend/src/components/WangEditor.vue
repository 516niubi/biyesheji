<template>
  <div style="border: 1px solid #ccc">
    <!-- 工具栏 -->
    <Toolbar
      style="border-bottom: 1px solid #ccc"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <!-- 编辑器 -->
    <Editor
      style="height: 500px; overflow-y: hidden;"
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="handleCreated"
    />
  </div>
</template>

<script setup>
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import Config from '../config/index'

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

// 内容 HTML
const valueHtml = ref('')

// 模拟 v-model
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: String,
    default: '500px'
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue'])

// 监听外部传入的值
watch(() => props.modelValue, (newVal) => {
  valueHtml.value = newVal
}, { immediate: true })

// 监听编辑器内容变化
watch(valueHtml, (newVal) => {
  emit('update:modelValue', newVal)
})

// 编辑器配置
const mode = 'default' // 或 'simple'

// 工具栏配置
const toolbarConfig = {
  excludeKeys: [
    'group-image', // 排除图片相关功能，使用自定义上传
    'group-video' // 排除视频相关功能
  ]
}

// 编辑器配置
const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {
    // 配置上传图片
    uploadImage: {
      server: Config.baseURL + '/file/wang/upload', // 上传接口地址
      fieldName: 'file', // 上传文件的字段名
      maxFileSize: 2 * 1024 * 1024, // 2M
      allowedFileTypes: ['image/*'],
      
      // 自定义插入图片
      customInsert(res, insertFn) {
        // res 即服务端的返回结果
        if (res.errno === 0 && res.data && res.data.length > 0) {
          // 从 res 中找到 url alt href ，然后插入图片
          const url = res.data[0].url
          insertFn(url, '', '')
        } else {
          console.error('图片上传失败', res)
        }
      },
      
      // 上传错误
      onError(file, err, res) {
        console.error('图片上传出错', err, res)
      }
    }
  }
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})

const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}
</script>

<style scoped>
/* 编辑器样式 */
</style>
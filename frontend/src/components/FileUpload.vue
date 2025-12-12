<template>
  <div class="file-upload">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :file-list="fileList"
      :limit="limit"
      :accept="accept"
      :disabled="disabled"
    >
      <el-button type="primary" :disabled="disabled">
        <el-icon><Upload /></el-icon>
        {{ buttonText }}
      </el-button>
      <template #tip>
        <div class="el-upload__tip" v-if="tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
  uploadUrl: {
    type: String,
    default: '/api/upload',
  },
  limit: {
    type: Number,
    default: 1,
  },
  accept: {
    type: String,
    default: 'image/*',
  },
  maxSize: {
    type: Number,
    default: 10 * 1024 * 1024, // 10MB
  },
  buttonText: {
    type: String,
    default: '选择文件',
  },
  tip: {
    type: String,
    default: '',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'success', 'error'])

const fileList = ref([])
const uploading = ref(false)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : '',
  }
})

const uploadData = computed(() => {
  return {}
})

const beforeUpload = (file) => {
  // 检查文件大小
  if (file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${(props.maxSize / 1024 / 1024).toFixed(0)}MB`)
    return false
  }

  // 检查文件类型
  if (props.accept && !file.type.match(props.accept.replace('*', '.*'))) {
    ElMessage.error('文件类型不支持')
    return false
  }

  uploading.value = true
  return true
}

const handleSuccess = (response, file) => {
  uploading.value = false
  if (response.success && response.data) {
    const fileUrl = response.data.url || response.data
    const newFileList = [...fileList.value, { url: fileUrl, name: file.name }]
    fileList.value = newFileList
    emit('update:modelValue', newFileList.map(f => f.url))
    emit('success', response, file)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleError = (error, file) => {
  uploading.value = false
  ElMessage.error('上传失败，请重试')
  emit('error', error, file)
}

const handleProgress = (event, file) => {
  // 可以在这里显示上传进度
  console.log('Upload progress:', event.percent)
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
</style>


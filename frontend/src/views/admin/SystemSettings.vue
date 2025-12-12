<template>
  <div class="system-settings">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="basicSettings" label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="系统名称">
              <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input
                v-model="basicSettings.systemDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入系统描述"
              />
            </el-form-item>
            <el-form-item label="默认库存预警值">
              <el-input-number v-model="basicSettings.defaultMinStock" :min="0" style="width: 100%" />
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                新建药品时默认的最低库存预警值
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings" :loading="saving">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="审核设置" name="audit">
          <el-form :model="auditSettings" label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="自动审核服务地址">
              <el-input v-model="auditSettings.auditServiceUrl" placeholder="请输入审核服务地址" />
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                例如：http://localhost:5000
              </div>
            </el-form-item>
            <el-form-item label="审核通过阈值">
              <el-input-number v-model="auditSettings.passThreshold" :min="0" :max="100" style="width: 100%" />
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                审核得分达到此值及以上视为通过（0-100）
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAuditSettings" :loading="saving">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="其他设置" name="other">
          <el-form label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="数据备份">
              <el-button type="primary">备份数据</el-button>
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                备份所有系统数据
              </div>
            </el-form-item>
            <el-form-item label="系统日志">
              <el-button type="info">查看日志</el-button>
              <div style="color: #909399; font-size: 12px; margin-top: 5px">
                查看系统操作日志
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')
const saving = ref(false)

const basicSettings = reactive({
  systemName: '医院药房智能管理系统',
  systemDescription: '用于管理医院药房的药品、库存、处方等业务',
  defaultMinStock: 10,
})

const auditSettings = reactive({
  auditServiceUrl: 'http://localhost:5000',
  passThreshold: 80,
})

const loadSettings = () => {
  // 从localStorage加载设置
  const savedBasicSettings = localStorage.getItem('systemBasicSettings')
  const savedAuditSettings = localStorage.getItem('systemAuditSettings')
  
  if (savedBasicSettings) {
    Object.assign(basicSettings, JSON.parse(savedBasicSettings))
  }
  
  if (savedAuditSettings) {
    Object.assign(auditSettings, JSON.parse(savedAuditSettings))
  }
}

const saveBasicSettings = async () => {
  saving.value = true
  try {
    // 保存到localStorage（实际应该保存到后端）
    localStorage.setItem('systemBasicSettings', JSON.stringify(basicSettings))
    ElMessage.success('基本设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const saveAuditSettings = async () => {
  saving.value = true
  try {
    // 保存到localStorage（实际应该保存到后端）
    localStorage.setItem('systemAuditSettings', JSON.stringify(auditSettings))
    ElMessage.success('审核设置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
</style>



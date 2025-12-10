<!--
TODO: 易瑜需要实现的页面组件清单
================================

核心页面组件 (高优先级):
TODO: LoginPage.vue - 用户登录页面
  - 用户名密码登录表单
  - 表单验证和错误提示
  - 记住密码功能
  - 响应式设计

TODO: DashboardPage.vue - 系统主仪表板
  - 统计数据卡片 (处方数、审核数、库存预警等)
  - 图表展示 (审核通过率、药品使用趋势)
  - 快捷操作入口
  - 权限控制显示

药品管理页面组件:
TODO: MedicineListPage.vue - 药品列表页面
  - 药品信息表格 (分页、排序、筛选)
  - 搜索功能 (按名称、分类等)
  - 新增药品按钮
  - 编辑删除操作

TODO: MedicineFormPage.vue - 药品表单页面
  - 新增药品表单
  - 编辑药品表单
  - 表单验证
  - 文件上传 (药品图片)

库存管理页面组件:
TODO: InventoryPage.vue - 库存管理页面
  - 当前库存列表
  - 库存预警提醒
  - 入库出库记录查询

TODO: StockInPage.vue - 药品入库页面
  - 入库单录入表单
  - 药品选择和数量输入
  - 批号和有效期录入
  - 供应商信息

TODO: StockOutPage.vue - 药品出库页面
  - 出库单录入表单
  - 出库原因选择
  - 药品选择和数量输入

处方管理页面组件 (最重要):
TODO: PrescriptionListPage.vue - 处方列表页面 ⭐⭐⭐
  - 处方查询列表 (分页、筛选)
  - 处方状态显示 (待审核/已审核/已驳回)
  - 快速搜索功能
  - 批量操作

TODO: PrescriptionFormPage.vue - 处方录入页面 ⭐⭐⭐
  - 患者信息录入表单
  - 药品明细添加 (动态增减药品行)
  - 用法用量录入
  - 实时审核状态显示
  - 处方保存和提交

TODO: PrescriptionDetailPage.vue - 处方详情页面
  - 处方信息展示
  - 审核结果显示
  - 审核意见查看
  - 打印功能

用户管理页面组件:
TODO: UserListPage.vue - 用户管理页面 (管理员)
  - 用户列表展示
  - 用户角色管理
  - 用户状态控制

系统设置页面组件:
TODO: SystemSettingsPage.vue - 系统设置页面
  - 系统参数配置
  - 审核规则设置
  - 用户权限配置

通用组件 (基础组件):
TODO: components/DataTable.vue - 数据表格组件
TODO: components/Pagination.vue - 分页组件
TODO: components/SearchBar.vue - 搜索栏组件
TODO: components/ModalDialog.vue - 模态对话框
TODO: components/FileUpload.vue - 文件上传组件

作者: 易瑜 (前端负责人)
-->
<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

#app {
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';
}
</style>

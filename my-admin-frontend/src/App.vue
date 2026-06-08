<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* 全局 Element Plus 弹窗移动端适配 */
@media (max-width: 768px) {
  .el-dialog {
    width: 92% !important;
    max-width: calc(100vw - 16px) !important;
    margin: 6vh auto !important;
    border-radius: 8px;
  }

  .el-dialog__wrapper {
    padding: 0 8px;
  }

  .el-dialog__header {
    padding: 14px 16px !important;
  }

  .el-dialog__headerbtn {
    top: 12px;
    right: 12px;
  }

  .el-dialog__body {
    padding: 16px !important;
    max-height: 70vh;
    overflow-y: auto;
    word-break: break-word;
  }

  .el-dialog__footer {
    padding: 12px 16px !important;
    text-align: center;
  }

  .el-message-box {
    width: 90% !important;
    max-width: 360px !important;
  }

  .el-table {
    font-size: 12px;
  }

  .el-form-item__label {
    width: 80px !important;
    text-align: left;
  }

  .el-form-item__content {
    margin-left: 80px !important;
  }

  .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
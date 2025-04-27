<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';
</script>
<template>
  <div class="flex content items-center flex-1">
    <img src="./images/logo.svg" class="w-45 h-45" />
    <div class="ml-3.25 text-content flex-1">
      <div>脚本是一系列用于验证软件系统、应用程序的自动化测试步骤和指令。通过脚本您可以自定义测试场景和用例，最终达到自动化测试和可重复持续测试的要求，或者通过脚本自定义Mock数据规则，生成满足业务或测试要求的数据。</div>
      <div class="mt-2">在这里您和您的团队可以完成以下工作：</div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>添加测试脚本并基于脚本发起功能测试、性能测试、稳定性测试以及自定义测试；</span>
      </div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>添加Mock脚本生成Mock数据，并将数据存储到：数据空间、数据源、本地执行节点、三方系统。</span>
      </div>
    </div>
  </div>
</template>

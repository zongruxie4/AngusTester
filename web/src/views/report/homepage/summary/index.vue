<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';
</script>
<template>
  <div class="flex content items-center flex-1">
    <img src="./images/logo.png" class="w-45 h-45" />
    <div class="ml-3.25 text-content flex-1">
      <div>通过“报告”帮助团队可以全面总结研发活动的各个方面，包括进度、效能、质量、执行情况、结果分析等，这有助于提高透明度和沟通，为项目的成功交付提供有力支持。</div>
      <div class="mt-2">在这里您和您的团队可以完成以下工作：</div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>根据报告模版在指定时间生成报告，并通过事件通知到相关人。</span>
      </div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>查看我添加的或者授权给我的报告及内容。</span>
      </div>
    </div>
  </div>
</template>

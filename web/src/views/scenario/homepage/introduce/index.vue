<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">欢迎进入Augus场景！</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>场景可以帮助您编排更加复杂的需求或用户故事，相比单个步骤的测试用例，场景可以包含更多步骤和流程。</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">在这里您和您的团队可以完成以下工作：</div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>在线编辑、调试场景；</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>添加并管理场景资源；</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>快速生成场景测试脚本和任务。</span>
      </div>
    </div>
  </div>
</template>

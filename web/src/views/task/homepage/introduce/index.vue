<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">欢迎进入Angus任务！</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>任务用于帮助团队管理和跟踪项目任务、问题和工作进展，提高团队协作和项目交付效率，确保项目按时完成并达到预期目标。</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">在这里您和您的团队可以完成以下工作：</div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>通过“迭代”组织规划团队成员开展项目活动和排期；</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>以明确负责人方式进行任务分配、确认和协作；</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>报告和分析项目的健康状况、进展情况和问题趋势。</span>
      </div>
    </div>
  </div>
</template>

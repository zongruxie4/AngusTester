<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold">欢迎来到AngusTester功能！</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>是一个功能测试管理模块，即用例库，用于验证软件系统是否符合规格说明和用户需求的过程。</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">在这里您和您的团队可以完成以下工作：</div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>通过“计划”组织测试人员开展功能测试活动；</span>
      </div>
      <!-- <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>在线编辑和调试测试用例；</span>
      </div> -->
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>组织用例评审及早发现潜在的缺陷和错误等；</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>报告和分析项目的健康状况、进展情况和问题趋势。</span>
      </div>
    </div>
  </div>
</template>

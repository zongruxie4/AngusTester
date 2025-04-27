<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">欢迎进入Angus接口！</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/1.png" />
      <div>接口可以帮助开发人员和测试人员轻松地添加、测试、调试和文档化API，最终帮助企业达成API资产化目的。</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">在这里您和您的团队可以完成以下工作：</div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>在线编辑、调试接口；</span>
      </div>
      <div class="flex items-start mb-1">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>管理项目、服务、接口以及测试任务；</span>
      </div>
      <div class="flex items-start">
        <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y-0.75" />
        <span>开展接口功能测试、性能测试和稳定性测试，保证交付稳定且满足性能指标要求的系统。</span>
      </div>
    </div>
  </div>
</template>

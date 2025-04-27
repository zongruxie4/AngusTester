<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

const DashedLine = defineAsyncComponent(() => import('./dashedLine.vue'));
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>目录</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">一、<em class="inline-block w-0.25 font-medium"></em>基本信息</span>
        <DashedLine />
      </a>
      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">二、<em class="inline-block w-0.25 font-medium"></em>人员信息</span>
        <DashedLine />
      </a>
      <a href="#a3" class="flex items-center space-x-2.5">
        <span class="h1">三、<em class="inline-block w-0.25 font-medium"></em>日期信息</span>
        <DashedLine />
      </a>
      <a href="#a4" class="flex items-center space-x-2.5">
        <span class="h1">四、<em class="inline-block w-0.25 font-medium"></em>前置条件</span>
        <DashedLine />
      </a>
      <a href="#a5" class="flex items-center space-x-2.5">
        <span class="h1">五、<em class="inline-block w-0.25 font-medium"></em>测试步骤</span>
        <DashedLine />
      </a>
      <a href="#a6" class="flex items-center space-x-2.5">
        <span class="h1">六、<em class="inline-block w-0.25 font-medium"></em>描述</span>
        <DashedLine />
      </a>
      <a href="#a7" class="flex items-center space-x-2.5">
        <span class="h1">七、<em class="inline-block w-0.25 font-medium"></em>评审信息</span>
        <DashedLine />
      </a>
      <a href="#a8" class="flex items-center space-x-2.5">
        <span class="h1">八、<em class="inline-block w-0.25 font-medium"></em>测试信息</span>
        <DashedLine />
      </a>
      <a href="#a9" class="flex items-center space-x-2.5">
        <span class="h1">九、<em class="inline-block w-0.25 font-medium"></em>关联任务</span>
        <DashedLine />
      </a>
      <a href="#a10" class="flex items-center space-x-2.5">
        <span class="h1">十、<em class="inline-block w-0.25 font-medium"></em>关联用例</span>
        <DashedLine />
      </a>
      <a href="#a11" class="flex items-center space-x-2.5">
        <span class="h1">十一、<em class="inline-block w-0.25 font-medium"></em>活动</span>
        <DashedLine />
      </a>
      <a href="#a12" class="flex items-center space-x-2.5">
        <span class="h1">十二、<em class="inline-block w-0.25 font-medium"></em>评论</span>
        <DashedLine />
      </a>
    </div>
    <div class="pdf-page-break"></div>
  </div>
</template>

<style scoped>
a {
  transition: all 300ms linear 0ms;
}

a:hover {
  color: #40a9ff;
}

.h1 {
  font-size: 14px;
  font-weight: 500;
}
</style>

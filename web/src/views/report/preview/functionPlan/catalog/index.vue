<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const DashedLine = defineAsyncComponent(() => import('./DashedLine.vue'));

const testers = computed(() => {
  const _testers = props.dataSource?.content?.cases?.testers;
  if (!_testers) {
    return [];
  }
  return Object.values(_testers);
});
</script>

<template>
  <div class="text-theme-title">
    <div class="font-medium text-4.5 mb-4">
      <span>目录</span>
      <div class="mt-1 rounded w-8.5 h-1 bg-gray-500"></div>
    </div>

    <div class="space-y-3.5">
      <a href="#a1" class="flex items-center space-x-2.5">
        <span class="h1">一、<em class="inline-block w-0.25 font-medium"></em>测试计划信息</span>
        <DashedLine />
      </a>
      <a href="#a1.1" class="flex items-center space-x-2.5">
        <span>1.1<em class="inline-block w-4.25 font-medium"></em>基本信息</span>
        <DashedLine />
      </a>
      <a href="#a1.2" class="flex items-center space-x-2.5">
        <span>1.2<em class="inline-block w-4.25 font-medium"></em>测试人与职责</span>
        <DashedLine />
      </a>
      <a href="#a1.3" class="flex items-center space-x-2.5">
        <span>1.3<em class="inline-block w-4.25 font-medium"></em>测试目标</span>
        <DashedLine />
      </a>
      <a href="#a1.4" class="flex items-center space-x-2.5">
        <span>1.4<em class="inline-block w-4.25 font-medium"></em>测试范围</span>
        <DashedLine />
      </a>
      <a href="#a1.5" class="flex items-center space-x-2.5">
        <span>1.5<em class="inline-block w-4.25 font-medium"></em>验收标准</span>
        <DashedLine />
      </a>
      <a href="#a1.6" class="flex items-center space-x-2.5">
        <span>1.6<em class="inline-block w-4.25 font-medium"></em>其他说明</span>
        <DashedLine />
      </a>

      <a href="#a2" class="flex items-center space-x-2.5">
        <span class="h1">二、<em class="inline-block w-0.25 font-medium"></em>用例汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a2.1" class="flex items-center space-x-2.5">
        <span>2.1<em class="inline-block w-4.25 font-medium"></em>测试汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a2.2" class="flex items-center space-x-2.5">
        <span>2.2<em class="inline-block w-4.25 font-medium"></em>评审汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a2.3" class="flex items-center space-x-2.5">
        <span>2.3<em class="inline-block w-4.25 font-medium"></em>燃尽图</span>
        <DashedLine />
      </a>
      <a href="#a2.4" class="flex items-center space-x-2.5">
        <span>2.4<em class="inline-block w-4.25 font-medium"></em>分组统计</span>
        <DashedLine />
      </a>

      <a href="#a3" class="flex items-center space-x-2.5">
        <span class="h1">三、<em class="inline-block w-0.25 font-medium"></em>测试人汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a3.1" class="flex items-center space-x-2.5">
        <span>3.1<em class="inline-block w-4.25 font-medium"></em>测试汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a3.2" class="flex items-center space-x-2.5">
        <span>3.2<em class="inline-block w-4.25 font-medium"></em>评审汇总结果</span>
        <DashedLine />
      </a>
      <a href="#a3.3" class="flex items-center space-x-2.5">
        <span>3.3<em class="inline-block w-4.25 font-medium"></em>燃尽图</span>
        <DashedLine />
      </a>
      <a href="#a3.4" class="flex items-center space-x-2.5">
        <span>3.4<em class="inline-block w-4.25 font-medium"></em>分组统计</span>
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

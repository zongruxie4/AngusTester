<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));
const DefaultPerform = defineAsyncComponent(() => import('./defaultPerf.vue'));
const DefaultStability = defineAsyncComponent(() => import('./defaultStability.vue'));
const DefaultFunc = defineAsyncComponent(() => import('./defaultFunction.vue'));
const DefaultEfficiency = defineAsyncComponent(() => import('./defaultEfficiency.vue'));

const headTitle = '测试指标是评估软件功能、性能和稳定性的量化值，也是判断软件功能、性能和稳定性及研发质量是否达标的依据。您可以通过“制定指标”设置平台默认认功能、性能和稳定性指标，如果希望接口/场景的指标比平台默认指标更加宽松，也可以在接口/场景对应的指标页面进行修改。';

</script>
<template>
  <div class="py-3.5 px-5 h-full overflow-auto">
    <HeadIntroduction :title="headTitle" />
    <DefaultFunc class="mt-3" />
    <DefaultPerform class="mt-10" />
    <DefaultStability class="mt-10" />
    <HeadIntroduction class="mt-10" title="效能指标用于项目、计划、迭代、组织人员工等工作效能等级标准设置和判定。" />
    <DefaultEfficiency class="mt-3 mb-10" />
  </div>
</template>

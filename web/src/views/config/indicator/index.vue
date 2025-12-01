<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));
const DefaultPerform = defineAsyncComponent(() => import('./DefaultPerf.vue'));
const DefaultStability = defineAsyncComponent(() => import('./DefaultStability.vue'));
// const DefaultFunc = defineAsyncComponent(() => import('./DefaultFunction.vue'));
const DefaultEfficiency = defineAsyncComponent(() => import('./DefaultEfficiency.vue'));
const DefaultEvaluation = defineAsyncComponent(() => import('./DefaultEvaluation.vue'));

const steps = [
  {
    title: '权重说明',
    content: '每个指标的权重范围为0-100，数值越大表示该指标越重要，权重应为100%。'
  },
  {
    title: '权重分配建议',
    content: '核心功能指标通常分配较高权重；\n根据项目类型调整安全和性能权重；\n合规性指标在特定行业可能权重较高；\n考虑用户实际使用场景分配易用性权重'
  }
];

</script>
<template>
  <div class="py-3.5 px-5 h-full overflow-auto">
    <HeadIntroduction :title="t('indicator.testHint')" />
    <!-- <DefaultFunc class="mt-3" />-->
    <DefaultPerform class="mt-5" />
    <DefaultStability class="mt-10" />
    <HeadIntroduction :steps="steps" class="mt-10 !text-3" />
    <DefaultEvaluation />
    <!-- <HeadIntroduction class="mt-10" :title="t('indicator.efficiencyHint')" />
    <DefaultEfficiency class="mt-3 mb-10" /> -->
  </div>
</template>

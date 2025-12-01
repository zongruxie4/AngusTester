<script lang="ts" setup>
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));
const DefaultPerform = defineAsyncComponent(() => import('./DefaultPerf.vue'));
const DefaultStability = defineAsyncComponent(() => import('./DefaultStability.vue'));
// const DefaultFunc = defineAsyncComponent(() => import('./DefaultFunction.vue'));
const DefaultEfficiency = defineAsyncComponent(() => import('./DefaultEfficiency.vue'));
const DefaultEvaluation = defineAsyncComponent(() => import('./DefaultEvaluation.vue'));

const steps = computed(() => [
  {
    icon: '',
    title: t('indicator.evaluation.steps.weightDescription.title'),
    content: t('indicator.evaluation.steps.weightDescription.content')
  },
  {
    icon: '',
    title: t('indicator.evaluation.steps.weightSuggestion.title'),
    content: t('indicator.evaluation.steps.weightSuggestion.content')
  }
]);

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

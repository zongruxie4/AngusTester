<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));
const DefaultPerform = defineAsyncComponent(() => import('./DefaultPerf.vue'));
const DefaultStability = defineAsyncComponent(() => import('./DefaultStability.vue'));
// const DefaultFunc = defineAsyncComponent(() => import('./DefaultFunction.vue'));
const DefaultEfficiency = defineAsyncComponent(() => import('./DefaultEfficiency.vue'));
</script>
<template>
  <div class="py-3.5 px-5 h-full overflow-auto">
    <HeadIntroduction :title="t('indicator.titleTip')" />
    <!-- <DefaultFunc class="mt-3" />-->
    <DefaultPerform class="mt-5" />
    <DefaultStability class="mt-10" />
    <HeadIntroduction class="mt-10" :title="t('indicator.efficiencyTitle')" />
    <DefaultEfficiency class="mt-3 mb-10" />
  </div>
</template>

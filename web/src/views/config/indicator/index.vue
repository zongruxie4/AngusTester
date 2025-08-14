<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const HeadIntroduction = defineAsyncComponent(() => import('@/components/layout/header/introduce/index.vue'));
const DefaultPerform = defineAsyncComponent(() => import('./defaultPerf.vue'));
const DefaultStability = defineAsyncComponent(() => import('./defaultStability.vue'));
const DefaultFunc = defineAsyncComponent(() => import('./defaultFunction.vue'));
const DefaultEfficiency = defineAsyncComponent(() => import('./defaultEfficiency.vue'));

const headTitle = t('indicator.title');

</script>
<template>
  <div class="py-3.5 px-5 h-full overflow-auto">
    <HeadIntroduction :title="headTitle" />
    <DefaultFunc class="mt-3" />
    <DefaultPerform class="mt-10" />
    <DefaultStability class="mt-10" />
    <HeadIntroduction class="mt-10" :title="t('indicator.efficiencyTitle')" />
    <DefaultEfficiency class="mt-3 mb-10" />
  </div>
</template>

<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { test } from '@/api/tester';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

// Async Components
const BaselineCaseList = defineAsyncComponent(() => import('@/views/test/baseline/case/list/List.vue'));
const BaselineCompare = defineAsyncComponent(() => import('@/views/test/baseline/case/compare/index.vue'));

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Injected Dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

// Reactive Data
const currentPlanId = ref();

/**
 * Load baseline information and update tab pane
 */
const loadBaselineInfo = async () => {
  const [error, res] = await test.getBaselineDetail(props.data?.id as string);
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }

  const { name } = data;
  currentPlanId.value = data.planId;

  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: props.data?.id + '-case' });
  }
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  if (props.data?.id) {
    loadBaselineInfo();
  }
});
</script>
<template>
  <div class="px-5 pt-3 h-full">
    <Tabs
      size="small"
      class="h-full"
      destroyInactiveTabPane>
      <TabPane key="baselinecase" :tab="t('testCaseBaseline.case.baselineCases')">
        <BaselineCaseList
          v-bind="props"
          :baselineId="props.data?.id"
          :planId="currentPlanId" />
      </TabPane>
      <TabPane key="baselineCompare" :tab="t('testCaseBaseline.case.baselineCompare')">
        <BaselineCompare
          v-bind="props"
          :baselineId="props.data?.id"
          :planId="currentPlanId" />
      </TabPane>
    </Tabs>
  </div>
</template>

<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { func } from '@/api/tester';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

const BaseLineCaseList = defineAsyncComponent(() => import('@/views/function/baseline/case/list/List.vue'));
const BaselineCompare = defineAsyncComponent(() => import('@/views/function/baseline/case/compare/index.vue'));

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

const planId = ref();

const loadBaseLineInfo = async () => {
  const [error, res] = await func.getBaselineDetail(props.data.id);
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }

  const { name } = data;
  planId.value = data.planId;

  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: props.data.id + '-case' });
  }
};

onMounted(() => {
  if (props.data.id) {
    loadBaseLineInfo();
  }
});
</script>
<template>
  <div class="px-5 pt-3 h-full">
    <Tabs
      size="small"
      class="h-full"
      destroyInactiveTabPane>
      <TabPane key="baselinecase" :tab="t('functionBaseline.case.baselineCases')">
        <BaseLineCaseList
          v-bind="props"
          :baselineId="props.data.id"
          :planId="planId" />
      </TabPane>
      <TabPane key="baselinecompare" :tab="t('functionBaseline.case.baselineCompare')">
        <BaselineCompare
          v-bind="props"
          :baselineId="props.data.id"
          :planId="planId" />
      </TabPane>
      <template>
      </template>
    </Tabs>
  </div>
</template>

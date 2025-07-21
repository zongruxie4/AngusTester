<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { func } from '@/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const BaseLineCaseList = defineAsyncComponent(() => import('@/views/function/baseline/case/list/index.vue'));
const BaselineCompare = defineAsyncComponent(() => import('@/views/function/baseline/case/compare/index.vue'));

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
      <TabPane key="baselinecase" tab="基线用例">
        <BaseLineCaseList
          v-bind="props"
          :baselineId="props.data.id"
          :planId="planId" />
      </TabPane>
      <TabPane key="baselinecompare" tab="基线对比">
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

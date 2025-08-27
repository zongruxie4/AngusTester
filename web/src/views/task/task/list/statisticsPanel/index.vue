<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { analysis } from '@/api/tester';

import { ResourceInfo } from '../PropsType';

type Props = {
  collapse: boolean;// 展开、折叠
  params: { filters?: { key: string; op: string; value: boolean | string | string[] }[] };
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  moduleId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  params: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:loading', value: boolean): void;
}>();

const Count = defineAsyncComponent(() => import('./count.vue'));
const Chart = defineAsyncComponent(() => import('@/views/task/task/list/statisticsPanel/chart/index.vue'));

const UUID = utils.uuid();

const dataSource = ref<ResourceInfo>({
  actualWorkload: '0',
  apiTestNum: '0',
  bugNum: '0',
  canceledNum: '0',
  completedNum: '0',
  completedWorkload: '0',
  confirmingNum: '0',
  evalWorkload: '0',
  functionalNum: '0',
  inProgressNum: '0',
  oneTimePassedNum: '0',
  oneTimePassedRate: '0',
  overdueNum: '0',
  pendingNum: '0',
  perfNum: '0',
  processFailTimes: '0',
  processTimes: '0',
  requirementNum: '0',
  scenarioTestNum: '0',
  stabilityNum: '0',
  storyNum: '0',
  taskNum: '0',
  testFailNum: '0',
  testSuccessNum: '0',
  totalStatusNum: '0',
  totalTaskNum: '0',
  totalTaskTypeNum: '0',
  totalTestTypeNum: '0',
  validTaskNum: '0',
  progress: '0'
});

const loadData = async () => {
  emit('update:loading', true);
  let params: {
    projectId: string;
    filters?: { key: string; op: string; value: boolean | string | string[] }[];
  } = {
    projectId: props.projectId
  };

  if (props.params) {
    params = { ...params, ...props.params };
  }

  const [error, res] = await analysis.getTaskCount(params);
  emit('update:loading', false);
  if (error) {
    return;
  }

  if (res?.data) {
    dataSource.value = res?.data;
  }
};

onMounted(() => {
  watch(() => props.params, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: false });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });
});

const activeKey = computed(() => {
  if (props.collapse) {
    return '';
  }

  return UUID;
});
</script>

<template>
  <Collapse
    v-model:activeKey="activeKey"
    ghost
    collapsible="disabled">
    <CollapsePanel :key="UUID" :showArrow="false">
      <Count :dataSource="dataSource" style="max-width: 1100px;" />
      <Chart :dataSource="dataSource" />
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse :deep(.ant-collapse-item) .ant-collapse-header {
  display: none;
}

.ant-collapse :deep(.ant-collapse-content) .ant-collapse-content-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}
</style>

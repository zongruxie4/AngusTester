<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { analysis } from '@/api/tester';

import { SummaryInfo } from '@/views/task/home/types';

// Props definition
type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

// Lazy-loaded child components
const SprintCount = defineAsyncComponent(() => import('@/views/task/home/summary/SprintCount.vue'));
const TaskCount = defineAsyncComponent(() => import('@/views/task/home/summary/TaskCount.vue'));
const BacklogPie = defineAsyncComponent(() => import('@/views/task/home/summary/BacklogPie.vue'));

const loading = ref(false);
// Data source for all summary widgets
const summaryData = ref<SummaryInfo>();

const loadData = async (): Promise<void> => {
  loading.value = true;
  const params = {
    projectId: props.projectId
  };

  const [error, res] = await analysis.getTaskResourceCount(params);
  loading.value = false;
  if (error) {
    reset();
    return;
  }

  if (res?.data) {
    summaryData.value = res.data;
  } else {
    reset();
  }
};

const reset = () => {
  summaryData.value = {
    allSprint: '0',
    sprintByLastWeek: '0',
    sprintByLastMonth: '0',
    allTask: '0',
    taskByLastWeek: '0',
    taskByLastMonth: '0',
    taskByOverdue: '0',
    allTag: '0',
    tagByLastWeek: '0',
    tagByLastMonth: '0',
    allBacklog: '0',
    backlogByType: {
      STORY: '0',
      REQUIREMENT: '0',
      TASK: '0',
      BUG: '0',
      API_TEST: '0',
      SCENARIO_TEST: '0'
    },
    sprintByStatus: {
      PENDING: '0',
      IN_PROGRESS: '0',
      COMPLETED: '0',
      BLOCKED: '0'
    },
    taskByType: {
      STORY: '0',
      REQUIREMENT: '0',
      TASK: '0',
      BUG: '0',
      API_TEST: '0',
      SCENARIO_TEST: '0'
    },
    taskByStatus: {
      PENDING: '0',
      IN_PROGRESS: '0',
      CONFIRMING: '0',
      COMPLETED: '0',
      CANCELED: '0'
    },
    taskByPriority: {
      HIGHEST: '0',
      HIGH: '0',
      MEDIUM: '0',
      LOW: '0',
      LOWEST: '0'
    }
  };
};

onMounted(() => {
  watch(() => props.projectId, () => {
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });
});
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('taskHome.resourceStatistics') }}</div>
    <div class="flex">
      <BacklogPie :dataSource="summaryData" class="w-1/4-media mr-media" />
      <SprintCount :dataSource="summaryData" class="w-1/4-media mr-media" />
      <TaskCount :dataSource="summaryData" class="w-1/2-media " />
    </div>
  </div>
</template>

<style scoped>
.w-1\/4-media {
  width: calc(25% - 10px);
}

.w-1\/2-media {
  width: calc(50% - 10px);
}

.mr-media {
  margin-right: 15px;
}
</style>

<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Icon, Spin } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { software } from '@/api/tester';

import { VersionInfo } from './types';
import Chart from './Chart.vue';

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

const { t } = useI18n();

const refreshNotify = ref('');
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const TaskTable = defineAsyncComponent(() => import('@/views/project/version/MyTask.vue'));

const dataSource = ref<VersionInfo>({});

const chartValue = ref({});

const loading = ref(false);
const loadVersionData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await software.getSoftwareVersionDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as VersionInfo;
  if (!data) {
    return;
  }

  dataSource.value = {
    ...data
  };

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-detail' });
  }

  const progress = data.progress || {};
  const { completedNum = 0, completedRate = 0, completedWorkload = 0, evalWorkload = 0, totalNum = 0 } = progress;

  chartValue.value = {
    chart1Value: {
      title: completedRate + '%',
      value: [{ value: totalNum - completedNum }, { value: completedNum }]
    },
    chart2Value: {
      title: +evalWorkload > 0 ? ((completedWorkload / evalWorkload).toFixed(2) + '%') : '0%',
      value: [{ value: evalWorkload - completedWorkload }, { value: completedWorkload }]
    }
  };
};

onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }
    await loadVersionData(id);
  }, { immediate: true });
});

const statusColorConfig = {
  ARCHIVED: 'default',
  NOT_RELEASED: 'processing',
  RELEASED: 'success'
};

</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex itesm-center space-x-3">
      <div class="text-theme-title text-5">
        {{ t('taskVersion.detail.version') }} {{ dataSource.name }}
      </div>
      <Tag v-if="dataSource.status?.value" :color="statusColorConfig[dataSource.status?.value]">{{ dataSource.status?.message }}</Tag>
    </div>

    <div class="flex itesm-center space-x-5 mt-2">
      <div class="inline-flex items-center space-x-1">
        <template v-if="dataSource.startDate">
          {{ t('taskVersion.detail.startDate') }} {{ dataSource.startDate }}
        </template>
        <template v-else>
          <Icon icon="icon-riqi" /> <span>{{ t('taskVersion.detail.noStartDate') }}</span>
        </template>
      </div>
      <div class="inline-flex items-center space-x-1">
        <template v-if="dataSource.releaseDate">
          {{ t('taskVersion.detail.releaseDate') }} {{ dataSource.releaseDate }}
        </template>
        <template v-else>
          <Icon icon="icon-riqi" /> <span>{{ t('taskVersion.detail.noReleaseDate') }}</span>
        </template>
      </div>
    </div>

    <Chart v-bind="chartValue" class="w-200" />
    <TaskTable
      :projectId="props.projectId"
      :userInfo="props.userInfo"
      :notify="refreshNotify"
      :versionInfo="dataSource" />
  </Spin>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

.version-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
}
</style>

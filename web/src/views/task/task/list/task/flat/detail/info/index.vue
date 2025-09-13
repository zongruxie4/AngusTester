<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { TaskType } from '@/enums/enums';

import { TaskInfo } from '@/views/task/types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  largePageLayout: boolean;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  largePageLayout: undefined,
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const APIBasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Apis.vue'));
const ScenarioBasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Scenario.vue'));
const BasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Basic.vue'));
const Description = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Description.vue'));
const PersonnelInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Date.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/detail/info/Attachment.vue'));

const change = (data: Partial<TaskInfo>) => {
  emit('change', data);
};

const loadingChange = (value: boolean) => {
  emit('update:loading', value);
};

const taskId = computed(() => {
  return props.dataSource?.id;
});

const taskType = computed(() => {
  return props.dataSource?.taskType?.value;
});

const className = computed(() => {
  if (props.largePageLayout) {
    return 'large-page-layout';
  }

  if (!props.largePageLayout) {
    return 'small-page-layout';
  }
  return '';
});
</script>

<template>
  <div :class="className" class="h-full pr-5 overflow-auto">
    <div class="flex-1 space-y-4">
      <APIBasicInfo
        v-if="taskType === TaskType.API_TEST"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />

      <ScenarioBasicInfo
        v-else-if="taskType === TaskType.SCENARIO_TEST"
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />

      <BasicInfo
        v-else
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />

      <Description
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />
    </div>

    <div class="flex-shrink-0 w-75 space-y-4">
      <PersonnelInfo
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />

      <DateInfo
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />

      <AttachmentInfo
        :dataSource="props.dataSource"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskId="taskId"
        @change="change"
        @loadingChange="loadingChange" />
    </div>
  </div>
</template>

<style scoped>
.large-page-layout {
  display: flex;
  align-items: flex-start;
}

.large-page-layout>div+div {
  margin-left: 20px;
}

.small-page-layout>div+div {
  margin-top: 16px;
}
</style>

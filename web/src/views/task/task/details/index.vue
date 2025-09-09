<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { AsyncComponent } from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';

import { TaskInfo } from '../../types';

type Props = {
  sprintId: string;
  sprintName: string;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  uiKey: string; // tabPane的uiKey
  activeKey: string;
  data: { id: string; query: string; };// 列表的查询参数，用于上一条、下一条的查询条件
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  sprintName: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  uiKey: undefined, // tabPane的uiKey
  activeKey: undefined,
  data: undefined
});

const Details = defineAsyncComponent(() => import('@/views/task/task/list/task/detail/view/index.vue'));
const EditTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/Edit.vue'));
const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/Move.vue'));

const getTabPane = inject<(key: string) => ({ data: { query: string; } }[])>('getTabPane', () => []);
const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');

const route = useRoute();
const router = useRouter();
const linkUrl = ref(route.fullPath);

const refreshNotify = ref<string>();

const selectedTaskName = ref<string>();
const selectedTaskSprintId = ref<string>();
const selectedTaskId = ref<string>();
const editTaskData = ref<TaskInfo>();
const taskModalVisible = ref(false);
const moveModalVisible = ref(false);

const initialize = () => {
  router.replace('/task#task');

  const id = props.data?.id;
  // 获取保存的查询条件
  const tabPaneDataList = getTabPane(id);
  if (tabPaneDataList?.length) {
    const item = tabPaneDataList[0];
    const query = item.data?.query;
    if (query) {
      linkUrl.value = route.path + '?' + query;
    }
  }
};

const deleteOk = (id: string) => {
  if (typeof deleteTabPane === 'function') {
    deleteTabPane([id]);
  }
};

const toMove = (data: TaskInfo) => {
  selectedTaskSprintId.value = data.sprintId;
  selectedTaskName.value = data.name;
  selectedTaskId.value = data.id;
  moveModalVisible.value = true;
};

const toEdit = () => {
  taskModalVisible.value = true;
};

const editOk = (data: TaskInfo) => {
  editTaskData.value = data;
};

onMounted(() => {
  initialize();

  watch(() => props.notify, (newValue) => {
    if ((newValue === undefined || newValue === null || newValue === '') || (props.activeKey && props.uiKey !== props.activeKey)) {
      return;
    }

    refreshNotify.value = newValue;
  }, { immediate: true });

  // watch(() => props.activeKey, (newValue) => {
  //   if (!newValue || props.uiKey !== newValue) {
  //     return;
  //   }

  //   refreshNotify.value = utils.uuid();
  // }, { immediate: true });
});

const taskId = computed(() => {
  return props.data?.id;
});
</script>

<template>
  <Details
    v-bind="props.data"
    :notify="refreshNotify"
    :userInfo="props.userInfo"
    :appInfo="props.appInfo"
    :projectId="props.projectId"
    :sprintId="props.sprintId"
    :sprintName="props.sprintName"
    :editTaskData="editTaskData"
    :linkUrl="linkUrl"
    type="details"
    @edit="toEdit"
    @move="toMove"
    @delete="deleteOk" />

  <AsyncComponent :visible="taskModalVisible">
    <EditTaskModal
      v-model:visible="taskModalVisible"
      :taskId="taskId"
      :sprintId="props.sprintId"
      :projectId="props.projectId"
      :userInfo="props.userInfo"
      :appInfo="props.appInfo"
      @ok="editOk" />
  </AsyncComponent>

  <AsyncComponent :visible="moveModalVisible">
    <MoveTaskModal
      v-model:visible="moveModalVisible"
      :sprintId="selectedTaskSprintId"
      :taskId="selectedTaskId"
      :taskName="selectedTaskName"
      :projectId="props.projectId" />
  </AsyncComponent>
</template>

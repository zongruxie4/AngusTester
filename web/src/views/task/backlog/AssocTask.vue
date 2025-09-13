<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, IconTask, NoData, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskInfo } from '../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Reactive State Variables
const isTaskEditing = ref(false);
const selectedTaskIds = ref<string[]>([]);

// Computed Properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

const associatedTaskList = computed(() => {
  return props.dataSource?.refTaskInfos?.map(item => {
    return {
      ...item,
      linkUrl: `/task#task?taskId=${item.id}`
    };
  }) || [];
});

const associatedTaskIds = computed(() => {
  return associatedTaskList.value.map(item => item.id);
});

// Task Association Management Functions
/**
 * <p>Initialize task association editing mode</p>
 * <p>Enables editing state for task associations</p>
 */
const startTaskAssociationEditing = () => {
  isTaskEditing.value = true;
};

/**
 * <p>Cancel task association editing</p>
 * <p>Exits editing mode without saving changes</p>
 */
const cancelTaskAssociationEditing = () => {
  isTaskEditing.value = false;
};

/**
 * <p>Confirm task association changes</p>
 * <p>Saves the selected task associations and updates the task</p>
 */
const confirmTaskAssociationChanges = async () => {
  const updateParams = {
    refTaskIds: selectedTaskIds.value
  };
  isTaskEditing.value = false;
  const [error] = await task.updateTask(currentTaskId.value, updateParams);
  if (error) {
    return;
  }

  const updatedTaskData = await fetchTaskDetails();
  emit('change', updatedTaskData);
};

/**
 * <p>Handle task selection change</p>
 * <p>Updates the selected task IDs when user selects new tasks</p>
 */
const handleTaskSelectionChange = (ids: any) => {
  selectedTaskIds.value = ids;
};

// Data Loading Functions
/**
 * <p>Load task details</p>
 * <p>Fetches complete task information from the server</p>
 */
const fetchTaskDetails = async (): Promise<Partial<TaskInfo>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(currentTaskId.value);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id: currentTaskId.value };
  }
  return res.data;
};
</script>

<template>
  <div class="h-full text-3 leading-5 px-5 overflow-y-auto">
    <div class="flex items-center text-theme-title mb-2.5">
      <span class="font-semibold">{{ t('backlog.assocTask.title') }}</span>
      <Button
        v-show="!isTaskEditing"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="startTaskAssociationEditing">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <template v-if="!isTaskEditing">
      <div v-if="associatedTaskList.length" class="w-full space-y-1.5 truncate">
        <RouterLink
          v-for="item in associatedTaskList"
          :key="item.id"
          :to="item.linkUrl"
          target="_blank"
          class="flex items-center overflow-hidden">
          <IconTask :value="item.taskType?.value" />
          <span class="truncate ml-1">{{ item.name }}</span>
        </RouterLink>
      </div>

      <NoData
        v-else
        size="small"
        style="height: calc(100% - 30px);" />
    </template>

    <template v-else>
      <Select
        :value="associatedTaskIds"
        showSearch
        internal
        allowClear
        :fieldNames="{ label: 'name', value: 'id' }"
        :maxTagCount="10"
        :maxTagTextLength="15"
        :maxTags="20"
        :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`"
        class="w-full"
        :placeholder="t('backlog.assocTask.placeholder')"
        mode="multiple"
        @change="handleTaskSelectionChange">
        <template #option="record">
          <div class="flex items-center leading-4.5 overflow-hidden">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <div class="link truncate ml-1" :title="record.name">
              {{ record.name }}
            </div>
            <div
              v-if="record.overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
              style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu">{{ t('backlog.assocTask.overdue') }}</span>
            </div>
          </div>
        </template>
      </Select>

      <div class="flex items-center space-x-2.5 mt-2.5 justify-end">
        <Button
          type="default"
          size="small"
          @click="cancelTaskAssociationEditing">
          {{ t('backlog.assocTask.cancel') }}
        </Button>
        <Button
          type="primary"
          size="small"
          @click="confirmTaskAssociationChanges">
          {{ t('backlog.assocTask.confirm') }}
        </Button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import {
  Colon, DatePicker, Icon, IconRequired, IconTask, Input, Modal, notification,
  SelectUser, Spin, TaskPriority, Tooltip
} from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { duration, TESTER, utils, EvalWorkloadMethod } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { ai } from '@/api/gm';
import { task } from '@/api/tester';
import { TaskType } from '@/enums/enums';

import { TaskInfo } from '../types';
import { TIME_FORMAT } from '@/utils/constant';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

import SelectEnum from '@/components/enum/SelectEnum.vue';

const { t } = useI18n();

const props = withDefaults(defineProps<TaskInfoProps>(), {
  visible: false,
  projectId: undefined,
  taskInfo: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok'): void;
}>();

const MAX_LENGTH = 200;

const aiEnabled = inject('aiEnabled', ref(false));

const confirmLoading = ref(false);
const generating = ref(false);

const idList = ref<string[]>([]);
const dataMap = ref<{
  [key: string]: {
    testType: TaskInfo['testType']['value'];
    taskType: TaskInfo['taskType']['value'];
    priority: TaskInfo['priority']['value'];
    name: string;
    evalWorkload: string;
    assigneeId: string;
    confirmerId: string;
    deadlineDate: string;
    projectId: string;
    sprintId: string;
    moduleId?: string;
    parentTaskId?: string;
  }
}>({});

const taskTypeErrorSet = ref(new Set<string>());
const priorityErrorSet = ref(new Set<string>());
const nameErrorSet = ref(new Set<string>());
const assigneeIdErrorSet = ref(new Set<string>());
const deadlineDateErrorSet = ref(new Set<string>());
const nameRepeatSet = ref(new Set<string>());

const aiSplitFlag = ref(false);
const aiKeywords = ref('');

const toAISplit = () => {
  aiSplitFlag.value = true;
  aiKeywords.value = t('backlog.splitTask.aiKeywords', { taskName: props.taskInfo?.name });
};

const toGenerate = async () => {
  generating.value = true;
  const [error, res] = await ai.getChartResult({ type: 'SPLIT_SUB_TASK', question: aiKeywords.value });
  generating.value = false;
  if (error) {
    return;
  }

  const data = (res?.data || { normal: '', content: [] }) as {
    normal:string;
    content:string[];
  };

  const taskInfo = props.taskInfo || {};
  const list = data.content;
  const newIdList:string[] = [];
  const newDataMap = {};
  for (let i = 0, len = list.length; i < len; i++) {
    const id = utils.uuid();
    newIdList.push(id);
    newDataMap[id] = {
      name: list[i],
      assigneeId: taskInfo.assigneeId,
      confirmerId: taskInfo.confirmerId,
      deadlineDate: taskInfo.deadlineDate,
      evalWorkload: '1',
      moduleId: taskInfo.moduleId,
      parentTaskId: taskInfo.id,
      priority: taskInfo.priority?.value,
      projectId: taskInfo.projectId,
      sprintId: taskInfo.sprintId,
      taskType: TaskType.TASK,
      testType: taskInfo.testType?.value
    };
  }

  idList.value = newIdList;
  dataMap.value = newDataMap;
};

const toCancelAISplit = () => {
  aiSplitFlag.value = false;
  aiKeywords.value = '';
};

const taskTypeChange = (id: string) => {
  taskTypeErrorSet.value.delete(id);
};

const priorityChange = (id: string) => {
  priorityErrorSet.value.delete(id);
};

const nameChange = debounce(duration.search, () => {
  validateRepeatName();
});

const checkboxChange = (id: string) => {
  if (dataMap.value[id].parentTaskId) {
    dataMap.value[id].parentTaskId = undefined;
    return;
  }
  dataMap.value[id].parentTaskId = taskId.value;
};

const assigneeIdChange = (id: string) => {
  assigneeIdErrorSet.value.delete(id);
};

const deadlineDateChange = (id: string) => {
  deadlineDateErrorSet.value.delete(id);
};

const toAdd = () => {
  const newId = utils.uuid();
  idList.value.push(newId);

  const data = props.taskInfo;
  dataMap.value[newId] = {
    assigneeId: data.assigneeId,
    confirmerId: data.confirmerId,
    deadlineDate: data.deadlineDate,
    evalWorkload: '1',
    moduleId: data.moduleId,
    parentTaskId: data.id,
    priority: data.priority?.value,
    projectId: data.projectId,
    sprintId: data.sprintId,
    taskType: TaskType.TASK,
    testType: data.testType?.value,
    name: ''
  };
};

const toDelete = (id: string, index: number) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  taskTypeErrorSet.value.delete(id);
  priorityErrorSet.value.delete(id);
  nameErrorSet.value.delete(id);
  assigneeIdErrorSet.value.delete(id);
  deadlineDateErrorSet.value.delete(id);
};

const cancel = () => {
  reset();
  emit('update:visible', false);
};

const ok = async () => {
  if (idList.value?.length > MAX_LENGTH) {
    notification.warning(t('backlog.splitTask.messages.maxSplitWarning'));
    return;
  }

  if (!toValidate()) {
    return;
  }

  const ids = idList.value;
  const map = dataMap.value;
  confirmLoading.value = true;
  let errorNum = 0;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const params = map[id];
    for (const key in params) {
      if (key === 'moduleId') {
        if (!params[key] || params[key] === '-1') {
          delete params[key];
        }
      } else if (!params[key]) {
        delete params[key];
      }
    }

    const [error] = await task.addTask(params);
    if (error) {
      errorNum++;
    }
  }

  confirmLoading.value = false;
  if (errorNum === 0) {
    reset();
    emit('update:visible', false);
    emit('ok');
  }
};

const resetError = () => {
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  assigneeIdErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  nameRepeatSet.value.clear();
};

const getRepeatNames = () => {
  const uniqueNames = new Set();
  const repeatNames = new Set();
  const names = Object.values(dataMap.value).map(item => item.name);
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (name) {
      if (uniqueNames.has(name)) {
        repeatNames.add(name);
      } else {
        uniqueNames.add(name);
      }
    }
  }

  return repeatNames;
};

const validateRepeatName = () => {
  const ids = idList.value;
  const repeatNameSet = getRepeatNames();
  const map = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const item = map[id];
    if (item.name && repeatNameSet.has(item.name)) {
      nameRepeatSet.value.add(id);
      nameErrorSet.value.add(id);
    } else {
      nameRepeatSet.value.delete(id);
      nameErrorSet.value.delete(id);
    }
  }
};

const toValidate = () => {
  const ids = idList.value;
  const map = dataMap.value;
  const repeatNameSet = getRepeatNames();
  resetError();
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const item = map[id];
    if (!item.taskType) {
      taskTypeErrorSet.value.add(id);
    }

    if (!item.priority) {
      priorityErrorSet.value.add(id);
    }

    if (!item.name) {
      nameErrorSet.value.add(id);
    } else {
      if (repeatNameSet.has(item.name)) {
        nameRepeatSet.value.add(id);
        nameErrorSet.value.add(id);
      }
    }

    if (!item.assigneeId) {
      assigneeIdErrorSet.value.add(id);
    }

    if (!item.deadlineDate) {
      deadlineDateErrorSet.value.add(id);
    } else {
      if (dayjs(item.deadlineDate).isBefore(dayjs())) {
        deadlineDateErrorSet.value.add(id);
      }
    }
  }

  return !(taskTypeErrorSet.value.size +
    priorityErrorSet.value.size +
    nameErrorSet.value.size +
    assigneeIdErrorSet.value.size +
    deadlineDateErrorSet.value.size);
};

const disabledDate = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

const reset = () => {
  aiKeywords.value = '';
  aiSplitFlag.value = false;
  idList.value = [];
  dataMap.value = {};
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  assigneeIdErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  resetError();
};

onMounted(() => {
  watch(() => props.taskInfo, (newValue) => {
    if (!newValue) {
      return;
    }

    const newId = utils.uuid();
    idList.value = [newId];
    dataMap.value[newId] = {
      assigneeId: newValue.assigneeId,
      confirmerId: newValue.confirmerId,
      deadlineDate: newValue.deadlineDate,
      evalWorkload: '1',
      moduleId: newValue.moduleId,
      parentTaskId: newValue.id,
      priority: newValue.priority?.value,
      projectId: newValue.projectId,
      sprintId: newValue.sprintId,
      taskType: TaskType.TASK,
      testType: newValue.testType?.value,
      name: ''
    };
  }, { immediate: true, deep: true });
});

const taskId = computed(() => {
  return props.taskInfo?.id;
});

const okButtonProps = computed(() => {
  return {
    disabled: generating.value
  };
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="1200"
    :confirmLoading="confirmLoading"
    :okButtonProps="okButtonProps"
    :title="t('backlog.splitTask.title')"
    @cancel="cancel"
    @ok="ok">
    <Spin :spinning="generating" :tip="t('backlog.splitTask.splitting')">
      <div class="flex flex-nowrap justify-between mb-3.5 space-x-5">
        <template v-if="!aiSplitFlag">
          <div class="flex items-start font-semibold leading-4.5 pt-1.5">
            <div class="flex-shrink-0 flex items-center mr-1.5">
              <span>{{ t('backlog.splitTask.splitTask') }}</span>
              <Colon />
            </div>
            <div>{{ props.taskInfo?.name }}</div>
          </div>

          <Button
            v-if="aiEnabled"
            :disabled="idList?.length>=MAX_LENGTH"
            type="primary"
            size="small"
            class="flex items-center space-x-1"
            ghost
            @click="toAISplit">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('backlog.splitTask.aiSplit') }}</span>
          </Button>
        </template>

        <template v-else-if="aiEnabled">
          <Input
            v-model:value="aiKeywords"
            :placeholder="t('backlog.splitTask.aiPlaceholder', { taskName: props.taskInfo?.name })"
            trim
            allowClear
            class="flex-1"
            :maxlength="2000" />

          <div class="flex items-center space-x-2.5">
            <Button
              :disabled="!aiKeywords||idList?.length>=MAX_LENGTH"
              type="primary"
              size="small"
              @click="toGenerate">
              {{ t('backlog.splitTask.aiSplit') }}
            </Button>
            <Button
              type="default"
              size="small"
              @click="toCancelAISplit">
              {{ t('backlog.splitTask.cancel') }}
            </Button>
          </div>
        </template>
      </div>

      <div class="head-container flex items-center space-x-2.5 mb-1.5 px-2 rounded">
        <div class="w-27 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.splitTask.headers.type') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.splitTask.headers.priority') }}</span>
        </div>

        <div class="flex-1 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.splitTask.headers.name') }}</span>
        </div>

        <div class="w-9 space-x-0.5 head-item-container">
          <span>{{ t('backlog.splitTask.headers.subTask') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <span>
            {{
              props.taskInfo?.evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                ? t('backlog.splitTask.headers.evalStoryPoint')
                : t('backlog.splitTask.headers.evalWorkload')
            }}
          </span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.splitTask.headers.assignee') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <span>{{ t('backlog.splitTask.headers.confirmer') }}</span>
        </div>

        <div class="w-42 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.splitTask.headers.deadline') }}</span>
        </div>

        <div class="w-5 h-5"></div>
      </div>

      <div style="max-height: 320px;" class="space-y-2 overflow-auto">
        <div
          v-for="(item, index) in idList"
          :key="item"
          class="action-container flex items-center px-2">
          <SelectEnum
            v-model:value="dataMap[item].taskType"
            :error="taskTypeErrorSet.has(item)"
            enumKey="TaskType"
            :placeholder="t('backlog.splitTask.placeholders.taskType')"
            class="w-27 mr-2.5"
            @change="taskTypeChange(item)">
            <template #option="record">
              <div class="flex items-center">
                <IconTask :value="record.value" class="text-4 flex-shrink-0" />
                <span class="ml-1">{{ record.message }}</span>
              </div>
            </template>
          </SelectEnum>

          <SelectEnum
            v-model:value="dataMap[item].priority"
            :error="priorityErrorSet.has(item)"
            enumKey="Priority"
            :placeholder="t('backlog.splitTask.placeholders.priority')"
            class="w-20 mr-2.5"
            @change="priorityChange(item)">
            <template #option="record">
              <TaskPriority :value="record" />
            </template>
          </SelectEnum>

          <Tooltip
            :title="t('backlog.splitTask.tooltips.nameRepeat')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="nameRepeatSet.has(item)">
            <Input
              v-model:value="dataMap[item].name"
              :error="nameErrorSet.has(item)"
              :maxlength="200"
              trim
              class="flex-1 mr-2.5"
              :placeholder="t('backlog.splitTask.placeholders.taskName')"
              @change="nameChange($event,item)" />
          </Tooltip>

          <div class="w-9 flex justify-center items-center mr-2.5">
            <Checkbox
              :checked="dataMap[item].parentTaskId === taskId"
              @change="checkboxChange(item)" />
          </div>

          <Input
            v-model:value="dataMap[item].evalWorkload"
            class="w-20 mr-2.5"
            size="small"
            dataType="float"
            trimAll
            :min="0.1"
            :max="1000"
            :placeholder="t('backlog.splitTask.placeholders.workloadRange')" />

          <SelectUser
            v-model:value="dataMap[item].assigneeId"
            :error="assigneeIdErrorSet.has(item)"
            :placeholder="t('backlog.splitTask.placeholders.assignee')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            @change="assigneeIdChange(item)" />

          <SelectUser
            v-model:value="dataMap[item].confirmerId"
            :placeholder="t('backlog.splitTask.placeholders.confirmer')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <Tooltip
            :title="t('backlog.splitTask.tooltips.deadlineMustBeFuture')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="deadlineDateErrorSet.has(item)">
            <DatePicker
              v-model:value="dataMap[item].deadlineDate"
              :error="deadlineDateErrorSet.has(item)"
              :showNow="false"
              :disabledDate="disabledDate"
              :showTime="{ hideDisabledOptions: true, format: TIME_FORMAT }"
              :placeholder="t('backlog.splitTask.placeholders.deadline')"
              type="date"
              size="small"
              showToday
              class="w-42 mr-2.5"
              @change="deadlineDateChange(item)" />
          </Tooltip>

          <div
            class="w-5 h-5 text-3.5 flex items-center justify-center text-theme-text-hover cursor-pointer"
            @click="toDelete(item, index)">
            <Icon icon="icon-qingchu" />
          </div>
        </div>
      </div>

      <Button
        :disabled="idList?.length>=MAX_LENGTH"
        type="link"
        size="small"
        class="flex items-center px-0 mt-1"
        @click="toAdd">
        <Icon icon="icon-jia" class="mr-1 text-3.5" />
        <span>{{ t('backlog.splitTask.continueAdd') }}</span>
      </Button>
    </Spin>
  </Modal>
</template>

<style scoped>
.head-container {
  background-color: rgb(246, 248, 251);
  line-height: 32px;
}

.head-item-container {
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.head-item-container:last-child {
  border: 0;
}
</style>

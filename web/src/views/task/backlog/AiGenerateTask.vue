<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import {
  DatePicker,
  Icon,
  IconRequired,
  IconTask,
  Input,
  Modal,
  notification,
  SelectUser,
  Spin,
  TaskPriority,
  Tooltip
} from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
import { Button } from 'ant-design-vue';
import { TESTER, utils, duration } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { task } from '@/api/tester';
import { ai } from '@/api/gm';
import SelectEnum from '@/components/enum/SelectEnum.vue';

import { TaskInfo } from '../types';

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'ok'): void;
}>();

const MAX_LENGTH = 200; // 最大允许创建200条

const aiEnabled = inject('aiEnabled', ref(false));

const confirmLoading = ref(false);
const generating = ref(false);

const idList = ref<string[]>([]);
const dataMap = ref<{
  [key: string]: {
    taskType: TaskInfo['taskType']['value'];
    priority: TaskInfo['priority']['value'];
    name: string;
    evalWorkload: string;
    projectId: string;
    assigneeId?: string;
    confirmerId?: string;
    deadlineDate?: string;
    sprintId?: string;
    moduleId?: string;
  }
}>({});

const taskTypeErrorSet = ref(new Set<string>());
const priorityErrorSet = ref(new Set<string>());
const nameErrorSet = ref(new Set<string>());
const deadlineDateErrorSet = ref(new Set<string>());

const nameRepeatSet = ref(new Set<string>());

const aiKeywords = ref('');

const toGenerate = async () => {
  generating.value = true;
  const [error, res] = await ai.getChartResult({ type: 'WRITE_BACKLOG', question: aiKeywords.value });
  generating.value = false;
  if (error) {
    return;
  }

  const data = (res?.data || { normal: '', content: [] }) as {
    normal:string;
    content:string[];
  };

  const list = data.content;
  const newIdList:string[] = [];
  const newDataMap = {};
  for (let i = 0, len = list.length; i < len; i++) {
    const id = utils.uuid();
    newIdList.push(id);
    newDataMap[id] = {
      name: list[i],
      assigneeId: undefined,
      confirmerId: undefined,
      deadlineDate: undefined,
      evalWorkload: '1',
      moduleId: undefined,
      priority: 'MEDIUM',
      projectId: props.projectId,
      sprintId: undefined,
      taskType: 'REQUIREMENT'
    };
  }

  idList.value = newIdList;
  dataMap.value = newDataMap;
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

const deadlineDateChange = (id: string) => {
  deadlineDateErrorSet.value.delete(id);
};

const toAdd = () => {
  const newId = utils.uuid();
  idList.value.push(newId);
  dataMap.value[newId] = {
    assigneeId: undefined,
    confirmerId: undefined,
    deadlineDate: undefined,
    evalWorkload: '1',
    moduleId: undefined,
    priority: 'MEDIUM',
    projectId: props.projectId,
    sprintId: undefined,
    taskType: 'REQUIREMENT',
    name: ''
  };
};

const toDelete = (id: string, index: number) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  taskTypeErrorSet.value.delete(id);
  priorityErrorSet.value.delete(id);
  nameErrorSet.value.delete(id);
  deadlineDateErrorSet.value.delete(id);
};

const cancel = () => {
  reset();
  emit('update:visible', false);
};

const ok = async () => {
  if (idList.value?.length > MAX_LENGTH) {
    notification.warning(t('backlog.aiGenerateTask.maxTasksWarning'));
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

    if (item.deadlineDate && dayjs(item.deadlineDate).isBefore(dayjs())) {
      deadlineDateErrorSet.value.add(id);
    }
  }

  return !(taskTypeErrorSet.value.size +
    priorityErrorSet.value.size +
    nameErrorSet.value.size +
    deadlineDateErrorSet.value.size);
};

// 禁用日期组件当前时间之前的年月日
const disabledDate = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

const reset = () => {
  aiKeywords.value = '';
  idList.value = [];
  dataMap.value = {};
  taskTypeErrorSet.value.clear();
  priorityErrorSet.value.clear();
  nameErrorSet.value.clear();
  deadlineDateErrorSet.value.clear();
  resetError();
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    reset();
    toAdd();
  }, { immediate: true, deep: true });
});

const okButtonProps = computed(() => {
  return {
    disabled: generating.value
  };
});

const excluedType = (option: {value: string; message: string}) => {
  return ['API_TEST', 'SCENARIO_TEST'].includes(option.value);
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="1200"
    :confirmLoading="confirmLoading"
    :okButtonProps="okButtonProps"
    :title="t('backlog.aiGenerateTask.title')"
    @cancel="cancel"
    @ok="ok">
    <Spin :spinning="generating||confirmLoading" :tip="generating ? t('backlog.aiGenerateTask.generating') : ''">
      <div class="flex flex-nowrap justify-between mb-3.5 space-x-5">
        <Input
          v-model:value="aiKeywords"
          :placeholder="t('backlog.aiGenerateTask.aiKeywordsPlaceholder')"
          trim
          allowClear
          class="flex-1"
          :maxlength="2000" />
        <Button
          :disabled="!aiKeywords"
          type="primary"
          size="small"
          @click="toGenerate">
          {{ t('backlog.aiGenerateTask.generate') }}
        </Button>
      </div>

      <div class="head-container flex items-center space-x-2.5 mb-1.5 px-2 rounded">
        <div class="w-27 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.aiGenerateTask.headers.type') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.aiGenerateTask.headers.priority') }}</span>
        </div>

        <div class="flex-1 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.aiGenerateTask.headers.name') }}</span>
        </div>

        <div class="w-20 space-x-0.5 head-item-container">
          <span>{{ t('backlog.aiGenerateTask.headers.evalWorkload') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <IconRequired />
          <span>{{ t('backlog.aiGenerateTask.headers.assignee') }}</span>
        </div>

        <div class="w-25 space-x-0.5 head-item-container">
          <span>{{ t('backlog.aiGenerateTask.headers.confirmer') }}</span>
        </div>

        <div class="w-42 space-x-0.5 head-item-container">
          <span>{{ t('backlog.aiGenerateTask.headers.deadline') }}</span>
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
            :excludes="excluedType"
            enumKey="TaskType"
            :placeholder="t('backlog.aiGenerateTask.placeholders.taskType')"
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
            :placeholder="t('backlog.aiGenerateTask.placeholders.priority')"
            class="w-20 mr-2.5"
            @change="priorityChange(item)">
            <template #option="record">
              <TaskPriority :value="record" />
            </template>
          </SelectEnum>

          <Tooltip
            :title="t('backlog.aiGenerateTask.tooltips.nameRepeat')"
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
              :placeholder="t('backlog.aiGenerateTask.placeholders.taskName')"
              @change="nameChange()" />
          </Tooltip>

          <Input
            v-model:value="dataMap[item].evalWorkload"
            class="w-20 mr-2.5"
            size="small"
            dataType="float"
            trimAll
            :min="0.1"
            :max="1000"
            :placeholder="t('backlog.aiGenerateTask.placeholders.evalWorkload')" />

          <SelectUser
            v-model:value="dataMap[item].assigneeId"
            :placeholder="t('backlog.aiGenerateTask.placeholders.assignee')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <SelectUser
            v-model:value="dataMap[item].confirmerId"
            :placeholder="t('backlog.aiGenerateTask.placeholders.confirmer')"
            allowClear
            class="w-25 mr-2.5"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80" />

          <Tooltip
            :title="t('backlog.aiGenerateTask.tooltips.deadlineDate')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="deadlineDateErrorSet.has(item)">
            <DatePicker
              v-model:value="dataMap[item].deadlineDate"
              :error="deadlineDateErrorSet.has(item)"
              :showNow="false"
              :disabledDate="disabledDate"
              :showTime="{ hideDisabledOptions: true, format: 'HH:mm:ss' }"
              :placeholder="t('backlog.aiGenerateTask.placeholders.deadline')"
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
        <span>{{ t('backlog.aiGenerateTask.continueAdd') }}</span>
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

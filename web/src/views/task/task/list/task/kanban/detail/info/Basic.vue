<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref } from 'vue';
import { Button, Tag, TreeSelect } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, IconTask, Input, Select, TaskPriority, TaskStatus } from '@xcan-angus/vue-ui';
import { TESTER, EvalWorkloadMethod } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskType, BugLevel, SoftwareVersionStatus } from '@/enums/enums';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
  (event: 'refresh'): void;
}>();

const Description = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/detail/info/Description.vue'));

const nameRef = ref();
const nameEditFlag = ref(false);
const taskName = ref<string>();

const evalWorkloadRef = ref();
const evalWorkloadEditFlag = ref(false);
const evalWorkloadValue = ref<string>();

const actualWorkloadRef = ref();
const actualWorkloadEditFlag = ref(false);
const actualWorkloadValue = ref<string>();

const taskTypeRef = ref();
const taskTypeEditFlag = ref(false);
const taskTypeMessage = ref<string>();
const taskTypeValue = ref<TaskInfo['taskType']['value']>();

const sprintRef = ref();
const sprintEditFlag = ref(false);
const sprintMessage = ref<string>();
const sprintValue = ref<string>();

const moduleRef = ref();
const moduleEditFlag = ref(false);
const moduleValue = ref<string>();

const priorityRef = ref();
const priorityEditFlag = ref(false);
const priorityMessage = ref<string>();
const priorityValue = ref<TaskInfo['priority']['value']>();

const tagRef = ref();
const tagEditFlag = ref(false);
const tagList = ref<{id:string;name:string;}[]>([]);
const tagIdList = ref<string[]>([]);

const versionRef = ref();
const versionEditFlag = ref(false);
const versionValue = ref<string>();

const toEditName = () => {
  taskName.value = name.value;
  nameEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof nameRef.value?.focus === 'function') {
        nameRef.value?.focus();
      }
    }, 100);
  });
};

const nameBlur = async (event: { target: { value: string; } }) => {
  const value = event.target.value;
  if (!value || value === name.value) {
    nameEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskName(taskId.value, value);
  emit('loadingChange', false);
  nameEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, name: value });
};

const namePressEnter = () => {
  if (typeof nameRef.value?.blur === 'function') {
    nameRef.value.blur();
  }
};

const toEditActualWorkload = () => {
  actualWorkloadValue.value = actualWorkload.value;
  actualWorkloadEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof actualWorkloadRef.value?.focus === 'function') {
        actualWorkloadRef.value?.focus();
      }
    }, 100);
  });
};

const actualWorkloadBlur = async (event: { target: { value: string; } }) => {
  const value = event.target.value;
  if (value === actualWorkload.value) {
    actualWorkloadEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(taskId.value, { workload: value });
  emit('loadingChange', false);
  actualWorkloadEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, actualWorkload: value });
};

const actualWorkloadPressEnter = () => {
  if (typeof actualWorkloadRef.value?.blur === 'function') {
    actualWorkloadRef.value.blur();
  }
};

const toEditEvalWorkload = () => {
  evalWorkloadValue.value = evalWorkload.value;
  evalWorkloadEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof evalWorkloadRef.value?.focus === 'function') {
        evalWorkloadRef.value?.focus();
      }
    }, 100);
  });
};

const evalWorkloadBlur = async (event: { target: { value: string; } }) => {
  const value = event.target.value;
  if (value === evalWorkload.value) {
    evalWorkloadEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(taskId.value, { workload: value });
  emit('loadingChange', false);
  evalWorkloadEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, evalWorkload: value });
};

const evalWorkloadPressEnter = () => {
  if (typeof evalWorkloadRef.value?.blur === 'function') {
    evalWorkloadRef.value.blur();
  }
};

const toEditSprint = () => {
  sprintValue.value = sprintId.value;
  sprintEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof sprintRef.value?.focus === 'function') {
        sprintRef.value?.focus();
      }
    }, 100);
  });
};

const sprintChange = async (
  _event: { target: { value: string; } },
  option: { message: string; value: string
  }) => {
  sprintMessage.value = option.message;
};

const sprintBlur = async () => {
  const value = sprintValue.value;
  if (!value || value === sprintId.value) {
    sprintEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const params = {
    taskIds: [taskId.value],
    targetSprintId: value
  };
  const [error] = await task.moveTask(params);
  emit('loadingChange', false);
  sprintEditFlag.value = false;
  if (error) {
    return;
  }

  const info = await loadTaskInfoById(taskId.value);
  emit('change', info);
};

const toEditModule = () => {
  moduleValue.value = moduleId.value;
  moduleEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof moduleRef.value?.focus === 'function') {
        moduleRef.value?.focus();
      }
    }, 100);
  });
};

const moduleOk = async () => {
  const value = moduleValue.value;
  if (!value || value === moduleId.value) {
    moduleEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const params = {
    moduleId: value
  };
  const [error] = await task.updateTask(taskId.value, params);
  emit('loadingChange', false);
  moduleEditFlag.value = false;
  if (error) {
    return;
  }

  const info = await loadTaskInfoById(taskId.value);
  emit('change', info);
};

const moduleCancel = () => {
  moduleEditFlag.value = false;
};

const moduleTreeData = ref([]);
const getModuleTreeData = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

const loadTaskInfoById = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(id);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id };
  }

  return res.data;
};

const toEditTaskType = () => {
  taskTypeValue.value = taskType.value;
  taskTypeEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof taskTypeRef.value?.focus === 'function') {
        taskTypeRef.value?.focus();
      }
    }, 100);
  });
};

const taskTypeChange = async (
  _event: { target: { value: TaskInfo['taskType']['value']; } },
  option: { message: string; value: TaskInfo['taskType']['value']
  }) => {
  taskTypeMessage.value = option.message;
};

const taskTypeBlur = async () => {
  const value = taskTypeValue.value;
  if (!value || value === taskType.value) {
    taskTypeEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTaskType(taskId.value, value);
  emit('loadingChange', false);
  taskTypeEditFlag.value = false;
  if (value === TaskType.BUG) {
    await task.updateTask(taskId.value, {
      bugLevel: BugLevel.MINOR,
      missingBug: false
    });
  }
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, taskType: { value, message: taskTypeMessage.value! } });
};

const taskTypeExcludes = (data: { value: TaskInfo['taskType']['value']; message: string }) => {
  const value = data.value;
  const type = taskType.value;
  if (taskId.value) {
    if (type === TaskType.API_TEST) {
      return value !== TaskType.API_TEST;
    }

    if (type === TaskType.SCENARIO_TEST) {
      return value !== TaskType.SCENARIO_TEST;
    }
    return [TaskType.API_TEST, TaskType.SCENARIO_TEST].includes(value);
  }
  return false;
};

const toEditPriority = () => {
  priorityValue.value = priority.value;
  priorityEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof priorityRef.value?.focus === 'function') {
        priorityRef.value?.focus();
      }
    }, 100);
  });
};

const priorityChange = async (
  _event: { target: { value: TaskInfo['priority']['value']; } },
  option: { message: string; value: TaskInfo['priority']['value']
  }) => {
  priorityMessage.value = option.message;
};

const priorityBlur = async () => {
  const value = priorityValue.value;
  if (!value || value === priority.value) {
    priorityEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskPriority(taskId.value, value);
  emit('loadingChange', false);
  priorityEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, priority: { value, message: priorityMessage.value! } });
};

const toEditTag = () => {
  tagIdList.value = tagIds.value;
  tagEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof tagRef.value?.focus === 'function') {
        tagRef.value?.focus();
      }
    }, 100);
  });
};

const tagChange = async (
  _event: { target: { value: string[]; } },
  options: { id: string; name: string; }[]) => {
  tagList.value = options;
};

const tagBlur = async () => {
  const ids = tagIdList.value;
  if (isEqual(ids, tagIds.value)) {
    tagEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskTags(taskId.value, { tagIds: ids });
  emit('loadingChange', false);
  tagEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, tags: tagList.value });
};

const toEditVersion = () => {
  versionEditFlag.value = true;
  versionValue.value = props.dataSource?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionRef.value?.focus === 'function') {
        versionRef.value?.focus();
      }
    }, 100);
  });
};

const versionChange = (value) => {
  versionValue.value = value;
};

const versionBlur = async () => {
  const value = versionValue.value;
  if (value === props.dataSource?.softwareVersion) {
    versionEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(taskId.value, { softwareVersion: value || '' });
  emit('loadingChange', false);
  versionEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, softwareVersion: versionValue.value });
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const taskInfoChange = (data: Partial<TaskInfo>) => {
  emit('change', data);
};

onMounted(() => {
  getModuleTreeData();
});

const taskId = computed(() => props.dataSource?.id);
const status = computed(() => props.dataSource?.status);
const name = computed(() => props.dataSource?.name);
const taskType = computed(() => props.dataSource?.taskType?.value);
const sprintId = computed(() => props.dataSource?.sprintId);
const moduleId = computed(() => {
  if (!props.dataSource?.moduleId || props.dataSource?.moduleId === '-1') {
    return undefined;
  }

  return props.dataSource?.moduleId;
});
const priority = computed(() => props.dataSource?.priority?.value);
const tags = computed(() => props.dataSource?.tags || []);
const tagIds = computed(() => props.dataSource?.tags?.map(item => item.id) || []);
const evalWorkloadMethod = computed(() => props.dataSource?.evalWorkloadMethod?.value);
const evalWorkload = computed(() => props.dataSource?.evalWorkload);
const actualWorkload = computed(() => props.dataSource?.actualWorkload);
const overdue = computed(() => props.dataSource?.overdue);
const totalNum = computed(() => +(props.dataSource?.totalNum || 0));
const failNum = computed(() => +(props.dataSource?.failNum || 0));
const onePassText = computed(() => {
  if (totalNum.value <= 0) {
    return '--';
  }
  return failNum.value === 0 ? t('status.yes') : t('status.no');
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div>
      <div class="text-theme-title mb-2.5 font-semibold">
        {{ t('task.detailInfo.basic.title') }}
      </div>

      <div class="space-y-2.5">
        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.code') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.code }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.name') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!nameEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ name }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="toEditName">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="nameEditFlag">
            <Input
              v-show="nameEditFlag"
              ref="nameRef"
              v-model:value="taskName"
              :maxlength="200"
              trim
              class="edit-container"
              :placeholder="t('task.detailInfo.basic.columns.namePlaceholder')"
              @blur="nameBlur"
              @pressEnter="namePressEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.status') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <TaskStatus :value="status" />
            <span
              v-if="overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">
                {{ t('task.detailInfo.basic.columns.overdue') }}
              </span>
            </span>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.sprint') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!sprintEditFlag" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.sprintName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="toEditSprint">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="sprintEditFlag">
            <Select
              v-show="sprintEditFlag"
              ref="sprintRef"
              v-model:value="sprintValue"
              :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
              :fieldNames="{ value: 'id', label: 'name' }"
              showSearch
              :placeholder="t('task.detailInfo.basic.columns.selectSprint')"
              class="edit-container"
              @change="sprintChange"
              @blur="sprintBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.module') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!moduleEditFlag" class="flex items-center">
            <span class="ml-1.5">{{ props.dataSource?.moduleName }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="toEditModule">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="moduleEditFlag">
            <div v-show="moduleEditFlag" class="flex items-center w-full">
              <TreeSelect
                ref="moduleRef"
                v-model:value="moduleValue"
                :treeData="moduleTreeData"
                :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
                :virtual="false"
                size="small"
                showSearch
                allowClear
                class="flex-1"
                :placeholder="t('task.detailInfo.basic.columns.selectModule')">
                <template #title="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>
              <Icon
                icon="icon-gouxuanzhong"
                class="text-3.5 ml-2 mr-1.5 cursor-pointer text-theme-text-hover"
                @click="moduleOk" />
              <Icon
                icon="icon-shanchuguanbi"
                class="text-3.5 cursor-pointer text-theme-text-hover"
                @click="moduleCancel" />
            </div>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.parentTask') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-if="!props.dataSource?.parentTaskId" class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </div>

          <RouterLink
            v-else
            target="_self"
            :to="`/task#task?projectId=${props.projectId}&taskId=${props.dataSource?.parentTaskId}&total=1`"
            style="color:#40a9ff"
            class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.parentTaskName || '--' }}
          </RouterLink>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!taskTypeEditFlag" class="flex items-center">
            <IconTask :value="taskType" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ props.dataSource?.taskType?.message }}</span>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="toEditTaskType">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
            <template v-if="taskType === TaskType.BUG">
              <Tag
                v-if="props.dataSource?.bugLevel"
                color="error"
                class="ml-2 text-3 leading-4">
                {{ props.dataSource?.bugLevel?.message }}
              </Tag>
              <Tag
                v-if="props.dataSource?.missingBug"
                color="error"
                class="ml-2 text-3 leading-4">
                {{ t('task.detailInfo.basic.columns.missingBug') }}
              </Tag>
            </template>
          </div>

          <AsyncComponent :visible="taskTypeEditFlag">
            <SelectEnum
              v-show="taskTypeEditFlag"
              ref="taskTypeRef"
              v-model:value="taskTypeValue"
              :excludes="taskTypeExcludes"
              enumKey="TaskType"
              :placeholder="t('task.detailInfo.basic.columns.selectTaskType')"
              class="edit-container"
              @change="taskTypeChange"
              @blur="taskTypeBlur">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.value" class="text-4 flex-shrink-0" />
                  <span class="ml-2">{{ record.message }}</span>
                </div>
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.priority') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!priorityEditFlag" class="flex items-center">
            <TaskPriority :value="props.dataSource?.priority" />
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="toEditPriority">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="priorityEditFlag">
            <SelectEnum
              v-show="priorityEditFlag"
              ref="priorityRef"
              v-model:value="priorityValue"
              enumKey="Priority"
              :placeholder="t('task.detailInfo.basic.columns.selectPriority')"
              class="edit-container"
              @change="priorityChange"
              @blur="priorityBlur">
              <template #option="record">
                <TaskPriority :value="record" />
              </template>
            </SelectEnum>
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.evalWorkloadMethod') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.evalWorkloadMethod?.message }}
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>
              {{ evalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                ? t('task.detailInfo.basic.columns.evalWorkload')
                : t('task.detailInfo.basic.columns.evalWorkloadHours')
              }}
            </span>
            <Colon class="w-1" />
          </div>

          <div v-show="!evalWorkloadEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ evalWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="toEditEvalWorkload">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="evalWorkloadEditFlag">
            <Input
              v-show="evalWorkloadEditFlag"
              ref="evalWorkloadRef"
              v-model:value="evalWorkloadValue"
              class="edit-container"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('task.detailInfo.basic.columns.evalWorkloadPlaceholder')"
              @blur="evalWorkloadBlur"
              @pressEnter="evalWorkloadPressEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>
              {{
                evalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                  ? t('task.detailInfo.basic.columns.actualStoryPoint')
                  : t('task.detailInfo.basic.columns.actualWorkload')
              }}
            </span>
            <Colon class="w-1" />
          </div>

          <div v-show="!actualWorkloadEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ actualWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="toEditActualWorkload">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="actualWorkloadEditFlag">
            <Input
              v-show="actualWorkloadEditFlag"
              ref="actualWorkloadRef"
              v-model:value="actualWorkloadValue"
              class="edit-container"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('task.detailInfo.basic.columns.actualWorkloadPlaceholder')"
              @blur="actualWorkloadBlur"
              @pressEnter="actualWorkloadPressEnter" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.totalNum') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ totalNum }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.failNum') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failNum }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.tags') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!tagEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div v-if="tags.length" class="flex items-center flex-wrap transform-gpu -translate-y-0.25">
              <div
                v-for="item in tags"
                :key="item.id"
                class="px-2 h-5 leading-4.5 mr-1 mb-1 rounded border border-solid border-border-divider bg-gray-light text-theme-sub-content">
                {{ item.name }}
              </div>
            </div>
            <div v-else>--</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-1 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="toEditTag">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="tagEditFlag">
            <Select
              v-show="tagEditFlag"
              ref="tagRef"
              v-model:value="tagIdList"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="5"
              :maxTagTextLength="15"
              :maxTags="5"
              :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
              showSearch
              :placeholder="t('task.detailInfo.basic.columns.tagsPlaceholder')"
              mode="multiple"
              class="edit-container"
              :notFoundContent="t('task.detailInfo.basic.columns.tagsNotFound')"
              @change="tagChange"
              @blur="tagBlur" />
          </AsyncComponent>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.onePass') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.softwareVersion') }}</span>
            <Colon class="w-1" />
          </div>
          <div class="flex-1 min-w-0">
            <template v-if="versionEditFlag">
              <Select
                ref="versionRef"
                v-model:value="versionValue"
                allowClear
                :placeholder="t('task.detailInfo.basic.columns.softwareVersionPlaceholder')"
                class="w-full"
                lazy
                :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="versionBlur"
                @change="versionChange">
              </Select>
            </template>
            <template v-else>
              <div class="flex space-x-1">
                <RouterLink
                  v-if="props.dataSource?.softwareVersion"
                  class="text-theme-special"
                  :to="`/task#version?name=${props.dataSource?.softwareVersion}`">
                  {{ props.dataSource?.softwareVersion }}
                </RouterLink>
                <template v-else>
                  --
                </template>
                <Button
                  type="link"
                  class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
                  @click="toEditVersion">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>
            </template>
          </div>
        </div>

        <div class="flex items-start">
          <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.unplanned') }}</span>
            <Colon class="w-1" />
          </div>
          <div>
            {{ props.dataSource?.unplanned ? t('task.detailInfo.basic.columns.yes') : t('task.detailInfo.basic.columns.no') }}
          </div>
        </div>
      </div>
    </div>

    <Description
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      @change="taskInfoChange"
      @loadingChange="loadingChange" />
  </div>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>

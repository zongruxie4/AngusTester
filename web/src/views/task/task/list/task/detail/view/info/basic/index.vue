<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import {
  AsyncComponent,
  Colon,
  Icon,
  IconTask,
  Input,
  Select,
  SelectEnum,
  TaskPriority,
  TaskStatus,
  Toggle
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { task } from 'src/api/tester';

import { TaskInfo } from '@/views/task/PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

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

const taskTypeChange = async (_event: { target: { value: TaskInfo['taskType']['value']; } }, option: { message: string; value: TaskInfo['taskType']['value'] }) => {
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
  if (value === 'BUG') {
    await task.updateTask(taskId.value, {
      bugLevel: 'MINOR',
      missingBugFlag: false
    });
  }
  taskTypeEditFlag.value = false;
  if (error) {
    return;
  }

  emit('change', { id: taskId.value, bugLevel: { value: 'MINOR', message: '一般' }, missingBugFlag: false, taskType: { value, message: taskTypeMessage.value! } });
};

const taskTypeExcludes = (data: { value: TaskInfo['taskType']['value']; message: string }) => {
  const value = data.value;
  const type = taskType.value;
  if (taskId.value) {
    if (type === 'API_TEST') {
      return value !== 'API_TEST';
    }

    if (type === 'SCENARIO_TEST') {
      return value !== 'SCENARIO_TEST';
    }

    return ['API_TEST', 'SCENARIO_TEST'].includes(value);
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

const priorityChange = async (_event: { target: { value: TaskInfo['priority']['value']; } }, option: { message: string; value: TaskInfo['priority']['value'] }) => {
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

const tagChange = async (_event: { target: { value: string[]; } }, options: { id: string; name: string; }[]) => {
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

const taskId = computed(() => props.dataSource?.id);
const status = computed(() => props.dataSource?.status);
const name = computed(() => props.dataSource?.name);
const taskType = computed(() => props.dataSource?.taskType?.value);
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

  return failNum.value === 0 ? '是' : '否';
});
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3">基本信息</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>编号</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.code }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>任务状态</span>
              <Colon class="w-1" />
            </div>

            <div class="flex items-center">
              <TaskStatus :value="status" />
              <span
                v-if="overdue"
                class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
                style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                <span class="inline-block transform-gpu scale-90">已逾期</span>
              </span>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>名称</span>
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
                class="left-component"
                placeholder="任务名称，最大支持200字符"
                @blur="nameBlur"
                @pressEnter="namePressEnter" />
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>所属迭代</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ props.dataSource?.sprintName }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>所属模块</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.moduleName || '--' }}
            </div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>工作量评估方式</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ props.dataSource?.evalWorkloadMethod?.message }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>父任务</span>
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

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ evalWorkloadMethod === 'STORY_POINT' ? '评估故事点' : '评估工时' }}</span>
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
                class="right-component max-w-52"
                dataType="float"
                trimAll
                :min="0.1"
                :max="1000"
                placeholder="最小0.1，最大1000，最多支持2位小数"
                @blur="evalWorkloadBlur"
                @pressEnter="evalWorkloadPressEnter" />
            </AsyncComponent>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>类型</span>
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
              <template v-if="taskType === 'BUG'">
                <Tag
                  v-if="props.dataSource?.bugLevel"
                  color="error"
                  class="ml-2 text-3 leading-4">
                  {{ props.dataSource?.bugLevel?.message }}
                </Tag>
                <Tag
                  v-if="props.dataSource?.missingBugFlag"
                  color="error"
                  class="ml-2 text-3 leading-4">
                  漏测
                </Tag>
              </template>
            </div>

            <AsyncComponent :visible="taskTypeEditFlag">
              <SelectEnum
                v-show="taskTypeEditFlag"
                ref="taskTypeRef"
                v-model:value="taskTypeValue"
                :allowClear="false"
                :excludes="taskTypeExcludes"
                internal
                enumKey="TaskType"
                placeholder="请选择任务类型"
                class="left-component max-w-52"
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

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ evalWorkloadMethod === 'STORY_POINT' ? '实际故事点' : '实际工时' }}</span>
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
                class="right-component max-w-52"
                dataType="float"
                trimAll
                :min="0.1"
                :max="1000"
                placeholder="最小0.1，最大1000，最多支持2位小数"
                @blur="actualWorkloadBlur"
                @pressEnter="actualWorkloadPressEnter" />
            </AsyncComponent>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>优先级</span>
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
                :allowClear="false"
                internal
                enumKey="Priority"
                placeholder="请选择优先级"
                class="left-component max-w-52"
                @change="priorityChange"
                @blur="priorityBlur">
                <template #option="record">
                  <TaskPriority :value="record" />
                </template>
              </SelectEnum>
            </AsyncComponent>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>处理失败次数</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ failNum }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>处理次数</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ totalNum }}</div>
          </div>

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>一次性通过</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>软件版本</span>
              <Colon class="w-1" />
            </div>
            <div class="flex-1 min-w-0">
              <template v-if="versionEditFlag">
                <Select
                  ref="versionRef"
                  v-model:value="versionValue"
                  allowClear
                  placeholder="请选择所属版本"
                  lazy
                  class="w-full max-w-60"
                  :action="`${TESTER}/software/version?projectId=${props.projectId}`"
                  :params="{filters: [{value: ['NOT_RELEASED', 'RELEASED'], key: 'status', op: 'IN'}]}"
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

          <div class="relative w-1/2 flex items-start">
            <div class="w-24.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>计划外任务</span>
              <Colon class="w-1" />
            </div>
            <div class="">
              {{ props.dataSource?.unplannedFlag ? '是' : '否' }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="relative w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>标签</span>
              <Colon class="w-1" />
            </div>

            <div v-show="!tagEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
              <div v-if="tags.length" class="flex items-center flex-wrap transform-gpu -translate-y-0.25">
                <div
                  v-for="item in tags"
                  :key="item.id"
                  class="px-2 h-5.5 leading-5 mr-2 mb-2 rounded border border-solid border-border-divider bg-gray-light text-theme-sub-content">
                  {{ item.name }}
                </div>
              </div>
              <div v-else>--</div>
              <Button
                type="link"
                class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
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
                :allowClear="false"
                :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
                showSearch
                internal
                placeholder="最多可添加5个标签"
                mode="multiple"
                class="left-component"
                notFoundContent="请联系管理员，前往”应用管理“-”任务管理“-”任务标签“配置任务标签。"
                @change="tagChange"
                @blur="tagBlur" />
            </AsyncComponent>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.border-none {
  border: none;
}

.left-component {
  position: absolute;
  top: -4px;
  left: 50px;
  width: calc(100% - 50px);
}

.right-component {
  position: absolute;
  top: -4px;
  left: 98px;
  width: calc(100% - 98px);
}
</style>

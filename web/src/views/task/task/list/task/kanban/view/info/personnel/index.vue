<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

import { TaskInfo } from '../../../../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
}

const props = withDefaults(defineProps<Props>(), {
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

const assigneeRef = ref();
const assigneeEditFlag = ref(false);
const assigneeMessage = ref<string>();
const assigneeIdValue = ref<string>();

const confirmorRef = ref();
const confirmorEditFlag = ref(false);
const confirmorMessage = ref<string>();
const confirmorIdValue = ref<string>();

const testerRef = ref();
const testerEditFlag = ref(false);
const testerMessage = ref<string>();
const testerIdValue = ref<string>();

const toEditAssignee = () => {
  assigneeIdValue.value = assigneeId.value;
  assigneeEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof assigneeRef.value?.focus === 'function') {
        assigneeRef.value?.focus();
      }
    }, 100);
  });
};

const assignToMe = (key:'assigneeId'|'confirmorId'|'testerId') => {
  if (key === 'assigneeId') {
    assigneeIdValue.value = userId.value;
    assigneeMessage.value = userName.value;
    assigneeBlur();
    return;
  }

  if (key === 'confirmorId') {
    confirmorIdValue.value = userId.value;
    confirmorMessage.value = userName.value;
    confirmorBlur();
  }

  if (key === 'testerId') {
    testerIdValue.value = userId.value;
    testerMessage.value = userName.value;
    testerBlur();
  }
};

const assigneeChange = async (_event: { target: { value: string; } }, option: { id: string; fullname: string; }) => {
  assigneeMessage.value = option.fullname;
};

const assigneeBlur = async () => {
  const value = assigneeIdValue.value;
  if (value === assigneeId.value) {
    assigneeEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await http.put(`${TESTER}/task/${taskId.value}/assignee`, { assigneeId: value });
  emit('loadingChange', false);
  if (error) {
    if (typeof assigneeRef.value?.focus === 'function') {
      assigneeRef.value?.focus();
    }

    return;
  }

  assigneeEditFlag.value = false;
  emit('change', { id: taskId.value, assigneeId: value, assigneeName: assigneeMessage.value! });
};

const toEditConfirmor = () => {
  confirmorIdValue.value = confirmorId.value;
  confirmorEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof confirmorRef.value?.focus === 'function') {
        confirmorRef.value?.focus();
      }
    }, 100);
  });
};

const confirmorChange = async (_event: { target: { value: string; } }, option: { id: string; fullname: string; }) => {
  confirmorMessage.value = option.fullname;
};

const confirmorBlur = async () => {
  const value = confirmorIdValue.value;
  if (value === confirmorId.value) {
    confirmorEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await http.put(`${TESTER}/task/${taskId.value}/confirmor`, { confirmorId: value });
  emit('loadingChange', false);
  if (error) {
    if (typeof confirmorRef.value?.focus === 'function') {
      confirmorRef.value?.focus();
    }

    return;
  }

  confirmorEditFlag.value = false;
  emit('change', { id: taskId.value, confirmorId: value, confirmorName: confirmorMessage.value! });
};

const toEdiTester = () => {
  testerEditFlag.value = true;
  testerIdValue.value = testerId.value;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerRef.value?.focus === 'function') {
        testerRef.value?.focus();
      }
    }, 100);
  });
};

const testerChange = async (_event: { target: { value: string; } }, option: { id: string; fullname: string; }) => {
  testerMessage.value = option.fullname;
};

const testerBlur = async () => {
  const value = testerIdValue.value;
  if (value === testerId.value) {
    testerEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await http.patch(`${TESTER}/task/${taskId.value}`, { testerId: value });
  emit('loadingChange', false);
  if (error) {
    if (typeof testerRef.value?.focus === 'function') {
      testerRef.value?.focus();
    }

    return;
  }

  testerEditFlag.value = false;
  emit('change', { id: taskId.value, testerId: value, testerName: testerMessage.value! });
};

const taskId = computed(() => props.dataSource?.id);
const createdByName = computed(() => props.dataSource?.createdByName);
const execByName = computed(() => props.dataSource?.execByName);
const lastModifiedByName = computed(() => props.dataSource?.lastModifiedByName);

const assigneeId = computed(() => props.dataSource?.assigneeId);
const assigneeName = computed(() => props.dataSource?.assigneeName);
const assigneeDefaultOptions = computed(() => {
  const id = assigneeId.value;
  if (!id) {
    return undefined;
  }

  return {
    [id]: {
      id: id,
      fullname: assigneeName.value
    }
  };
});

const testerId = computed(() => props.dataSource?.testerId);
const testerName = computed(() => props.dataSource?.testerName);
const testerDefaultOptions = computed(() => {
  const id = testerId.value;
  if (!id) {
    return undefined;
  }

  return {
    [id]: {
      id: id,
      fullname: testerName.value
    }
  };
});

const userId = computed(() => {
  return props.userInfo?.id;
});
const userName = computed(() => {
  return props.userInfo?.fullname;
});
const confirmorId = computed(() => props.dataSource?.confirmorId);
const confirmorName = computed(() => props.dataSource?.confirmorName);
const confirmorDefaultOptions = computed(() => {
  const id = confirmorId.value;
  if (!id) {
    return undefined;
  }

  return {
    [id]: {
      id: id,
      fullname: confirmorName.value
    }
  };
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">人员</div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>添加人</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ createdByName }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>经办人</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!assigneeEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ assigneeName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="toEditAssignee">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!assigneeId||assigneeId!==userId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignToMe('assigneeId')">
            指派给我
          </Button>
        </div>

        <AsyncComponent :visible="assigneeEditFlag">
          <SelectUser
            v-show="assigneeEditFlag"
            ref="assigneeRef"
            v-model:value="assigneeIdValue"
            placeholder="选择经办人"
            allowClear
            :defaultOptions="assigneeDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="assigneeChange"
            @blur="assigneeBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>执行人</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ execByName || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>确认人</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!confirmorEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ confirmorName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="toEditConfirmor">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!confirmorId||confirmorId!==userId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignToMe('confirmorId')">
            指派给我
          </Button>
        </div>

        <AsyncComponent :visible="confirmorEditFlag">
          <SelectUser
            v-show="confirmorEditFlag"
            ref="confirmorRef"
            v-model:value="confirmorIdValue"
            placeholder="选择确认人"
            allowClear
            :defaultOptions="confirmorDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="confirmorChange"
            @blur="confirmorBlur" />
        </AsyncComponent>
      </div>

      <div class="relative flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>测试人</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!testerEditFlag" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ testerName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="toEdiTester">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!testerId||testerId!==userId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignToMe('testerId')">
            指派给我
          </Button>
        </div>

        <AsyncComponent :visible="testerEditFlag">
          <SelectUser
            v-show="testerEditFlag"
            ref="testerRef"
            v-model:value="testerIdValue"
            placeholder="选择测试人"
            allowClear
            internal
            :defaultOptions="testerDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="left-component"
            @change="testerChange"
            @blur="testerBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>最后修改人</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ lastModifiedByName }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>

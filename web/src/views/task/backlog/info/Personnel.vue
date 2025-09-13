<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';

import { TaskInfo } from '../../types';
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

// Reactive State Variables - Assignee Management
const assigneeSelectRef = ref();
const isAssigneeEditing = ref(false);
const assigneeDisplayName = ref<string>();
const assigneeInputValue = ref<string>();

// Reactive State Variables - Confirmer Management
const confirmerSelectRef = ref();
const isConfirmerEditing = ref(false);
const confirmerDisplayName = ref<string>();
const confirmerInputValue = ref<string>();

// Reactive State Variables - Tester Management
const testerSelectRef = ref();
const isTesterEditing = ref(false);
const testerDisplayName = ref<string>();
const testerInputValue = ref<string>();

// Assignee Management Functions
/**
 * <p>Initialize assignee editing mode</p>
 * <p>Sets the current assignee value and enables editing state</p>
 */
const startAssigneeEditing = () => {
  assigneeInputValue.value = assigneeId.value;
  isAssigneeEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof assigneeSelectRef.value?.focus === 'function') {
        assigneeSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handle assignee selection change</p>
 * <p>Updates the display name when user selects a new assignee</p>
 */
const handleAssigneeSelectionChange = async (
  _event: { target: { value: string; } },
  option: { id: string; fullName: string; }
) => {
  assigneeDisplayName.value = option.fullName;
};

/**
 * <p>Handle assignee input blur event</p>
 * <p>Validates the selection and calls API to update assignee if changed</p>
 */
const handleAssigneeBlur = async () => {
  const newAssigneeId = assigneeInputValue.value;
  if (newAssigneeId === assigneeId.value) {
    isAssigneeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskAssignees(currentTaskId.value, { assigneeId: newAssigneeId as string });
  emit('loadingChange', false);
  if (error) {
    if (typeof assigneeSelectRef.value?.focus === 'function') {
      assigneeSelectRef.value?.focus();
    }
    return;
  }

  isAssigneeEditing.value = false;
  emit('change', { id: currentTaskId.value, assigneeId: newAssigneeId, assigneeName: assigneeDisplayName.value! });
};

// Confirmer Management Functions
/**
 * <p>Initialize confirmer editing mode</p>
 * <p>Sets the current confirmer value and enables editing state</p>
 */
const startConfirmerEditing = () => {
  confirmerInputValue.value = confirmerId.value;
  isConfirmerEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof confirmerSelectRef.value?.focus === 'function') {
        confirmerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handle confirmer selection change</p>
 * <p>Updates the display name when user selects a new confirmer</p>
 */
const handleConfirmerSelectionChange = async (
  _event: { target: { value: string; } },
  option: { id: string; fullName: string; }
) => {
  confirmerDisplayName.value = option.fullName;
};

/**
 * <p>Handle confirmer input blur event</p>
 * <p>Validates the selection and calls API to update confirmer if changed</p>
 */
const handleConfirmerBlur = async () => {
  const newConfirmerId = confirmerInputValue.value;
  if (newConfirmerId === confirmerId.value) {
    isConfirmerEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskConfirmer(currentTaskId.value, { confirmerId: newConfirmerId as string });
  emit('loadingChange', false);
  if (error) {
    if (typeof confirmerSelectRef.value?.focus === 'function') {
      confirmerSelectRef.value?.focus();
    }
    return;
  }

  isConfirmerEditing.value = false;
  emit('change', { id: currentTaskId.value, confirmerId: newConfirmerId, confirmerName: confirmerDisplayName.value! });
};

// Tester Management Functions
/**
 * <p>Initialize tester editing mode</p>
 * <p>Sets the current tester value and enables editing state</p>
 */
const startTesterEditing = () => {
  isTesterEditing.value = true;
  testerInputValue.value = testerId.value;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerSelectRef.value?.focus === 'function') {
        testerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handle tester selection change</p>
 * <p>Updates the display name when user selects a new tester</p>
 */
const handleTesterSelectionChange = async (
  _event: { target: { value: string; } },
  option: { id: string; fullName: string; }
) => {
  testerDisplayName.value = option.fullName;
};

/**
 * <p>Handle tester input blur event</p>
 * <p>Validates the selection and calls API to update tester if changed</p>
 */
const handleTesterBlur = async () => {
  const newTesterId = testerInputValue.value;
  if (newTesterId === testerId.value) {
    isTesterEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(currentTaskId.value, { testerId: newTesterId });
  emit('loadingChange', false);
  if (error) {
    if (typeof testerSelectRef.value?.focus === 'function') {
      testerSelectRef.value?.focus();
    }
    return;
  }

  isTesterEditing.value = false;
  emit('change', { id: currentTaskId.value, testerId: newTesterId, testerName: testerDisplayName.value! });
};

// Utility Functions
/**
 * <p>Assign current user to specified role</p>
 * <p>Automatically assigns the current user to the specified personnel role</p>
 */
const assignCurrentUserToRole = (roleKey: 'assigneeId' | 'confirmerId' | 'testerId') => {
  if (roleKey === 'assigneeId') {
    assigneeInputValue.value = currentUserId.value;
    assigneeDisplayName.value = currentUserName.value;
    handleAssigneeBlur();
    return;
  }

  if (roleKey === 'confirmerId') {
    confirmerInputValue.value = currentUserId.value;
    confirmerDisplayName.value = currentUserName.value;
    handleConfirmerBlur();
  }

  if (roleKey === 'testerId') {
    testerInputValue.value = currentUserId.value;
    testerDisplayName.value = currentUserName.value;
    handleTesterBlur();
  }
};

// Computed Properties
const currentTaskId = computed(() => props.dataSource?.id);
const createdByName = computed(() => props.dataSource?.createdByName);
const execByName = computed(() => props.dataSource?.execByName);
const lastModifiedByName = computed(() => props.dataSource?.lastModifiedByName);

// Current user information
const currentUserId = computed(() => {
  return props.userInfo?.id;
});
const currentUserName = computed(() => {
  return props.userInfo?.fullName;
});

// Assignee information
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
      fullName: assigneeName.value
    }
  };
});

// Confirmer information
const confirmerId = computed(() => props.dataSource?.confirmerId);
const confirmerName = computed(() => props.dataSource?.confirmerName);
const confirmerDefaultOptions = computed(() => {
  const id = confirmerId.value;
  if (!id) {
    return undefined;
  }
  return {
    [id]: {
      id: id,
      fullName: confirmerName.value
    }
  };
});

// Tester information
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
      fullName: testerName.value
    }
  };
});
</script>

<template>
  <div class="h-full text-3 leading-5 px-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('backlog.info.personnel.title') }}
    </div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.creator') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ createdByName }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.assignee') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isAssigneeEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ assigneeName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="startAssigneeEditing">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!assigneeId||assigneeId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('assigneeId')">
            {{ t('backlog.info.personnel.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isAssigneeEditing">
          <SelectUser
            v-show="isAssigneeEditing"
            ref="assigneeSelectRef"
            v-model:value="assigneeInputValue"
            :placeholder="t('backlog.info.personnel.placeholders.selectAssignee')"
            allowClear
            :defaultOptions="assigneeDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="handleAssigneeSelectionChange"
            @blur="handleAssigneeBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.executor') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ execByName || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.confirmer') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isConfirmerEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ confirmerName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="startConfirmerEditing">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!confirmerId||confirmerId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('confirmerId')">
            {{ t('backlog.info.personnel.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isConfirmerEditing">
          <SelectUser
            v-show="isConfirmerEditing"
            ref="confirmerSelectRef"
            v-model:value="confirmerInputValue"
            :placeholder="t('backlog.info.personnel.placeholders.selectConfirmer')"
            allowClear
            :defaultOptions="confirmerDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="handleConfirmerSelectionChange"
            @blur="handleConfirmerBlur" />
        </AsyncComponent>
      </div>

      <div class="relative flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.tester') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isTesterEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ testerName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="startTesterEditing">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!testerId||testerId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('testerId')">
            {{ t('backlog.info.personnel.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isTesterEditing">
          <SelectUser
            v-show="isTesterEditing"
            ref="testerSelectRef"
            v-model:value="testerInputValue"
            :placeholder="t('backlog.info.personnel.placeholders.selectTester')"
            allowClear
            internal
            :defaultOptions="testerDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="left-component"
            @change="handleTesterSelectionChange"
            @blur="handleTesterBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('backlog.info.personnel.lastModifier') }}</span>
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

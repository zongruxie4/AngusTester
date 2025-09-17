<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

// Component props and emits
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
}>();

// Assignee editing state
const assigneeSelectRef = ref();
const isAssigneeEditing = ref(false);
const assigneeDisplayName = ref<string>();
const assigneeInputValue = ref<string>();

// Confirmer editing state
const confirmerSelectRef = ref();
const isConfirmerEditing = ref(false);
const confirmerDisplayName = ref<string>();
const confirmerInputValue = ref<string>();

// Tester editing state
const testerSelectRef = ref();
const isTesterEditing = ref(false);
const testerDisplayName = ref<string>();
const testerInputValue = ref<string>();

// Computed properties
/**
 * Current task ID from data source
 */
const currentTaskId = computed(() => props.dataSource?.id);

/**
 * Name of the user who created the task
 */
const taskCreatedByName = computed(() => props.dataSource?.createdByName);

/**
 * Name of the user who executed the task
 */
const taskExecByName = computed(() => props.dataSource?.execByName);

/**
 * Name of the user who last modified the task
 */
const taskLastModifiedByName = computed(() => props.dataSource?.lastModifiedByName);

/**
 * Current assignee ID from data source
 */
const currentAssigneeId = computed(() => props.dataSource?.assigneeId);

/**
 * Current assignee name from data source
 */
const currentAssigneeName = computed(() => props.dataSource?.assigneeName);

/**
 * Default options for assignee select component
 * Creates an object with assignee ID as key and user info as value
 */
const assigneeDefaultOptions = computed(() => {
  const id = currentAssigneeId.value;
  if (!id) {
    return undefined;
  }
  return {
    [id]: {
      id: id,
      fullName: currentAssigneeName.value
    }
  };
});

/**
 * Current tester ID from data source
 */
const currentTesterId = computed(() => props.dataSource?.testerId);

/**
 * Current tester name from data source
 */
const currentTesterName = computed(() => props.dataSource?.testerName);

/**
 * Default options for tester select component
 * Creates an object with tester ID as key and user info as value
 */
const testerDefaultOptions = computed(() => {
  const id = currentTesterId.value;
  if (!id) {
    return undefined;
  }
  return {
    [id]: {
      id: id,
      fullName: currentTesterName.value
    }
  };
});

/**
 * Current user ID from user info
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Current user name from user info
 */
const currentUserName = computed(() => {
  return props.userInfo?.fullName;
});

/**
 * Current confirmer ID from data source
 */
const currentConfirmerId = computed(() => props.dataSource?.confirmerId);

/**
 * Current confirmer name from data source
 */
const currentConfirmerName = computed(() => props.dataSource?.confirmerName);

/**
 * Default options for confirmer select component
 * Creates an object with confirmer ID as key and user info as value
 */
const confirmerDefaultOptions = computed(() => {
  const id = currentConfirmerId.value;
  if (!id) {
    return undefined;
  }
  return {
    [id]: {
      id: id,
      fullName: currentConfirmerName.value
    }
  };
});

/**
 * Enter assignee editing mode and focus the select
 */
const enterAssigneeEditMode = () => {
  assigneeInputValue.value = currentAssigneeId.value;
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
 * Assign current user to specified role
 * @param roleKey - Role key to assign ('assigneeId', 'confirmerId', 'testerId')
 */
const assignCurrentUserToRole = (roleKey: 'assigneeId' | 'confirmerId' | 'testerId') => {
  if (roleKey === 'assigneeId') {
    assigneeInputValue.value = currentUserId.value;
    assigneeDisplayName.value = currentUserName.value;
    handleAssigneeSelectionBlur();
    return;
  }

  if (roleKey === 'confirmerId') {
    confirmerInputValue.value = currentUserId.value;
    confirmerDisplayName.value = currentUserName.value;
    handleConfirmerSelectionBlur();
  }

  if (roleKey === 'testerId') {
    testerInputValue.value = currentUserId.value;
    testerDisplayName.value = currentUserName.value;
    handleTesterSelectionBlur();
  }
};

/**
 * Handle assignee selection change and update display name
 * @param _event - Selection change event
 * @param option - Selected user option with id and fullName
 */
const handleAssigneeSelectionChange = async (_event: { target: { value: string; } }, option: { id: string; fullName: string; }) => {
  assigneeDisplayName.value = option.fullName;
};

/**
 * Handle assignee selection blur and update task assignee
 */
const handleAssigneeSelectionBlur = async () => {
  const selectedValue = assigneeInputValue.value;
  if (selectedValue === currentAssigneeId.value) {
    isAssigneeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskAssignees(currentTaskId.value, { assigneeId: selectedValue || '' });
  emit('loadingChange', false);
  if (error) {
    if (typeof assigneeSelectRef.value?.focus === 'function') {
      assigneeSelectRef.value?.focus();
    }
    return;
  }

  isAssigneeEditing.value = false;
  emit('change', { id: currentTaskId.value, assigneeId: selectedValue, assigneeName: assigneeDisplayName.value || '' });
};

// Confirmer editing methods
/**
 * Enter confirmer editing mode and focus the select
 */
const enterConfirmerEditMode = () => {
  confirmerInputValue.value = currentConfirmerId.value;
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
 * Handle confirmer selection change and update display name
 * @param _event - Selection change event
 * @param option - Selected user option with id and fullName
 */
const handleConfirmerSelectionChange = async (_event: { target: { value: string; } }, option: { id: string; fullName: string; }) => {
  confirmerDisplayName.value = option.fullName;
};

/**
 * Handle confirmer selection blur and update task confirmer
 */
const handleConfirmerSelectionBlur = async () => {
  const selectedValue = confirmerInputValue.value;
  if (selectedValue === currentConfirmerId.value) {
    isConfirmerEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskConfirmer(currentTaskId.value, { confirmerId: selectedValue || '' });
  emit('loadingChange', false);
  if (error) {
    if (typeof confirmerSelectRef.value?.focus === 'function') {
      confirmerSelectRef.value?.focus();
    }
    return;
  }

  isConfirmerEditing.value = false;
  emit('change', { id: currentTaskId.value, confirmerId: selectedValue, confirmerName: confirmerDisplayName.value || '' });
};

/**
 * Enter tester editing mode and focus the select
 */
const enterTesterEditMode = () => {
  isTesterEditing.value = true;
  testerInputValue.value = currentTesterId.value;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerSelectRef.value?.focus === 'function') {
        testerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * Handle tester selection change and update display name
 * @param _event - Selection change event
 * @param option - Selected user option with id and fullName
 */
const handleTesterSelectionChange = async (_event: { target: { value: string; } }, option: { id: string; fullName: string; }) => {
  testerDisplayName.value = option.fullName;
};

/**
 * Handle tester selection blur and update task tester
 */
const handleTesterSelectionBlur = async () => {
  const selectedValue = testerInputValue.value;
  if (selectedValue === currentTesterId.value) {
    isTesterEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(currentTaskId.value, { testerId: selectedValue });
  emit('loadingChange', false);
  if (error) {
    if (typeof testerSelectRef.value?.focus === 'function') {
      testerSelectRef.value?.focus();
    }
    return;
  }

  isTesterEditing.value = false;
  emit('change', { id: currentTaskId.value, testerId: selectedValue, testerName: testerDisplayName.value! });
};
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('task.detailInfo.personnel.title') }}</div>

    <div class="space-y-2.5">
      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.creator') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskCreatedByName }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.assignee') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isAssigneeEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ currentAssigneeName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="enterAssigneeEditMode">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!currentAssigneeId||currentAssigneeId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('assigneeId')">
            {{ t('task.detailInfo.personnel.actions.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isAssigneeEditing">
          <SelectUser
            v-show="isAssigneeEditing"
            ref="assigneeSelectRef"
            v-model:value="assigneeInputValue"
            :placeholder="t('task.detailInfo.personnel.placeholders.selectAssignee')"
            allowClear
            :defaultOptions="assigneeDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="handleAssigneeSelectionChange"
            @blur="handleAssigneeSelectionBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.executor') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskExecByName || '--' }}</div>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.confirmer') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isConfirmerEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ currentConfirmerName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="enterConfirmerEditMode">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!currentConfirmerId||currentConfirmerId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('confirmerId')">
            {{ t('task.detailInfo.personnel.actions.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isConfirmerEditing">
          <SelectUser
            v-show="isConfirmerEditing"
            ref="confirmerSelectRef"
            v-model:value="confirmerInputValue"
            :placeholder="t('task.detailInfo.personnel.placeholders.selectConfirmer')"
            allowClear
            :defaultOptions="confirmerDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="edit-container"
            @change="handleConfirmerSelectionChange"
            @blur="handleConfirmerSelectionBlur" />
        </AsyncComponent>
      </div>

      <div class="relative flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.tester') }}</span>
          <Colon class="w-1" />
        </div>

        <div v-show="!isTesterEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
          <div>{{ currentTesterName }}</div>
          <Button
            type="link"
            class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
            @click="enterTesterEditMode">
            <Icon icon="icon-shuxie" class="text-3.5" />
          </Button>
          <Button
            v-if="!currentTesterId||currentTesterId!==currentUserId"
            size="small"
            type="link"
            class="p-0 h-5 leading-5 ml-1"
            @click="assignCurrentUserToRole('testerId')">
            {{ t('task.detailInfo.personnel.actions.assignToMe') }}
          </Button>
        </div>

        <AsyncComponent :visible="isTesterEditing">
          <SelectUser
            v-show="isTesterEditing"
            ref="testerSelectRef"
            v-model:value="testerInputValue"
            :placeholder="t('task.detailInfo.personnel.placeholders.selectTester')"
            allowClear
            internal
            :defaultOptions="testerDefaultOptions"
            :action="`${TESTER}/project/${props.projectId}/member/user`"
            :maxlength="80"
            class="left-component"
            @change="handleTesterSelectionChange"
            @blur="handleTesterSelectionBlur" />
        </AsyncComponent>
      </div>

      <div class="flex items-start">
        <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('task.detailInfo.personnel.fields.lastModifier') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ taskLastModifiedByName }}</div>
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

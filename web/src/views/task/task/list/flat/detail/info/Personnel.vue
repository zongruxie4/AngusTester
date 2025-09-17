<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, SelectUser, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';

import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/types';

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Personnel editing state variables
const assigneeSelectRef = ref();
const isAssigneeEditing = ref(false);
const assigneeSelectMessage = ref<string>();
const assigneeSelectValue = ref<string>();

const confirmerSelectRef = ref();
const isConfirmerEditing = ref(false);
const confirmerSelectMessage = ref<string>();
const confirmerSelectValue = ref<string>();

const testerSelectRef = ref();
const isTesterEditing = ref(false);
const testerSelectMessage = ref<string>();
const testerSelectValue = ref<string>();

// Personnel editing methods
/**
 * <p>Initiates assignee editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startAssigneeEditing = () => {
  assigneeSelectValue.value = currentAssigneeId.value;
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
 * <p>Assigns the current user to the specified role (assignee, confirmer, or tester).</p>
 * <p>Sets the user ID and name, then triggers the blur event to save changes.</p>
 * @param key - The role key to assign the current user to
 */
const assignCurrentUserToRole = (key:'assigneeId'|'confirmerId'|'testerId') => {
  if (key === 'assigneeId') {
    assigneeSelectValue.value = currentUserId.value;
    assigneeSelectMessage.value = currentUserName.value;
    handleAssigneeBlur();
    return;
  }

  if (key === 'confirmerId') {
    confirmerSelectValue.value = currentUserId.value;
    confirmerSelectMessage.value = currentUserName.value;
    handleConfirmerBlur();
  }

  if (key === 'testerId') {
    testerSelectValue.value = currentUserId.value;
    testerSelectMessage.value = currentUserName.value;
    handleTesterBlur();
  }
};

/**
 * <p>Handles assignee selection change to update the message display.</p>
 * @param value - Selected assignee ID
 * @param option - Selected assignee option containing id and fullName
 */
const handleAssigneeChange = async (
  value: any,
  option: any) => {
  assigneeSelectMessage.value = option.fullName;
};

/**
 * <p>Handles assignee select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update task assignee if value has changed.</p>
 */
const handleAssigneeBlur = async () => {
  const newValue = assigneeSelectValue.value;
  if (!newValue || newValue === currentAssigneeId.value) {
    isAssigneeEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskAssignees(currentTaskId.value, { assigneeId: newValue });
  emit('loadingChange', false);
  if (error) {
    if (typeof assigneeSelectRef.value?.focus === 'function') {
      assigneeSelectRef.value?.focus();
    }

    return;
  }

  isAssigneeEditing.value = false;
  emit('change', { id: currentTaskId.value, assigneeId: newValue, assigneeName: assigneeSelectMessage.value! });
};

/**
 * <p>Initiates confirmer editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startConfirmerEditing = () => {
  confirmerSelectValue.value = currentConfirmerId.value;
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
 * <p>Handles confirmer selection change to update the message display.</p>
 * @param value - Selected confirmer ID
 * @param option - Selected confirmer option containing id and fullName
 */
const handleConfirmerChange = async (value: any, option: any) => {
  confirmerSelectMessage.value = option.fullName;
};

/**
 * <p>Handles confirmer select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update task confirmer if value has changed.</p>
 */
const handleConfirmerBlur = async () => {
  const newValue = confirmerSelectValue.value;
  if (newValue === currentConfirmerId.value) {
    isConfirmerEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.editTaskConfirmer(currentTaskId.value, { confirmerId: newValue ?? null });
  emit('loadingChange', false);
  if (error) {
    if (typeof confirmerSelectRef.value?.focus === 'function') {
      confirmerSelectRef.value?.focus();
    }

    return;
  }

  isConfirmerEditing.value = false;
  emit('change', { id: currentTaskId.value, confirmerId: newValue, confirmerName: confirmerSelectMessage.value! });
};

/**
 * <p>Initiates tester editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startTesterEditing = () => {
  isTesterEditing.value = true;
  testerSelectValue.value = currentTesterId.value;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerSelectRef.value?.focus === 'function') {
        testerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Handles tester selection change to update the message display.</p>
 * @param value - Selected tester ID
 * @param option - Selected tester option containing id and fullName
 */
const handleTesterChange = async (value: any, option: any) => {
  testerSelectMessage.value = option.fullName;
};

/**
 * <p>Handles tester select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update task tester if value has changed.</p>
 */
const handleTesterBlur = async () => {
  const newValue = testerSelectValue.value;
  if (newValue === currentTesterId.value) {
    isTesterEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await task.updateTask(currentTaskId.value, { testerId: newValue });
  emit('loadingChange', false);
  if (error) {
    if (typeof testerSelectRef.value?.focus === 'function') {
      testerSelectRef.value?.focus();
    }

    return;
  }

  isTesterEditing.value = false;
  emit('change', { id: currentTaskId.value, testerId: newValue, testerName: testerSelectMessage.value! });
};

// Computed properties for task personnel data
const currentTaskId = computed(() => props.dataSource?.id);
const currentCreatedByName = computed(() => props.dataSource?.createdByName);
const currentExecByName = computed(() => props.dataSource?.execByName);
const currentLastModifiedByName = computed(() => props.dataSource?.lastModifiedByName);

/**
 * <p>Gets the current assignee ID from the task data source.</p>
 * @returns The assignee ID or undefined if not available
 */
const currentAssigneeId = computed(() => props.dataSource?.assigneeId);

/**
 * <p>Gets the current assignee name from the task data source.</p>
 * @returns The assignee name or undefined if not available
 */
const currentAssigneeName = computed(() => props.dataSource?.assigneeName);

/**
 * <p>Creates default options for the assignee select component.</p>
 * <p>Returns an object with the current assignee's ID and full name for display purposes.</p>
 * @returns Object containing assignee data or undefined if no assignee is set
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
 * <p>Gets the current tester ID from the task data source.</p>
 * @returns The tester ID or undefined if not available
 */
const currentTesterId = computed(() => props.dataSource?.testerId);

/**
 * <p>Gets the current tester name from the task data source.</p>
 * @returns The tester name or undefined if not available
 */
const currentTesterName = computed(() => props.dataSource?.testerName);

/**
 * <p>Creates default options for the tester select component.</p>
 * <p>Returns an object with the current tester's ID and full name for display purposes.</p>
 * @returns Object containing tester data or undefined if no tester is set
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
 * <p>Gets the current user ID from the user info props.</p>
 * @returns The current user ID or undefined if not available
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * <p>Gets the current user name from the user info props.</p>
 * @returns The current user full name or undefined if not available
 */
const currentUserName = computed(() => {
  return props.userInfo?.fullName;
});

/**
 * <p>Gets the current confirmer ID from the task data source.</p>
 * @returns The confirmer ID or undefined if not available
 */
const currentConfirmerId = computed(() => props.dataSource?.confirmerId);

/**
 * <p>Gets the current confirmer name from the task data source.</p>
 * @returns The confirmer name or undefined if not available
 */
const currentConfirmerName = computed(() => props.dataSource?.confirmerName);

/**
 * <p>Creates default options for the confirmer select component.</p>
 * <p>Returns an object with the current confirmer's ID and full name for display purposes.</p>
 * @returns Object containing confirmer data or undefined if no confirmer is set
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
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3">{{ t('task.detailInfo.personnel.title') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="relative flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.personnel.fields.assignee') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isAssigneeEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentAssigneeName }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startAssigneeEditing">
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
              v-model:value="assigneeSelectValue"
              :placeholder="t('task.detailInfo.personnel.placeholders.selectAssignee')"
              internal
              :defaultOptions="assigneeDefaultOptions"
              :action="`${TESTER}/project/${props.projectId}/member/user`"
              :maxlength="80"
              class="left-component"
              @change="handleAssigneeChange"
              @blur="handleAssigneeBlur" />
          </AsyncComponent>
        </div>

        <div class="relative flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.personnel.fields.executor') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ currentExecByName || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.personnel.fields.confirmer') }}</span>
            <Colon class="w-1" />
          </div>

          <div v-show="!isConfirmerEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div>{{ currentConfirmerName }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startConfirmerEditing">
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
              v-model:value="confirmerSelectValue"
              :placeholder="t('task.detailInfo.personnel.placeholders.selectConfirmer')"
              allowClear
              internal
              :defaultOptions="confirmerDefaultOptions"
              :action="`${TESTER}/project/${props.projectId}/member/user`"
              :maxlength="80"
              class="left-component"
              @change="handleConfirmerChange"
              @blur="handleConfirmerBlur" />
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
              @click="startTesterEditing">
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
              v-model:value="testerSelectValue"
              :placeholder="t('task.detailInfo.personnel.placeholders.selectTester')"
              allowClear
              internal
              :defaultOptions="testerDefaultOptions"
              :action="`${TESTER}/project/${props.projectId}/member/user`"
              :maxlength="80"
              class="left-component"
              @change="handleTesterChange"
              @blur="handleTesterBlur" />
          </AsyncComponent>
        </div>

        <div class="relative flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.personnel.fields.creator') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ currentCreatedByName }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('task.detailInfo.personnel.fields.lastModifier') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ currentLastModifiedByName }}</div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
.border-none {
  border: none;
}

.left-component {
  position: absolute;
  top: -4px;
  left: 74px;
  width: calc(100% - 74px);
}
</style>

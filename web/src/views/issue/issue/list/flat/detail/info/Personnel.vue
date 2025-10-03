<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, SelectUser, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

import { TaskDetail } from '@/views/issue/types';

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
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
  (event: 'change', value: Partial<TaskDetail>): void;
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
  _value: any,
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
const handleConfirmerChange = async (_value: any, option: any) => {
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
const handleTesterChange = async (_value: any, option: any) => {
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
      <div class="text-3.5">{{ t('common.personnel') }}</div>
    </template>

    <template #default>
      <div class="personnel-info-container">
        <!-- Assignee -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.assignee') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isAssigneeEditing" class="info-value-content">
                <span :class="{ 'placeholder-text': !currentAssigneeName }" class="info-text">
                  {{ currentAssigneeName || '--' }}
                </span>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="startAssigneeEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
                <Button
                  v-if="!currentAssigneeId||currentAssigneeId!==currentUserId"
                  size="small"
                  type="link"
                  class="assign-to-me-btn"
                  @click="assignCurrentUserToRole('assigneeId')">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>

              <AsyncComponent :visible="isAssigneeEditing">
                <SelectUser
                  v-show="isAssigneeEditing"
                  ref="assigneeSelectRef"
                  v-model:value="assigneeSelectValue"
                  :placeholder="t('common.placeholders.selectAssignee')"
                  internal
                  :defaultOptions="assigneeDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80"
                  class="edit-select"
                  @change="handleAssigneeChange"
                  @blur="handleAssigneeBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Executor -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.executor') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !currentExecByName }" class="info-text">
                {{ currentExecByName || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Confirmer -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.confirmer') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isConfirmerEditing" class="info-value-content">
                <span :class="{ 'placeholder-text': !currentConfirmerName }" class="info-text">
                  {{ currentConfirmerName || '--' }}
                </span>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="startConfirmerEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
                <Button
                  v-if="!currentConfirmerId||currentConfirmerId!==currentUserId"
                  size="small"
                  type="link"
                  class="assign-to-me-btn"
                  @click="assignCurrentUserToRole('confirmerId')">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>

              <AsyncComponent :visible="isConfirmerEditing">
                <SelectUser
                  v-show="isConfirmerEditing"
                  ref="confirmerSelectRef"
                  v-model:value="confirmerSelectValue"
                  :placeholder="t('common.placeholders.selectConfirmer')"
                  allowClear
                  internal
                  :defaultOptions="confirmerDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80"
                  class="edit-select"
                  @change="handleConfirmerChange"
                  @blur="handleConfirmerBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Tester -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.tester') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isTesterEditing" class="info-value-content">
                <span :class="{ 'placeholder-text': !currentTesterName }" class="info-text">
                  {{ currentTesterName || '--' }}
                </span>
                <Button
                  type="link"
                  class="edit-btn"
                  @click="startTesterEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
                <Button
                  v-if="!currentTesterId||currentTesterId!==currentUserId"
                  size="small"
                  type="link"
                  class="assign-to-me-btn"
                  @click="assignCurrentUserToRole('testerId')">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>

              <AsyncComponent :visible="isTesterEditing">
                <SelectUser
                  v-show="isTesterEditing"
                  ref="testerSelectRef"
                  v-model:value="testerSelectValue"
                  :placeholder="t('common.placeholders.selectTester')"
                  allowClear
                  internal
                  :defaultOptions="testerDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80"
                  class="edit-select"
                  @change="handleTesterChange"
                  @blur="handleTesterBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Creator -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.creator') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ currentCreatedByName }}</span>
            </div>
          </div>
        </div>

        <!-- Last Modifier -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.modifier') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ currentLastModifiedByName }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Main Container */
.personnel-info-container {
  padding: 1rem 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* Info Row Layout */
.info-row {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  align-items: stretch;
}

/* Individual Info Item */
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.25rem;
  min-height: 2rem;
}

/* Label Styling */
.info-label {
  flex-shrink: 0;
  width: 5rem;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-label span {
  font-size: 0.75rem;
  font-weight: 400;
  color: #7c8087;
  line-height: 1.2;
  word-break: break-word;
  white-space: normal;
}

/* Value Styling */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-text {
  font-size: 0.75rem;
  font-weight: 400;
  color: #374151;
  line-height: 1.4;
  word-break: break-word;
  white-space: normal;
}

/* Value Content Container */
.info-value-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  flex-wrap: wrap;
}

/* Edit Button */
.edit-btn {
  padding: 0.125rem;
  height: 1.25rem;
  width: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.edit-btn:hover {
  background-color: #f3f4f6;
}

/* Assign to Me Button */
.assign-to-me-btn {
  padding: 0.125rem 0.25rem;
  height: 1.25rem;
  font-size: 0.625rem;
  line-height: 1;
  border-radius: 0.25rem;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.assign-to-me-btn:hover {
  background-color: #f3f4f6;
}

/* Edit Select */
.edit-select {
  width: 100%;
  max-width: 20rem;
  font-size: 0.75rem;
}

/* Placeholder text styling */
.placeholder-text {
  color: #7c8087 !important;
  font-weight: 400 !important;
}

/* Responsive Design */
@media (max-width: 768px) {
  .personnel-info-container {
    padding: 0.75rem 1rem;
    gap: 0.25rem;
  }

  .info-row {
    gap: 0.25rem;
  }

  .info-item {
    gap: 0.25rem;
  }

  .info-label {
    width: 4.5rem;
  }

  .edit-select {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .personnel-info-container {
    padding: 0.5rem 0.75rem;
    gap: 0.125rem;
  }

  .info-label {
    width: 4rem;
  }

  .info-label span {
    font-size: 0.6875rem;
  }

  .info-text {
    font-size: 0.6875rem;
  }

  .assign-to-me-btn {
    font-size: 0.5625rem;
    padding: 0.125rem;
  }
}

/* Animation for smooth transitions */
.info-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Legacy support */
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

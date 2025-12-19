<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { issue } from '@/api/tester';

import { TaskDetail } from '../../types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
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
  const [error] = await issue.editTaskAssignees(currentTaskId.value, { assigneeId: newAssigneeId as string });
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
  const [error] = await issue.editTaskConfirmer(currentTaskId.value, { confirmerId: newConfirmerId as string });
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
  const [error] = await issue.updateTask(currentTaskId.value, { testerId: newTesterId });
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
const creator = computed(() => props.dataSource?.creator);
const modifier = computed(() => props.dataSource?.modifier);

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
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.personnel') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Creator -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.creator') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ creator }}</span>
          </div>
        </div>

        <!-- Assignee -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.assignee') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isAssigneeEditing" class="info-value-content">
              <span class="info-text">{{ assigneeName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startAssigneeEditing">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!assigneeId||assigneeId!==currentUserId"
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
                v-model:value="assigneeInputValue"
                :placeholder="t('common.placeholders.selectAssignee')"
                allowClear
                :defaultOptions="assigneeDefaultOptions"
                :action="`${TESTER}/project/${props.projectId}/member/user`"
                :maxlength="80"
                class="edit-input"
                @change="handleAssigneeSelectionChange"
                @blur="handleAssigneeBlur" />
            </AsyncComponent>
          </div>
        </div>


        <!-- Confirmer -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.confirmer') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isConfirmerEditing" class="info-value-content">
              <span class="info-text">{{ confirmerName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startConfirmerEditing">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!confirmerId||confirmerId!==currentUserId"
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
                v-model:value="confirmerInputValue"
                :placeholder="t('common.placeholders.selectConfirmer')"
                allowClear
                :defaultOptions="confirmerDefaultOptions"
                :action="`${TESTER}/project/${props.projectId}/member/user`"
                :maxlength="80"
                class="edit-input"
                @change="handleConfirmerSelectionChange"
                @blur="handleConfirmerBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Tester -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.tester') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTesterEditing" class="info-value-content">
              <span class="info-text">{{ testerName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="startTesterEditing">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!testerId||testerId!==currentUserId"
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
                v-model:value="testerInputValue"
                :placeholder="t('common.placeholders.selectTester')"
                allowClear
                internal
                :defaultOptions="testerDefaultOptions"
                :action="`${TESTER}/project/${props.projectId}/member/user`"
                :maxlength="80"
                class="edit-input"
                @change="handleTesterSelectionChange"
                @blur="handleTesterBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Last Modifier -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.modifier') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ modifier }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Info row styles */
.info-row {
  display: flex;
  align-items: flex-start;
  min-height: auto;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

/* Label styles */
.info-label {
  flex-shrink: 0;
  width: 80px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #686868;
  font-weight: 500;
  line-height: 1.4;
}

.info-label span {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

/* Value area styles */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.info-value-content {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-height: 20px;
  flex: 1;
  min-width: 0;
}

.info-text {
  font-size: 12px;
  color: #262626;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
  min-width: 0;
}

.info-text.dash-text {
  color: #8c8c8c;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
  margin-left: auto;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}

/* Assign to me button styles */
.assign-to-me-btn {
  font-size: 12px;
  height: 18px;
  line-height: 16px;
  margin-left: 4px;
}

/* Edit input styles */
.edit-input {
  width: 100%;
  font-size: 12px;
}

/* Legacy style compatibility */
.border-none {
  border: none;
}

.edit-container {
  width: 100%;
  transform: translateY(-5px);
}
</style>

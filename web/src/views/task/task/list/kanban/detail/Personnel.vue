<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Colon, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskDetail } from '@/views/task/types';
import { AssocCaseProps } from '@/views/task/task/list/types';

// Component props and emits
const props = withDefaults(defineProps<AssocCaseProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
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
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('task.detailInfo.personnel.title') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Creator -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.creator') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ taskCreatedByName }}</span>
          </div>
        </div>

        <!-- Assignee -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.assignee') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isAssigneeEditing" class="info-value-content">
              <span class="info-text">{{ currentAssigneeName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterAssigneeEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!currentAssigneeId||currentAssigneeId!==currentUserId"
                size="small"
                type="link"
                class="assign-to-me-btn"
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
                class="edit-input"
                @change="handleAssigneeSelectionChange"
                @blur="handleAssigneeSelectionBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Executor -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.executor') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !taskExecByName }">{{ taskExecByName || '--' }}</span>
          </div>
        </div>

        <!-- Confirmer -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.confirmer') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isConfirmerEditing" class="info-value-content">
              <span class="info-text">{{ currentConfirmerName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterConfirmerEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!currentConfirmerId||currentConfirmerId!==currentUserId"
                size="small"
                type="link"
                class="assign-to-me-btn"
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
                class="edit-input"
                @change="handleConfirmerSelectionChange"
                @blur="handleConfirmerSelectionBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Tester -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.tester') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isTesterEditing" class="info-value-content">
              <span class="info-text">{{ currentTesterName }}</span>
              <Button
                type="link"
                class="edit-btn"
                @click="enterTesterEditMode">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="!currentTesterId||currentTesterId!==currentUserId"
                size="small"
                type="link"
                class="assign-to-me-btn"
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
                class="edit-input left-component"
                @change="handleTesterSelectionChange"
                @blur="handleTesterSelectionBlur" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Last Modifier -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('task.detailInfo.personnel.fields.lastModifier') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ taskLastModifiedByName }}</span>
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
.scrollable-content { flex: 1; overflow-y: auto; padding: 0; }

/* Content area styles */
.basic-info-content { padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }

/* Info row styles */
.info-row { display: flex; align-items: flex-start; min-height: auto; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }

/* Label styles */
.info-label { flex-shrink: 0; width: 100px; display: flex; align-items: center; font-size: 12px; color: #686868; font-weight: 500; line-height: 1.4; }
.info-label span { white-space: normal; word-break: break-word; line-height: 1.4; }

/* Value area styles */
.info-value { flex: 1; min-width: 0; display: flex; align-items: flex-start; justify-content: space-between; }
.info-value-content { display: flex; align-items: center; gap: 6px; width: 100%; min-height: 20px; flex: 1; min-width: 0; }

/* Text styles */
.info-text { font-size: 12px; color: #262626; line-height: 1.4; word-break: break-word; flex: 1; min-width: 0; }
.info-text.dash-text { color: #8c8c8c; }

/* Edit button styles */
.edit-btn { flex-shrink: 0; padding: 0; height: 16px; width: 16px; display: flex; align-items: center; justify-content: center; border: none; background: none; color: #1890ff !important; cursor: pointer; transition: color 0.2s; margin-left: auto; }
.edit-btn:focus { color: #1890ff !important; background: none !important; border: none !important; box-shadow: none !important; }
.edit-btn:hover { color: #1890ff; }
.edit-btn .anticon { font-size: 12px; }

/* Assign to me button styles */
.assign-to-me-btn { font-size: 12px; height: 18px; line-height: 16px; margin-left: 4px; }

/* Edit input styles */
.edit-input { width: 100%; font-size: 12px; }
.left-component { transform: translateX(-10px); }

/* Legacy style compatibility */
.border-none { border: none; }
.edit-container { width: 100%; transform: translateY(-5px); }
</style>

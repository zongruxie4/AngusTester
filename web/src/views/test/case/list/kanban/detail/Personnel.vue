<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, SelectUser } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';

import { CaseDetail } from '@/views/test/types';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});


const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseDetail): void;
  (event: 'update:dataSource', value: CaseDetail): void;
}>();

const testerRef = ref();
const testerEditFlag = ref(false);
const testerMessage = ref<string>();
const testerIdValue = ref<number>();

const developerName = computed(() => props.dataSource?.developerName);
const reviewerName = computed(() => props.dataSource?.reviewerName);
const createdByName = computed(() => props.dataSource?.createdByName);
const lastModifiedByName = computed(() => props.dataSource?.lastModifiedByName);

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

const userId = computed(() => {
  return props.userInfo?.id;
});

const userName = computed(() => {
  return props.userInfo?.fullName;
});

/*
  Enter tester edit mode and autofocus the selector.
*/
const openEditAssignee = () => {
  testerIdValue.value = testerId.value;
  testerEditFlag.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerRef.value?.focus === 'function') {
        testerRef.value?.focus();
      }
    }, 100);
  });
};

/*
  Assign current user as tester and commit immediately.
*/
const assignToMe = () => {
  testerIdValue.value = userId.value;
  testerMessage.value = userName.value;
  commitTesterIfChanged();
};

/*
  Handle tester selection change; store display name for feedback.
*/
const handleTesterChange = async (
  _event: { target: { value: string; } },
  option: { id: string; fullName: string; }
) => {
  testerMessage.value = option.fullName;
};

/*
  Commit tester selection if changed; otherwise close edit mode.
*/
const commitTesterIfChanged = async () => {
  const value = testerIdValue.value;
  if (value === testerId.value) {
    testerEditFlag.value = false;
    return;
  }

  emitLoadingChange(true);
  const [error] = await testCase.updateCase([{
    id: props.dataSource?.id,
    testerId: value
  }]);
  emitLoadingChange(false);
  if (error) {
    if (typeof testerRef.value?.focus === 'function') {
      testerRef.value?.focus();
    }
    return;
  }

  testerEditFlag.value = false;
  refreshCaseDetail();
};

/*
  Emit loading state to parent.
*/
const emitLoadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

/*
  Refresh case detail and sync to parent.
*/
const refreshCaseDetail = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  emitLoadingChange(true);
  const [error, res] = await testCase.getCaseDetail(id);
  emitLoadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};
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
            <span class="info-text">{{ createdByName }}</span>
          </div>
        </div>

        <!-- Tester -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.tester') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!testerEditFlag" class="info-value-content">
              <span class="info-text">{{ testerName }}</span>
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditAssignee">
                <Icon icon="icon-shuxie" />
              </Button>
              <Button
                v-if="props.canEdit && (!testerId || testerId !== userId)"
                size="small"
                type="link"
                class="assign-to-me-btn"
                @click="assignToMe">
                {{ t('actions.assignToMe') }}
              </Button>
            </div>
            <AsyncComponent :visible="testerEditFlag">
              <SelectUser
                v-show="testerEditFlag"
                ref="testerRef"
                v-model:value="testerIdValue"
                :placeholder="t('common.placeholders.selectTester')"
                allowClear
                :defaultOptions="testerDefaultOptions"
                :action="`${TESTER}/project/${props.projectId}/member/user`"
                :maxlength="80"
                class="edit-input"
                @change="handleTesterChange"
                @blur="commitTesterIfChanged" />
            </AsyncComponent>
          </div>
        </div>

        <!-- Developer -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.developer') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !developerName }">{{ developerName || '--' }}</span>
          </div>
        </div>

        <!-- Reviewer -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.reviewer') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !reviewerName }">{{ reviewerName || '--' }}</span>
          </div>
        </div>

        <!-- Last Modifier -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.modifier') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ lastModifiedByName }}</span>
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
  width: 100px;
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

/* Text styles */
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
</style>

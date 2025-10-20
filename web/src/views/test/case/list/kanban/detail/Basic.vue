<script setup lang="ts">
import { defineAsyncComponent, inject, nextTick, ref, Ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Input, Popover, ReviewStatus, Select } from '@xcan-angus/vue-ui';
import { TESTER, EvalWorkloadMethod } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

import TaskPriority from '@/components/task/TaskPriority/index.vue';
import TestResult from '@/components/test/TestResult/index.vue';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';

// Async sub-sections
const Description = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Description.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/Precondition.vue'));
const TestStep = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/TestSteps.vue'));

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

// Injected states
const projectId = inject<Ref<string>>('projectId', ref(''));

const nameInputRef = ref();
const tagsSelectRef = ref();
const priorityRef = ref();
const evalWorkloadInputRef = ref();
const actualWorkloadInputRef = ref();

const isEditName = ref(false);
const tagsIds = ref<number[]>([]);
const defaultTags = ref<{[key: string]: { label: string; value: number }}>({});
const isEditTag = ref(false);
const isEditPriority = ref(false);
const priority = ref();
const isEditEvalWorkload = ref(false);
const alWorkload = ref(props.dataSource?.actualWorkload);
const isEditActualWorkload = ref(false);

/*
  Coerce select option to expected Priority shape for display component.
*/
const coercePriorityOption = (option: any) => ({
  value: option.value,
  label: option.label || option.message || option.value
});

/*
  Enter name edit mode and autofocus the input.
*/
const openEditName = () => {
  isEditName.value = true;
  nextTick(() => {
    nameInputRef.value.focus();
  });
};

/*
  Persist updated name if changed and reload detail.
*/
const editName = async (event) => {
  if (event.target.value === props.dataSource?.name || !event.target.value) {
    isEditName.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await testCase.putName(props.dataSource.id, event.target.value);
  loadingChange(false);
  isEditName.value = false;
  if (error) {
    return;
  }

  change();
};

/*
  Enter priority edit mode and autofocus the select.
*/
const openEditPriority = () => {
  isEditPriority.value = true;
  priority.value = props.dataSource?.priority?.value;
  nextTick(() => {
    priorityRef.value.focus();
  });
};

/*
  Persist updated priority if changed and reload detail.
*/
const editPriority = async (value) => {
  if (value === props.dataSource?.priority?.value) {
    isEditPriority.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await testCase.putPriority(props.dataSource.id, value);
  loadingChange(false);
  isEditPriority.value = false;
  if (error) {
    return;
  }

  change();
};

/*
  Enter eval workload edit mode and autofocus input.
*/
const openEditEvalWorkload = () => {
  isEditEvalWorkload.value = true;
  nextTick(() => {
    evalWorkloadInputRef.value.focus();
  });
};

/*
  Persist updated eval workload if changed and reload detail.
*/
const editEvalWorkload = async (event) => {
  if (+event.target.value === (+props.dataSource?.evalWorkload) || (!event.target.value && !props.dataSource?.evalWorkload)) {
    isEditEvalWorkload.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await testCase.putEvalWorkload(props.dataSource.id, { workload: String(event.target.value) });
  loadingChange(false);
  isEditEvalWorkload.value = false;
  if (error) {
    return;
  }

  change();
};

/*
  Enter actual workload edit mode and autofocus input.
*/
const openEditActualWorkload = () => {
  alWorkload.value = props.dataSource.actualWorkload || props.dataSource.evalWorkload;
  isEditActualWorkload.value = true;
  nextTick(() => {
    actualWorkloadInputRef.value.focus();
  });
};

/*
  Persist updated actual workload if changed and reload detail.
*/
const editActualWorkload = async (event) => {
  if (+event.target.value === (+props.dataSource?.actualWorkload) || (!event.target.value && !props.dataSource?.evalWorkload)) {
    isEditActualWorkload.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await testCase.putActualWorkload(props.dataSource.id, { workload: String(event.target.value) });
  loadingChange(false);
  isEditActualWorkload.value = false;
  if (error) {
    return;
  }

  change();
};

/*
  Enter tag edit mode and preload current tags.
*/
const openEditTag = () => {
  tagsIds.value = (props.dataSource?.tags || [])?.map(item => {
    defaultTags.value[item.id] = { value: item.id, label: item.name };
    return item.id;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    tagsSelectRef.value.focus();
  });
};

/*
  Persist updated tag list if changed and reload detail.
*/
const editTag = async () => {
  if (tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }

  const oldTagIds = props.dataSource?.tags?.map(item => item.id) || [];
  const equalFlag = isEqual(oldTagIds, tagsIds.value);
  if (equalFlag) {
    isEditTag.value = false;
    return;
  }
  const params = { tagIds: tagsIds.value.length ? tagsIds.value : null };
  loadingChange(true);
  const [error] = await testCase.putTag(props.dataSource.id, params);
  loadingChange(false);
  isEditTag.value = false;
  if (error) {
    return;
  }

  change();
};

/*
  Emit loading state to parent.
*/
const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

/*
  Refresh case detail and sync to parent.
*/
const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await testCase.getCaseDetail(id);
  loadingChange(false);
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
      <h3 class="basic-info-title">{{ t('common.basicInfo') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Case Code -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.code') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">{{ props.dataSource?.code }}</span>
            <div
              v-if="props.dataSource?.overdue"
              class="overdue-badge">
              {{ t('status.overdue') }}
            </div>
          </div>
        </div>

        <!-- Case Name -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.name') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEditName" class="info-value-content">
              <span class="info-text">{{ props.dataSource?.name }}</span>
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditName">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <Input
              v-show="isEditName"
              ref="nameInputRef"
              :value="props.dataSource?.name"
              :allowClear="false"
              :maxlength="200"
              size="small"
              class="edit-input"
              :placeholder="t('common.name')"
              @blur="editName" />
          </div>
        </div>

        <!-- Review Status -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.reviewStatus') }}</span>
          </div>
          <div class="info-value">
            <ReviewStatus v-if="props.dataSource?.reviewStatus" :value="props.dataSource?.reviewStatus" />
            <span v-else class="info-text dash-text">--</span>
          </div>
        </div>

        <!-- Version -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.version') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.softwareVersion }">
              {{ props.dataSource?.softwareVersion || '--' }}
            </span>
          </div>
        </div>

        <!-- Software Version -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.softwareVersion') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.softwareVersion }">
              {{ props.dataSource?.softwareVersion || '--' }}
            </span>
          </div>
        </div>

        <!-- Priority -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.priority') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEditPriority" class="info-value-content">
              <TaskPriority :value="props.dataSource?.priority" />
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditPriority">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <SelectEnum
              v-show="isEditPriority"
              ref="priorityRef"
              v-model:value="priority"
              :allowClear="false"
              :autofocus="isEditPriority"
              enumKey="Priority"
              size="small"
              class="edit-input"
              :placeholder="t('common.placeholders.selectPriority')"
              @blur="editPriority($event.target.value)">
              <template #option="item">
                <TaskPriority :value="coercePriorityOption(item)" />
              </template>
            </SelectEnum>
          </div>
        </div>

        <!-- Tags -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.tag') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEditTag" class="info-value-content">
              <div v-if="props.dataSource?.tags?.length" class="tags-container">
                <div
                  v-for="tag in props.dataSource?.tags"
                  :key="tag.id"
                  class="tag-item">
                  {{ tag.name }}
                </div>
              </div>
              <span v-else class="info-text dash-text">--</span>
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditTag">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <Select
              v-show="isEditTag"
              ref="tagsSelectRef"
              v-model:value="tagsIds"
              showSearch
              :defaultOptions="defaultTags"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTags="5"
              :placeholder="t('common.placeholders.selectTag')"
              :class="{'border-error':tagsIds && tagsIds.length > 5 }"
              :action="`${TESTER}/tag?projectId=${projectId}&fullTextSearch=true`"
              mode="multiple"
              size="small"
              class="edit-input"
              @blur="editTag" />
          </div>
        </div>

        <!-- Plan -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.plan') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.planName }">
              {{ props.dataSource?.planName || '--' }}
            </span>
          </div>
        </div>

        <!-- Module -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.module') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.moduleName }">
              {{ props.dataSource?.moduleName || '--' }}
            </span>
          </div>
        </div>

        <!-- Test Result -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.testResult') }}</span>
          </div>
          <div class="info-value">
            <TestResult :value="props.dataSource?.testResult" />
          </div>
        </div>

        <!-- Eval Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.evalWorkload') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEditEvalWorkload" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.evalWorkload }">
                {{ props.dataSource?.evalWorkload || '--' }}
              </span>
              <Button
                v-if="props.canEdit"
                type="link"
                class="edit-btn"
                @click="openEditEvalWorkload">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <Input
              v-show="isEditEvalWorkload"
              ref="evalWorkloadInputRef"
              :value="props.dataSource?.evalWorkload !== undefined && props.dataSource?.evalWorkload !== null ? String(props.dataSource?.evalWorkload) : ''"
              :allowClear="false"
              :autofocus="isEditEvalWorkload"
              :min="0.1"
              :max="1000"
              :placeholder="t('common.placeholders.inputEvalWorkload')"
              dataType="float"
              size="small"
              class="edit-input"
              @blur="editEvalWorkload" />
          </div>
        </div>

        <!-- Actual Workload -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.actualWorkload') }}</span>
          </div>
          <div class="info-value">
            <div v-show="!isEditActualWorkload" class="info-value-content">
              <span class="info-text" :class="{ 'dash-text': !props.dataSource?.actualWorkload }">
                {{ props.dataSource?.actualWorkload || '--' }}
              </span>
              <Button
                v-if="props.canEdit && props.dataSource?.evalWorkload"
                type="link"
                class="edit-btn"
                @click="openEditActualWorkload">
                <Icon icon="icon-shuxie" />
              </Button>
            </div>
            <Input
              v-show="isEditActualWorkload"
              ref="actualWorkloadInputRef"
              :value="alWorkload !== undefined && alWorkload !== null ? String(alWorkload) : ''"
              :allowClear="false"
              :autofocus="isEditActualWorkload"
              :min="0.1"
              :max="1000"
              :placeholder="t('common.placeholders.inputActualWorkload')"
              dataType="float"
              size="small"
              class="edit-input"
              @blur="editActualWorkload" />
          </div>
        </div>

        <!-- Unplanned -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.unplanned') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text">
              {{ props.dataSource?.unplanned ? t('status.yes') : t('status.no') }}
            </span>
          </div>
        </div>
      </div>

      <!-- Sub-sections -->
      <div class="description-section">
        <Precondition
          :projectId="props.projectId"
          :appInfo="props.appInfo"
          :dataSource="props.dataSource"
          :canEdit="props.canEdit"
          @change="change"
          @loadingChange="loadingChange" />

        <TestStep
          :projectId="props.projectId"
          :appInfo="props.appInfo"
          :dataSource="props.dataSource"
          :canEdit="props.canEdit"
          @change="change"
          @loadingChange="loadingChange" />

        <Description
          :projectId="props.projectId"
          :appInfo="props.appInfo"
          :dataSource="props.dataSource"
          :canEdit="props.canEdit"
          @change="change"
          @loadingChange="loadingChange" />
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

/* Description section styles */
.description-section {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
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
  width: 70px;
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

/* Edit input styles */
.edit-input {
  width: 100%;
  font-size: 12px;
}

/* Tags container styles */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}
.tag-item {
  font-size: 10px;
  padding: 2px 6px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  color: #595959;
  line-height: 1.2;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Overdue badge */
.overdue-badge {
  font-size: 10px;
  padding: 1px 4px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 2px;
  color: #ff4d4f;
  line-height: 1.2;
  margin-left: 6px;
}
</style>

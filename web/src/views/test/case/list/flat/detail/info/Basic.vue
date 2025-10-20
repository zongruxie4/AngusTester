<script setup lang="ts">
import { nextTick, ref } from 'vue';
import { Icon, Input, Select, ReviewStatus, Toggle } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { Priority, TESTER, utils, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { SoftwareVersionStatus } from '@/enums/enums';
import { CaseActionAuth } from '@/views/test/case/types';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import TaskPriority from '@/components/task/TaskPriority/index.vue';
import TestResult from '@/components/test/TestResult/index.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: CaseActionAuth[];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ([])
});

const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

// Refs for editing states
const nameInputRef = ref();
const tagsSelectRef = ref();
const priorityRef = ref();
const versionRef = ref();

// Editing states
const isEditName = ref(false);
const editNameLoading = ref(false);
const isEditPriority = ref(false);
const editPriorityLoading = ref(false);
const priority = ref();
const tagsIds = ref<number[]>([]);
const defaultTags = ref<{[key: number]: { name: string; id: number }}>({});
const isEditTag = ref(false);
const editTagLoading = ref(false);
const isVersionEditMode = ref(false);
const selectedVersionValue = ref();
const infoExpand = ref(true);

// Computed properties - removed infoColumns as we now use manual layout

// Name editing functions
const openEditName = () => {
  isEditName.value = true;
  nextTick(() => {
    nameInputRef.value.focus();
  });
};

const editName = async (event) => {
  if (event.target.value === props.dataSource?.name || !event.target.value || !props.dataSource) {
    isEditName.value = false;
    return;
  }

  editNameLoading.value = true;
  const [error] = await testCase.putName(props.dataSource.id, event.target.value);
  editNameLoading.value = false;
  isEditName.value = false;
  if (error) {
    return;
  }
  emit('change', { name: event.target.value });
};

// Priority editing functions
const openEditPriority = () => {
  isEditPriority.value = true;
  priority.value = props.dataSource?.priority?.value;
  nextTick(() => {
    setTimeout(() => {
      if (typeof priorityRef.value?.focus === 'function') {
        priorityRef.value?.focus();
      }
    }, 100);
  });
};

const editPriority = async (value: Priority) => {
  if (editPriorityLoading.value || value === props.dataSource?.priority?.value || !props.dataSource) {
    isEditPriority.value = false;
    return;
  }

  editPriorityLoading.value = true;
  const [error] = await testCase.putPriority(props.dataSource.id, value);
  editPriorityLoading.value = false;
  isEditPriority.value = false;
  if (error) {
    return;
  }
  emit('change', { priority: { value, message: '' } });
};

// Tag editing functions
const openEditTag = () => {
  tagsIds.value = (props.dataSource?.tags || [])?.map(item => {
    defaultTags.value[item.id] = { name: item.name, id: item.id };
    return item.id;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    setTimeout(() => {
      if (typeof tagsSelectRef.value?.focus === 'function') {
        tagsSelectRef.value?.focus();
      }
    }, 100);
  });
};

const editTag = async () => {
  if (!props.dataSource) {
    isEditTag.value = false;
    return;
  }

  if (editTagLoading.value || tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }
  const oldTagIds = props.dataSource?.tags?.map(item => item.id) || [];
  const isEqual = utils.deepCompare(oldTagIds, tagsIds.value);
  if (isEqual) {
    isEditTag.value = false;
    return;
  }
  editTagLoading.value = true;
  const [error] = await testCase.putTag(props.dataSource.id, { tagIds: tagsIds.value.length ? tagsIds.value : null });
  editTagLoading.value = false;
  isEditTag.value = false;
  if (error) {
    return;
  }
  // Convert tag IDs back to tag objects for the change event
  const updatedTags = tagsIds.value.map(id => {
    const tagInfo = defaultTags.value[id];
    return tagInfo ? { id: id, name: tagInfo.name } : null;
  }).filter((tag): tag is { id: number; name: string } => tag !== null);
  emit('change', { tags: updatedTags });
};

// Version editing functions
const openVersionEditMode = () => {
  isVersionEditMode.value = true;
  selectedVersionValue.value = props.dataSource?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionRef.value?.focus === 'function') {
        versionRef.value?.focus();
      }
    }, 100);
  });
};

const handleVersionChange = (newVersionValue) => {
  selectedVersionValue.value = newVersionValue;
};

const handleVersionBlur = async () => {
  if (!props.dataSource) {
    isVersionEditMode.value = false;
    return;
  }

  const versionValue = selectedVersionValue.value;
  if (versionValue === props.dataSource?.softwareVersion) {
    isVersionEditMode.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await testCase.updateCase([{ softwareVersion: versionValue || '', id: props.dataSource.id }]);
  emit('loadingChange', false);
  isVersionEditMode.value = false;
  if (error) {
    return;
  }
  emit('change', { softwareVersion: versionValue });
};
</script>
<template>
  <Toggle
    v-model:open="infoExpand"
    :title="t('common.basicInfo')">
    <template #default>
      <div class="basic-info-container">
        <!-- Primary Information Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.code') }}</span>
            </div>
            <div class="info-value">
              <div class="flex items-center">
                <span class="info-text">{{ dataSource?.code }}</span>
                <div
                  v-if="dataSource?.overdue"
                  class="overdue-badge">
                  {{ t('status.overdue') }}
                </div>
              </div>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.testResult') }}</span>
            </div>
            <div class="info-value">
              <TestResult :value="dataSource?.testResult" />
            </div>
          </div>
        </div>

        <!-- Name and Review Status Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.name') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isEditName" class="info-value-content">
                <span class="info-text">{{ dataSource?.name }}</span>
                <Button
                  v-if="props.actionAuth.includes('edit')"
                  type="link"
                  class="edit-btn"
                  @click="openEditName">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>

              <Input
                v-show="isEditName"
                ref="nameInputRef"
                :value="dataSource?.name"
                :allowClear="false"
                :maxlength="200"
                class="edit-input"
                :placeholder="t('common.name')"
                @blur="editName" />
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.reviewStatus') }}</span>
            </div>
            <div class="info-value">
              <template v-if="dataSource?.reviewStatus">
                <ReviewStatus :value="dataSource.reviewStatus" />
              </template>
              <template v-else>
                <span class="info-text">--</span>
              </template>
            </div>
          </div>
        </div>

        <!-- Version and Software Version Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.version') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">--</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.softwareVersion') }}</span>
            </div>
            <div class="info-value">
              <template v-if="isVersionEditMode">
                <Select
                  ref="versionRef"
                  v-model:value="selectedVersionValue"
                  allowClear
                  :placeholder="t('common.placeholders.selectSoftwareVersion')"
                  lazy
                  class="edit-select"
                  :action="`${TESTER}/software/version?projectId=${projectId}`"
                  :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: SearchCriteria.OpEnum.In}]}"
                  :fieldNames="{value:'name', label: 'name'}"
                  @blur="handleVersionBlur"
                  @change="handleVersionChange">
                </Select>
              </template>
              <template v-else>
                <div class="info-value-content">
                  <RouterLink
                    v-if="dataSource?.softwareVersion"
                    class="info-link"
                    :to="`/task#version?name=${dataSource?.softwareVersion}`">
                    {{ dataSource?.softwareVersion }}
                  </RouterLink>
                  <span v-else class="info-text">--</span>
                  <Button
                    v-if="props.actionAuth.includes('edit')"
                    type="link"
                    class="edit-btn"
                    @click="openVersionEditMode">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </Button>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Plan and Module Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.plan') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ dataSource?.planName || '--' }}</span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.module') }}</span>
            </div>
            <div class="info-value">
              <template v-if="!dataSource?.moduleName">
                <span class="info-text">--</span>
              </template>
              <div v-else class="module-tag">
                <Icon icon="icon-mokuai" class="mr-1.25 flex-none mt-0.5" />
                <div class="flex-1 whitespace-break-spaces break-all leading-4">{{ dataSource.moduleName }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Priority and Unplanned Row -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.priority') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isEditPriority" class="info-value-content">
                <TaskPriority :value="dataSource?.priority as any" />
                <Button
                  v-if="props.actionAuth.includes('edit')"
                  type="link"
                  class="edit-btn"
                  @click="openEditPriority">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>

              <SelectEnum
                v-show="isEditPriority"
                ref="priorityRef"
                v-model:value="priority"
                :allowClear="false"
                :disabled="editPriorityLoading"
                :autofocus="isEditPriority"
                enumKey="Priority"
                class="edit-select"
                :placeholder="t('common.priority')"
                @blur="editPriority(priority as Priority)">
                <template #option="item">
                  <TaskPriority :value="item as any" />
                </template>
              </SelectEnum>
            </div>
          </div>

          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.unplanned') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ dataSource?.unplanned ? t('status.yes') : t('status.no') }}</span>
            </div>
          </div>
        </div>

        <!-- Tags Row -->
        <div class="info-row">
          <div class="info-item info-item-full">
            <div class="info-label">
              <span>{{ t('common.tag') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isEditTag" class="info-value-content">
                <div v-if="dataSource?.tags?.length" class="tags-container">
                  <div
                    v-for="tag in dataSource.tags"
                    :key="tag.id"
                    class="tag-item">
                    {{ tag.name }}
                  </div>
                </div>
                <span v-else class="info-text">--</span>
                <Button
                  v-if="props.actionAuth.includes('edit')"
                  type="link"
                  class="edit-btn"
                  @click="openEditTag">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>

              <Select
                v-show="isEditTag"
                ref="tagsSelectRef"
                v-model:value="tagsIds as any"
                showSearch
                :defaultOptions="defaultTags as any"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTags="5"
                :placeholder="t('common.placeholders.selectTag')"
                :class="{'border-error':tagsIds && tagsIds.length > 5 }"
                :action="`${TESTER}/tag?projectId=${projectId}&fullTextSearch=true`"
                mode="multiple"
                internal
                class="edit-select"
                :notFoundContent="t('backlog.edit.messages.contactAdminForTags')"
                @blur="editTag" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>
<style scoped>
:deep(.toggle-title) {
  font-size: 0.875rem;
}

/* Main Container */
.basic-info-container {
  padding: 1rem 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* Info Row Layout */
.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
  align-items: start;
}

.info-item-full {
  grid-column: 1 / -1;
}

/* Individual Info Item */
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.125rem;
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

.info-link {
  font-size: 0.75rem;
  font-weight: 400;
  color: #3b82f6;
  text-decoration: none;
  line-height: 1.4;
  word-break: break-word;
  white-space: normal;
  transition: color 0.2s ease;
}

.info-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* Value Content Container */
.info-value-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
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

/* Edit Input and Select */
.edit-input,
.edit-select {
  width: 100%;
  max-width: 20rem;
  font-size: 0.75rem;
}

/* Tags Container */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.tag-item {
  padding: 0.25rem 0.5rem;
  height: 1.375rem;
  line-height: 1.375rem;
  border-radius: 0.25rem;
  border: 1px solid #dbeafe;
  background-color: #eff6ff;
  color: #6d7ebc;
  font-size: 0.75rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Module Tag */
.module-tag {
  display: flex;
  align-items: center;
  padding: 0.25rem 0.5rem;
  height: 1.375rem;
  border-radius: 0.25rem;
  border: 1px solid #dbeafe;
  background-color: #eff6ff;
  color: #6d7ebc;
  font-size: 0.75rem;
  font-weight: 500;
}

/* Overdue Badge */
.overdue-badge {
  flex-shrink: 0;
  border: 1px solid #ef4444;
  border-radius: 0.25rem;
  padding: 0.125rem 0.25rem;
  margin-left: 0.5rem;
  color: #ef4444;
  font-size: 0.75rem;
  line-height: 1;
  font-weight: 500;
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

.border-none {
  border: none;
}
</style>

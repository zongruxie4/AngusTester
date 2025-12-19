<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, SelectUser, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { TESTER, appContext } from '@xcan-angus/infra';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';

interface Props {
  id?: string;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([])
});

const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

// Personnel editing state variables
const testerSelectRef = ref();
const isTesterEditing = ref(false);
const testerSelectMessage = ref<string>();
const testerSelectValue = ref<string>();

const developerSelectRef = ref();
const isDeveloperEditing = ref(false);
const developerSelectMessage = ref<string>();
const developerSelectValue = ref<string>();

// Computed properties for case personnel data
const currentCaseId = computed(() => props.dataSource?.id);
const currentcreator = computed(() => props.dataSource?.creator);
const currentmodifier = computed(() => props.dataSource?.modifier);

/**
 * <p>Gets the current tester ID from the case data source.</p>
 * @returns The tester ID or undefined if not available
 */
const currentTesterId = computed(() => props.dataSource?.testerId);

/**
 * <p>Gets the current tester name from the case data source.</p>
 * @returns The tester name or undefined if not available
 */
const currentTesterName = computed(() => props.dataSource?.testerName);

/**
 * <p>Gets the current developer ID from the case data source.</p>
 * @returns The developer ID or undefined if not available
 */
const currentDeveloperId = computed(() => props.dataSource?.developerId);

/**
 * <p>Gets the current developer name from the case data source.</p>
 * @returns The developer name or undefined if not available
 */
const currentDeveloperName = computed(() => props.dataSource?.developerName);

/**
 * <p>Gets the current reviewer name from the case data source.</p>
 * @returns The reviewer name or undefined if not available
 */
const currentReviewerName = computed(() => props.dataSource?.reviewerName);

/**
 * <p>Gets the current user ID from the app context.</p>
 * @returns The current user ID or undefined if not available
 */
const currentUserId = computed(() => {
  return appContext.getUser()?.id;
});

/**
 * <p>Gets the current user name from the app context.</p>
 * @returns The current user full name or undefined if not available
 */
const currentUserName = computed(() => {
  return appContext.getUser()?.fullName;
});

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
 * <p>Creates default options for the developer select component.</p>
 * <p>Returns an object with the current developer's ID and full name for display purposes.</p>
 * @returns Object containing developer data or undefined if no developer is set
 */
const developerDefaultOptions = computed(() => {
  const id = currentDeveloperId.value;
  if (!id) {
    return undefined;
  }

  return {
    [id]: {
      id: id,
      fullName: currentDeveloperName.value
    }
  };
});

/**
 * <p>Initiates tester editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startTesterEditing = () => {
  testerSelectValue.value = currentTesterId.value;
  isTesterEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof testerSelectRef.value?.focus === 'function') {
        testerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Initiates developer editing mode by setting the select value and enabling edit flag.</p>
 * <p>Focuses the select field after a short delay to ensure proper rendering.</p>
 */
const startDeveloperEditing = () => {
  developerSelectValue.value = currentDeveloperId.value;
  isDeveloperEditing.value = true;

  nextTick(() => {
    setTimeout(() => {
      if (typeof developerSelectRef.value?.focus === 'function') {
        developerSelectRef.value?.focus();
      }
    }, 100);
  });
};

/**
 * <p>Assigns the current user to the specified role (tester or developer).</p>
 * <p>Sets the user ID and name, then triggers the blur event to save changes.</p>
 * @param key - The role key to assign the current user to
 */
const assignCurrentUserToRole = (key: 'testerId' | 'developerId') => {
  if (key === 'testerId') {
    isTesterEditing.value = true;
    testerSelectValue.value = currentUserId.value;
    testerSelectMessage.value = currentUserName.value;
    handleTesterBlur();
    return;
  }

  if (key === 'developerId') {
    isDeveloperEditing.value = true;
    developerSelectValue.value = currentUserId.value;
    developerSelectMessage.value = currentUserName.value;
    handleDeveloperBlur();
  }
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
 * <p>Validates selected value and calls API to update case tester if value has changed.</p>
 */
const handleTesterBlur = async () => {
  const newValue = testerSelectValue.value;
  if (newValue === currentTesterId.value) {
    isTesterEditing.value = false;
    return;
  }

  if (!currentCaseId.value) {
    isTesterEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await testCase.updateCase([{
    id: currentCaseId.value,
    testerId: newValue
  }]);
  emit('loadingChange', false);
  if (error) {
    if (typeof testerSelectRef.value?.focus === 'function') {
      testerSelectRef.value?.focus();
    }
    return;
  }

  isTesterEditing.value = false;
  emit('change', { id: currentCaseId.value, testerId: newValue, testerName: testerSelectMessage.value! });
};

/**
 * <p>Handles developer selection change to update the message display.</p>
 * @param value - Selected developer ID
 * @param option - Selected developer option containing id and fullName
 */
const handleDeveloperChange = async (_value: any, option: any) => {
  developerSelectMessage.value = option.fullName;
};

/**
 * <p>Handles developer select blur event to save changes or cancel editing.</p>
 * <p>Validates selected value and calls API to update case developer if value has changed.</p>
 */
const handleDeveloperBlur = async () => {
  const newValue = developerSelectValue.value;
  if (newValue === currentDeveloperId.value) {
    isDeveloperEditing.value = false;
    return;
  }

  if (!currentCaseId.value) {
    isDeveloperEditing.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await testCase.updateCase([{
    id: currentCaseId.value,
    developerId: newValue
  }]);
  emit('loadingChange', false);
  if (error) {
    if (typeof developerSelectRef.value?.focus === 'function') {
      developerSelectRef.value?.focus();
    }
    return;
  }

  isDeveloperEditing.value = false;
  emit('change', { id: currentCaseId.value, developerId: newValue, developerName: developerSelectMessage.value! });
};

</script>
<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('common.personnel') }}</div>
    </template>

    <template #default>
      <div class="personnel-info-container">
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
                  v-if="props.actionAuth.includes('edit')"
                  type="link"
                  class="edit-btn"
                  @click="startTesterEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
                <Button
                  v-if="props.actionAuth.includes('edit') && (!currentTesterId || currentTesterId !== currentUserId)"
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
                  v-model:value="testerSelectValue as any"
                  :placeholder="t('common.placeholders.selectTester')"
                  allowClear
                  internal
                  :defaultOptions="testerDefaultOptions"
                  :action="`${TESTER}/project/${String(props.projectId || '')}/member/user`"
                  :maxlength="80"
                  class="edit-select"
                  @change="handleTesterChange"
                  @blur="handleTesterBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Developer -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.developer') }}</span>
            </div>
            <div class="info-value">
              <div v-show="!isDeveloperEditing" class="info-value-content">
                <span :class="{ 'placeholder-text': !currentDeveloperName }" class="info-text">
                  {{ currentDeveloperName || '--' }}
                </span>
                <Button
                  v-if="props.actionAuth.includes('edit')"
                  type="link"
                  class="edit-btn"
                  @click="startDeveloperEditing">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
                <Button
                  v-if="props.actionAuth.includes('edit') && (!currentDeveloperId || currentDeveloperId !== currentUserId)"
                  size="small"
                  type="link"
                  class="assign-to-me-btn"
                  @click="assignCurrentUserToRole('developerId')">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>

              <AsyncComponent :visible="isDeveloperEditing">
                <SelectUser
                  v-show="isDeveloperEditing"
                  ref="developerSelectRef"
                  v-model:value="developerSelectValue as any"
                  :placeholder="t('common.placeholders.selectDeveloper')"
                  allowClear
                  internal
                  :defaultOptions="developerDefaultOptions"
                  :action="`${TESTER}/project/${String(props.projectId || '')}/member/user`"
                  :maxlength="80"
                  class="edit-select"
                  @change="handleDeveloperChange"
                  @blur="handleDeveloperBlur" />
              </AsyncComponent>
            </div>
          </div>
        </div>

        <!-- Reviewer -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.reviewer') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !currentReviewerName }" class="info-text">
                {{ currentReviewerName || '--' }}
              </span>
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
              <span class="info-text">{{ currentcreator }}</span>
            </div>
          </div>
        </div>

        <!-- Modifier -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.modifier') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ currentmodifier }}</span>
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

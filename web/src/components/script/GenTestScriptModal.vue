<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, notification, Toggle } from '@xcan-angus/vue-ui';
import { apis, indicator, services } from '@/api/tester';
import { CheckboxGroup } from 'ant-design-vue';
import { CombinedTargetType, Priority } from '@xcan-angus/infra';
import { TestType } from '@/enums/enums';

import TestForm from './GenTestScriptForm.vue';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  id: string;
  visible: boolean;
  type: CombinedTargetType.API | CombinedTargetType.SERVICE ;
  setType: 'create'|'update';
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: CombinedTargetType.SERVICE,
  setType: 'create'
});

const emits = defineEmits<{(e: 'update:visible', value: boolean): void}>();

// ===== Reactive Data and State =====
const perfRef = ref();
const stabilityRef = ref();
const funcRef = ref();
const checked = ref<string[]>(['perf', 'stability', 'func']);
const submitting = ref(false);

// Test type options for checkbox group
const testTypeOpt = [
  { value: 'func', label: t('commonComp.apis.genTestScriptModal.functionalTest') },
  { value: 'perf', label: t('commonComp.apis.genTestScriptModal.performanceTest') },
  { value: 'stability', label: t('commonComp.apis.genTestScriptModal.stabilityTest') }
];

// Test data for different test types
const perfTestData = reactive({
  priority: Priority.MEDIUM,
  threads: '',
  duration: '0s',
  auth: false,
  rampUpInterval: undefined,
  rampUpThreads: undefined
});

const funcTestData = reactive({
  priority: Priority.HIGH,
  iterations: '1',
  auth: false,
  ignoreAssertions: false,
  threads: '1',
  duration: '1s'
});

const stabilityTestData = reactive({
  priority: Priority.LOW,
  threads: '',
  duration: '0s',
  auth: false
});

// Toggle state for form sections
const perfOpen = ref(true);
const stabilityOpen = ref(true);
const funcOpen = ref(true);

// ===== Computed Properties =====
/**
 * <p>
 * Computed property for the modal title.
 * <p>
 * Returns the translated title for the generate test script modal.
 * @returns The modal title string
 */
const getTitle = computed(() => {
  return t('commonComp.apis.genTestScriptModal.title');
});

// ===== API Methods =====
/**
 * <p>
 * Loads default test indicators from the API.
 * <p>
 * Fetches performance and stability test indicators and updates the local state.
 */
const loadTestIndicator = async () => {
  if (!props.id) {
    return;
  }
  const [perfErr, perfData] = await indicator.getDefaultPerformance(props.id, props.type);
  if (perfErr) {
    notification.error(perfErr.message);
  } else {
    perfTestData.duration = perfData.data.duration;
    perfTestData.threads = perfData.data.threads;
    perfTestData.rampUpThreads = perfData.data.rampUpThreads;
    perfTestData.rampUpInterval = perfData.data.rampUpInterval;
  }
  const [stabilityErr, stabilityData] = await indicator.getDefaultStability(props.id, props.type);
  if (stabilityErr) {
    notification.error(stabilityErr.message);
  } else {
    stabilityTestData.duration = stabilityData.data.duration;
    stabilityTestData.threads = stabilityData.data.threads;
  }
};

// ===== Event Handlers =====
/**
 * <p>
 * Handles the form submission.
 * <p>
 * Collects data from all test forms, calls the appropriate API, and shows success notification.
 * Closes the modal on successful submission.
 */
const submit = async () => {
  if (submitting.value) {
    return;
  }
  submitting.value = true;
  const perfData = perfRef.value?.getData();
  const stabilityData = stabilityRef.value?.getData();
  const funcData = funcRef.value?.getData();
  if (!perfData && !stabilityData && !funcData) {
    cancel();
    return;
  }
  const params = [perfData, stabilityData, funcData].filter(Boolean);
  if (props.setType === 'create') {
    const [error] = await (props.type === 'API' ? apis.generateTestScript(props.id, params) : services.putApiScript(props.id, params));
    submitting.value = false;
    if (error) {
      return;
    }
    notification.success(t('commonComp.apis.genTestScriptModal.generateSuccess'));
    cancel();
  } else {
    const [error] = await (props.type === 'API' ? apis.updateTestScript(props.id, params) : services.updateApiScript(props.id, params));
    submitting.value = false;
    if (error) {
      return;
    }
    notification.success(t('actions.tips.updateSuccess'));
    cancel();
  }
};

/**
 * <p>
 * Handles the modal close action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const cancel = () => {
  emits('update:visible', false);
};

// ===== Lifecycle Hooks =====
watch(() => props.visible, newValue => {
  if (newValue) {
    loadTestIndicator();
    checked.value = ['perf', 'stability', 'func'];
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    class="gen-test-script-modal"
    :visible="props.visible"
    :title="getTitle"
    :confirmLoading="submitting"
    :width="1200"
    @ok="submit"
    @cancel="cancel">
    <!-- Main content area -->
    <div class="modal-content">
      <!-- Notice section -->
      <div class="notice-section">
        <div class="notice-content">
          <Icon icon="icon-tishi1" class="notice-icon" />
          <div class="notice-text">
            <span class="notice-main">{{ t('commonComp.apis.genTestScriptModal.note') }}</span>
            <span class="notice-highlight">{{ t('commonComp.apis.genTestScriptModal.noteHighlight') }}</span>
          </div>
        </div>
      </div>

      <!-- Test type selection section -->
      <div class="selection-section">
        <h3 class="section-title">{{ t('commonComp.apis.genTestScriptModal.title') }}</h3>
        <div class="checkbox-container">
          <CheckboxGroup
            v-model:value="checked"
            :options="testTypeOpt"
            class="test-type-checkboxes" />
        </div>
      </div>

      <!-- Test configuration forms -->
      <div class="forms-container">
        <div class="form-grid">
          <Toggle
            v-if="checked.includes('func')"
            key="func"
            v-model:open="funcOpen"
            class="test-form-toggle"
            :title="t('commonComp.apis.genTestScriptModal.functionalTestConfig')">
            <TestForm
              key="func"
              ref="funcRef"
              class="test-form-content"
              :setType="props.setType"
              :value="funcTestData"
              :priority="funcTestData.priority"
              :type="props.type"
              :testType="TestType.FUNCTIONAL" />
          </Toggle>

          <Toggle
            v-if="checked.includes('perf')"
            key="perf"
            v-model:open="perfOpen"
            class="test-form-toggle"
            :title="t('commonComp.apis.genTestScriptModal.performanceTestConfig')">
            <TestForm
              key="perf"
              ref="perfRef"
              class="test-form-content"
              :setType="props.setType"
              :value="perfTestData"
              :priority="perfTestData.priority"
              :type="props.type"
              :testType="TestType.PERFORMANCE" />
          </Toggle>

          <Toggle
            v-if="checked.includes('stability')"
            key="stability"
            v-model:open="stabilityOpen"
            class="test-form-toggle"
            :title="t('commonComp.apis.genTestScriptModal.stabilityTestConfig')">
            <TestForm
              key="stability"
              ref="stabilityRef"
              class="test-form-content"
              :setType="props.setType"
              :value="stabilityTestData"
              :priority="stabilityTestData.priority"
              :type="props.type"
              :testType="TestType.STABILITY" />
          </Toggle>
        </div>
      </div>
    </div>
  </Modal>
</template>
<style scoped>
/* Modal content area */
.modal-content {
  padding: 16px 0;
}

/* Notice section */
.notice-section {
  margin-bottom: 24px;
}

.notice-content {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 6px;
}

.notice-icon {
  color: #fa8c16;
  font-size: 16px;
  margin-right: 8px;
  margin-top: 2px;
  flex-shrink: 0;
}

.notice-text {
  flex: 1;
  font-size: 12px;
  color: #d46b08;
  line-height: 1.5;
}

.notice-main {
  color: #d46b08;
}

.notice-highlight {
  color: #fa8c16;
  font-weight: 600;
}

/* Selection section */
.selection-section {
  margin-bottom: 24px;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.checkbox-container {
  margin-top: 8px;
}

.test-type-checkboxes {
  display: flex;
  gap: 16px;
}

/* Forms container */
.forms-container {
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 16px;
}

.test-form-toggle {
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.test-form-content {
  padding: 16px;
  background-color: #fff;
}

/* Deep style overrides */
:deep(.toggle-title) {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
}

:deep(.ant-checkbox-wrapper) {
  font-size: 12px;
  color: #262626;
  margin-right: 0;
}

:deep(.ant-checkbox) {
  margin-right: 8px;
}

:deep(.ant-checkbox-inner) {
  width: 16px;
  height: 16px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-modal-header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.ant-modal-title) {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

:deep(.ant-modal-body) {
  padding: 24px;
  max-height: 80vh;
  overflow-y: auto;
}

:deep(.ant-modal-footer) {
  border-top: 1px solid #f0f0f0;
  padding: 12px 24px;
}

/* Responsive design */
@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .notice-content {
    padding: 8px 12px;
  }

  .selection-section {
    padding: 12px;
  }

  .test-form-content {
    padding: 12px;
  }

  .test-type-checkboxes {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

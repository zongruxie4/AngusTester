<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { func } from '@/api/tester';

import RichEditor from '@/components/richEditor/index.vue';
import { Switch } from 'ant-design-vue';
import _ from 'lodash-es';

const { t } = useI18n();

// Async components for case comparison
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocCase.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/detail/case/Attachment.vue'));

// Props and Emits
interface Props {
  visible: boolean;
  caseId: string;
  baselineId: string;
  compareBaselineId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined,
  compareBaselineId: undefined,
  caseId: undefined,
  projectId: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

// Configuration
const toolbarOptions = ['color', 'weight'];

// Reactive Data
const originalCaseInfo = ref();
const baseCaseData = ref();
const compareCaseData = ref();
const selectedBaseVersion = ref();
const selectedCompareVersion = ref();
const versionOptions = ref<{value: string; label: string}[]>([]);
const shouldHideSameItems = ref(false);

/**
 * Reset all comparison data to initial state
 */
const resetComparisonData = () => {
  baseCaseData.value = undefined;
  compareCaseData.value = undefined;
  selectedBaseVersion.value = undefined;
  selectedCompareVersion.value = undefined;
  versionOptions.value = [];
};

/**
 * Load base case details from baseline
 */
const loadBaseCaseDetails = async () => {
  const [error, { data }] = await func.getBaselineCaseDetail(props.baselineId, props.caseId);
  if (error) {
    return;
  }
  originalCaseInfo.value = data;
  baseCaseData.value = data;
  if (!versionOptions.value.length) {
    versionOptions.value = Object.keys(data.allVersionCaseVos).map(key => {
      return {
        value: key,
        label: 'v' + key
      };
    });
  }
};

/**
 * Load compare case details from baseline
 */
const loadCompareCaseDetails = async () => {
  const [error, { data }] = await func.getBaselineCaseDetail(props.compareBaselineId, props.caseId);
  if (error) {
    return;
  }
  compareCaseData.value = data;
};

/**
 * Handle base version selection change
 * @param value - Selected version value
 */
const handleBaseVersionSelection = (value) => {
  baseCaseData.value = (originalCaseInfo.value.allVersionCaseVos || {})[value];
};

/**
 * Handle compare version selection change
 * @param value - Selected version value
 */
const handleCompareVersionSelection = (value) => {
  compareCaseData.value = (originalCaseInfo.value.allVersionCaseVos || {})[value];
};

/**
 * Close the comparison modal
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * Get CSS class for case name comparison highlighting
 */
const caseNameHighlightClass = computed(() => {
  if (baseCaseData.value && compareCaseData.value) {
    if (baseCaseData.value.name !== compareCaseData.value.name) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

/**
 * Get CSS class for case description comparison highlighting
 */
const caseDescriptionHighlightClass = computed(() => {
  if (baseCaseData.value && compareCaseData.value) {
    if (!!baseCaseData.value?.description && !compareCaseData.value?.description) {
      return 'bg-status-del';
    }
    if (!baseCaseData.value?.description && !!compareCaseData.value?.description) {
      return 'bg-status-add';
    }
    if (baseCaseData.value?.description !== compareCaseData.value?.description) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

/**
 * Get CSS class for case precondition comparison highlighting
 */
const casePreconditionHighlightClass = computed(() => {
  if (baseCaseData.value && compareCaseData.value) {
    if (!!baseCaseData.value?.precondition && !compareCaseData.value?.precondition) {
      return 'bg-status-del';
    }
    if (!baseCaseData.value?.precondition && !!compareCaseData.value?.precondition) {
      return 'bg-status-add';
    }
    if (baseCaseData.value?.precondition !== compareCaseData.value?.precondition) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

/**
 * Get CSS class for test steps comparison highlighting
 */
const testStepsHighlightClass = computed(() => {
  if (!baseCaseData.value?.steps?.length && compareCaseData.value?.steps?.length) {
    return 'bg-status-add';
  } else if (baseCaseData.value?.steps?.length && !compareCaseData.value?.steps?.length) {
    return 'bg-status-del';
  } else if (!!baseCaseData.value?.steps?.length && (baseCaseData.value?.steps?.length !== compareCaseData.value?.steps?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCaseData.value?.steps?.length && (baseCaseData.value?.steps?.length === compareCaseData.value?.steps?.length)) {
    const hasModifications = baseCaseData.value.steps.some((item, idx) => {
      return (item.expectedResult !== compareCaseData.value.steps[idx].expectedResult) || (item.step !== compareCaseData.value.steps[idx].step);
    });
    if (hasModifications) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

/**
 * Get CSS class for associated tasks comparison highlighting
 */
const associatedTasksHighlightClass = computed(() => {
  if (!baseCaseData.value?.refTaskInfos?.length &&
   compareCaseData.value?.refTaskInfos?.length) {
    return 'bg-status-add';
  } else if (baseCaseData.value?.refTaskInfos?.length &&
  !compareCaseData.value?.refTaskInfos?.length) {
    return 'bg-status-del';
  } else if (!!baseCaseData.value?.refTaskInfos?.length &&
  (baseCaseData.value?.refTaskInfos?.length !== compareCaseData.value?.refTaskInfos?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCaseData.value?.refTaskInfos?.length &&
  (baseCaseData.value?.refTaskInfos?.length === compareCaseData.value?.refTaskInfos?.length)) {
    const differentTasks = _.differenceBy(baseCaseData.value.refTaskInfos?.length, compareCaseData.value.refTaskInfos, 'id');
    if (differentTasks?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

/**
 * Get CSS class for associated cases comparison highlighting
 */
const associatedCasesHighlightClass = computed(() => {
  if (!baseCaseData.value?.refCaseInfos?.length && compareCaseData.value?.refCaseInfos?.length) {
    return 'bg-status-add';
  } else if (baseCaseData.value?.refCaseInfos?.length && !compareCaseData.value?.refCaseInfos?.length) {
    return 'bg-status-del';
  } else if (!!baseCaseData.value?.refCaseInfos?.length && (baseCaseData.value?.refCaseInfos?.length !== compareCaseData.value.refCaseInfos?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCaseData.value?.refCaseInfos?.length && (baseCaseData.value?.refCaseInfos?.length === compareCaseData.value.refCaseInfos?.length)) {
    const differentCases = _.differenceBy(baseCaseData.value.refCaseInfos?.length, compareCaseData.value.refCaseInfos, 'id');
    if (differentCases?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

/**
 * Get CSS class for attachments comparison highlighting
 */
const attachmentsHighlightClass = computed(() => {
  if (!baseCaseData.value?.attachmentsData?.length &&
  compareCaseData.value?.attachmentsData?.length) {
    return 'bg-status-add';
  } else if (baseCaseData.value?.attachmentsData?.length &&
  !compareCaseData.value?.attachmentsData?.length) {
    return 'bg-status-del';
  } else if (!!baseCaseData.value?.attachmentsData?.length &&
  (baseCaseData.value?.attachmentsData?.length !== compareCaseData.value.attachmentsData?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCaseData.value?.attachmentsData?.length &&
  (baseCaseData.value?.attachmentsData?.length === compareCaseData.value.attachmentsData?.length)) {
    const differentAttachments = _.differenceBy(baseCaseData.value.attachmentsData?.length, compareCaseData.value.attachmentsData, 'id');
    if (differentAttachments?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

/**
 * Initialize component and watch for visibility changes
 */
onMounted(() => {
  watch(() => props.visible, async isVisible => {
    if (!isVisible) {
      resetComparisonData();
      return;
    }
    if (props.baselineId === props.compareBaselineId) {
      await loadBaseCaseDetails();
      compareCaseData.value = baseCaseData.value;
      selectedBaseVersion.value = baseCaseData.value.version;
      selectedCompareVersion.value = compareCaseData.value.version;
    } else {
      Promise.all([loadBaseCaseDetails(), loadCompareCaseDetails()]).then(() => {
        selectedBaseVersion.value = baseCaseData.value.version;
        selectedCompareVersion.value = compareCaseData.value.version;
      });
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <Modal
    :title="t('functionBaseline.case.compareModalTitle')"
    :visible="props.visible"
    :width="1000"
    :footer="false"
    @cancel="closeModal">
    <div style="height: 80svh; overflow-y: auto;">
      <div class="flex  items-center space-x-3">
        <div class="inline-flex items-center">
          {{ t('functionBaseline.case.hideSameItems') }}
          <Switch
            v-model:checked="shouldHideSameItems"
            size="small"
            class="ml-1" />
        </div>
        <div class="flex-1"></div>
        <div class="inline-flex items-center space-x-1">
          <span>{{ t('status.new') }}</span>
          <span class="w-4 h-4 inline-block bg-status-add"></span>
        </div>

        <div class="inline-flex items-center space-x-1">
          <span>{{ t('status.deleted') }}</span>
          <span class="w-4 h-4 inline-block bg-status-del"></span>
        </div>

        <div class="inline-flex items-center space-x-1">
          <span>{{ t('status.modified') }}</span>
          <span class="w-4 h-4 inline-block bg-blue-active"></span>
        </div>
      </div>
      <div class="flex leading-10 border-b border-t mt-2">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.versionNumber') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <Select
            v-model:value="selectedBaseVersion"
            class="w-20 self-center"
            :options="versionOptions"
            @change="handleBaseVersionSelection" />
        </div>
        <div class="flex-1 border-r px-2  flex justify-between">
          <Select
            v-model:value="selectedCompareVersion"
            class="w-20 self-center"
            :options="versionOptions"
            @change="handleCompareVersionSelection" />
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!caseNameHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.name') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCaseData?.name">
            {{ baseCaseData?.name }}
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="caseNameHighlightClass">
          <div v-if="compareCaseData?.version">
            {{ compareCaseData?.name }}
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!caseDescriptionHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('common.description') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCaseData?.description">
            <RichEditor
              :value="baseCaseData.description"
              mode="view" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="caseDescriptionHighlightClass">
          <div v-if="compareCaseData?.description">
            <RichEditor
              :value="compareCaseData.description"
              mode="view" />
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!casePreconditionHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.precondition') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCaseData?.precondition">
            <RichEditor
              v-model:value="baseCaseData.precondition"
              mode="view"
              :toolbarOptions="toolbarOptions"
              :options="{theme: 'bubble'}"
              class="step-content"
              height="auto" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="casePreconditionHighlightClass">
          <div v-if="compareCaseData?.precondition">
            <RichEditor
              v-model:value="compareCaseData.precondition"
              mode="view"
              :toolbarOptions="toolbarOptions"
              :options="{theme: 'bubble'}"
              class="step-content"
              height="auto" />
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!testStepsHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l flex flex-col justify-center  bg-gray-bg">
          {{ t('functionBaseline.case.testSteps') }}
        </div>
        <div class="flex-1 border-r">
          <div v-if="baseCaseData?.steps?.length" class="-mb-0.25">
            <div class="flex border-b">
              <span class="w-8 border-r"></span>
              <span class="flex-1 px-2 border-r">{{ t('functionBaseline.case.stepDescription') }}</span>
              <span class="flex-1 px-2">{{ t('functionBaseline.case.expectedResult') }}</span>
            </div>
            <div v-for="(step, idx) in baseCaseData.steps" class="flex border-b leading-5">
              <div class="w-8 text-center border-r py-2">{{ idx + 1 }}</div>
              <div class="flex-1 px-2 border-r py-2">
                <RichEditor
                  v-model:value="step.step"
                  mode="view"
                  :toolbarOptions="toolbarOptions"
                  :options="{theme: 'bubble'}"
                  class="step-content"
                  height="auto" />
              </div>
              <div class="flex-1 px-2 py-2">
                <RichEditor
                  v-model:value="step.expectedResult"
                  mode="view"
                  :toolbarOptions="toolbarOptions"
                  :options="{theme: 'bubble'}"
                  class="step-content"
                  height="auto" />
              </div>
            </div>
          </div>
        </div>
        <div class="flex-1 border-r" :class="testStepsHighlightClass">
          <div v-if="compareCaseData?.steps?.length" class="-mb-0.25">
            <div class="flex border-b">
              <span class="w-8 border-r"></span>
              <span class="flex-1 px-2 border-r">{{ t('functionBaseline.case.stepDescription') }}</span>
              <span class="flex-1 px-2">{{ t('functionBaseline.case.expectedResult') }}</span>
            </div>
            <div v-for="(step, idx) in compareCaseData.steps" class="flex border-b leading-5">
              <div class="w-8 text-center border-r py-2">{{ idx + 1 }}</div>
              <div class="flex-1 px-2 border-r py-2">
                <RichEditor
                  v-model:value="step.step"
                  mode="view"
                  :toolbarOptions="toolbarOptions"
                  :options="{theme: 'bubble'}"
                  class="step-content"
                  height="auto" />
              </div>
              <div class="flex-1 px-2 py-2">
                <RichEditor
                  v-model:value="step.expectedResult"
                  mode="view"
                  :toolbarOptions="toolbarOptions"
                  :options="{theme: 'bubble'}"
                  class="step-content"
                  height="auto" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!associatedTasksHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('functionBaseline.case.associatedTasks') }}
        </div>
        <div class="flex-1 border-r px-2 min-w-0">
          <div v-if="baseCaseData?.refTaskInfos" class="py-1">
            <AssocTasks
              hideTitle
              :dataSource="baseCaseData?.refTaskInfos"
              :projectId="props.projectId"
              :caseInfo="baseCaseData" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  min-w-0" :class="associatedTasksHighlightClass">
          <div v-if="compareCaseData?.refTaskInfos" class="py-1">
            <AssocTasks
              hideTitle
              :dataSource="compareCaseData?.refTaskInfos"
              :projectId="props.projectId"
              :caseInfo="compareCaseData" />
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!associatedCasesHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('functionBaseline.case.associatedCases') }}
        </div>
        <div class="flex-1 border-r px-2 min-w-0">
          <div v-if="baseCaseData?.refCaseInfos" class="py-1">
            <AssocCases
              hideTitle
              :dataSource="baseCaseData?.refCaseInfos"
              :projectId="props.projectId"
              :caseInfo="baseCaseData" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  min-w-0" :class="associatedCasesHighlightClass">
          <div v-if="compareCaseData?.refCaseInfos" class="py-1">
            <AssocCases
              hideTitle
              :dataSource="compareCaseData?.refCaseInfos"
              :projectId="props.projectId"
              :caseInfo="compareCaseData" />
          </div>
        </div>
      </div>

      <div v-show="!shouldHideSameItems || !!attachmentsHighlightClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('common.attachments') }}
        </div>
        <div class="flex-1 border-r px-2">
          <div v-if="baseCaseData?.attachment?.length" class="py-1">
            <Attachment
              hideTitle
              :caseInfo="baseCaseData" />
          </div>
        </div>
        <div class="flex-1 border-r px-2 ">
          <div
            v-if="compareCaseData?.attachment?.length"
            class="py-1"
            :class="attachmentsHighlightClass">
            <Attachment
              hideTitle
              :caseInfo="compareCaseData" />
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>

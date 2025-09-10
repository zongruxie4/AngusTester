<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { func } from '@/api/tester';

import RichEditor from '@/components/richEditor/index.vue';
import { Switch } from 'ant-design-vue';
import _ from 'lodash-es';

const { t } = useI18n();

interface Props {
  visible: boolean;
  caseId: string;
  baselineId: string;
  comparelineId: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined,
  comparelineId: undefined,
  caseId: undefined,
  projectId: undefined
});

const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/components/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/components/AssocCase.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/components/Attachment.vue'));
const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();
const toolbarOptions = ['color', 'weight'];

const defaultCaseInfo = ref();

const baseCase = ref();
const compareCase = ref();

const baseVersion = ref();
const compareVersion = ref();

const versionOpt = ref<{value: string; label: string}[]>([]);

const hideSameItem = ref(false);

const resetData = () => {
  baseCase.value = undefined;
  compareCase.value = undefined;
  baseVersion.value = undefined;
  compareVersion.value = undefined;
  versionOpt.value = [];
};

const loadBaseCase = async () => {
  const [error, { data }] = await func.getBaselineCaseDetail(props.baselineId, props.caseId);
  if (error) {
    return;
  }
  defaultCaseInfo.value = data;
  baseCase.value = data;
  if (!versionOpt.value.length) {
    versionOpt.value = Object.keys(data.allVersionCaseVos).map(key => {
      return {
        value: key,
        label: 'v' + key
      };
    });
  }
};

const loadCompareCase = async () => {
  const [error, { data }] = await func.getBaselineCaseDetail(props.comparelineId, props.caseId);
  if (error) {
    return;
  }
  compareCase.value = data;
};

const handleBaseVersionChange = (value) => {
  baseCase.value = (defaultCaseInfo.value.allVersionCaseVos || {})[value];
};

const handleCompareVersionChange = (value) => {
  compareCase.value = (defaultCaseInfo.value.allVersionCaseVos || {})[value];
};

const cancel = () => {
  emits('update:visible', false);
};
const nameClass = computed(() => {
  if (baseCase.value && compareCase.value) {
    if (baseCase.value.name !== compareCase.value.name) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

const descriptionClass = computed(() => {
  if (baseCase.value && compareCase.value) {
    if (!!baseCase.value?.description && !compareCase.value?.description) {
      return 'bg-status-del';
    }
    if (!baseCase.value?.description && !!compareCase.value?.description) {
      return 'bg-status-add';
    }
    if (baseCase.value?.description !== compareCase.value?.description) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

const preconditionClass = computed(() => {
  if (baseCase.value && compareCase.value) {
    if (!!baseCase.value?.precondition && !compareCase.value?.precondition) {
      return 'bg-status-del';
    }
    if (!baseCase.value?.precondition && !!compareCase.value?.precondition) {
      return 'bg-status-add';
    }
    if (baseCase.value?.precondition !== compareCase.value?.precondition) {
      return 'bg-blue-active';
    }
    return '';
  }
  return '';
});

const stepsClass = computed(() => {
  if (!baseCase.value?.steps?.length && compareCase.value?.steps?.length) {
    return 'bg-status-add';
  } else if (baseCase.value?.steps?.length && !compareCase.value?.steps?.length) {
    return 'bg-status-del';
  } else if (!!baseCase.value?.steps?.length && (baseCase.value?.steps?.length !== compareCase.value?.steps?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCase.value?.steps?.length && (baseCase.value?.steps?.length === compareCase.value?.steps?.length)) {
    const isModify = baseCase.value.steps.some((item, idx) => {
      return (item.expectedResult !== compareCase.value.steps[idx].expectedResult) || (item.step !== compareCase.value.steps[idx].step);
    });
    if (isModify) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

const taskClass = computed(() => {
  if (!baseCase.value?.refTaskInfos?.length && compareCase.value?.refTaskInfos?.length) {
    return 'bg-status-add';
  } else if (baseCase.value?.refTaskInfos?.length && !compareCase.value?.refTaskInfos?.length) {
    return 'bg-status-del';
  } else if (!!baseCase.value?.refTaskInfos?.length && (baseCase.value?.refTaskInfos?.length !== compareCase.value?.refTaskInfos?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCase.value?.refTaskInfos?.length && (baseCase.value?.refTaskInfos?.length === compareCase.value?.refTaskInfos?.length)) {
    const different = _.differenceBy(baseCase.value.refTaskInfos?.length, compareCase.value.refTaskInfos, 'id');
    if (different?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

const caseClass = computed(() => {
  if (!baseCase.value?.refCaseInfos?.length && compareCase.value?.refCaseInfos?.length) {
    return 'bg-status-add';
  } else if (baseCase.value?.refCaseInfos?.length && !compareCase.value?.refCaseInfos?.length) {
    return 'bg-status-del';
  } else if (!!baseCase.value?.refCaseInfos?.length && (baseCase.value?.refCaseInfos?.length !== compareCase.value.refCaseInfos?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCase.value?.refCaseInfos?.length && (baseCase.value?.refCaseInfos?.length === compareCase.value.refCaseInfos?.length)) {
    const different = _.differenceBy(baseCase.value.refCaseInfos?.length, compareCase.value.refCaseInfos, 'id');
    if (different?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

const attachmentClass = computed(() => {
  if (!baseCase.value?.attachmentsData?.length && compareCase.value?.attachmentsData?.length) {
    return 'bg-status-add';
  } else if (baseCase.value?.attachmentsData?.length && !compareCase.value?.attachmentsData?.length) {
    return 'bg-status-del';
  } else if (!!baseCase.value?.attachmentsData?.length && (baseCase.value?.attachmentsData?.length !== compareCase.value.attachmentsData?.length)) {
    return 'bg-blue-active';
  } else if (!!baseCase.value?.attachmentsData?.length && (baseCase.value?.attachmentsDataE?.length === compareCase.value.attachmentsData?.length)) {
    const different = _.differenceBy(baseCase.value.attachmentsData?.length, compareCase.value.attachmentsData, 'id');
    if (different?.length) {
      return 'bg-blue-active';
    }
  } else {
    return '';
  }
});

onMounted(() => {
  watch(() => props.visible, async newValue => {
    if (!newValue) {
      resetData();
      return;
    }
    if (props.baselineId === props.comparelineId) {
      await loadBaseCase();
      compareCase.value = baseCase.value;
      baseVersion.value = baseCase.value.version;
      compareVersion.value = compareCase.value.version;
    } else {
      Promise.all([loadBaseCase(), loadCompareCase()]).then(() => {
        baseVersion.value = baseCase.value.version;
        compareVersion.value = compareCase.value.version;
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
    @cancel="cancel">
    <div style="height: 80svh; overflow-y: auto;">
      <div class="flex  items-center space-x-3">
        <div class="inline-flex items-center">
          {{ t('functionBaseline.case.hideSameItems') }}
          <Switch
            v-model:checked="hideSameItem"
            size="small"
            class="ml-1" />
        </div>
        <div class="flex-1"></div>
        <div class="inline-flex items-center space-x-1">
          <span>{{ t('functionBaseline.case.compareTag.new') }}</span>
          <span class="w-4 h-4 inline-block bg-status-add"></span>
        </div>

        <div class="inline-flex items-center space-x-1">
          <span>{{ t('functionBaseline.case.compareTag.deleted') }}</span>
          <span class="w-4 h-4 inline-block bg-status-del"></span>
        </div>

        <div class="inline-flex items-center space-x-1">
          <span>{{ t('functionBaseline.case.compareTag.modified') }}</span>
          <span class="w-4 h-4 inline-block bg-blue-active"></span>
        </div>
      </div>
      <div class="flex leading-10 border-b border-t mt-2">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.versionNumber') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <Select
            v-model:value="baseVersion"
            class="w-20 self-center"
            :options="versionOpt"
            @change="handleBaseVersionChange" />
          <!-- <div v-if="baseCase?.version">
            v{{ baseCase?.version }}
          </div>
          <div>
            {{ compareCase?.lastModifiedDate }}
          </div> -->
        </div>
        <div class="flex-1 border-r px-2  flex justify-between">
          <Select
            v-model:value="compareVersion"
            class="w-20 self-center"
            :options="versionOpt"
            @change="handleCompareVersionChange" />
          <!-- <div v-if="compareCase?.version">
            v{{ compareCase?.version }}
          </div>
          <div>
            {{ compareCase?.lastModifiedDate }}
          </div> -->
        </div>
      </div>

      <div v-show="!hideSameItem || !!nameClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.name') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCase?.name">
            {{ baseCase?.name }}
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="nameClass">
          <div v-if="compareCase?.version">
            {{ compareCase?.name }}
          </div>
        </div>
      </div>

      <div v-show="!hideSameItem || !!descriptionClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.description') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCase?.description">
            <RichEditor
              :value="baseCase.description"
              mode="view" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="descriptionClass">
          <div v-if="compareCase?.description">
            <RichEditor
              :value="compareCase.description"
              mode="view" />
          </div>
        </div>
      </div>

      <div v-show="!hideSameItem || !!preconditionClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l bg-gray-bg">
          {{ t('functionBaseline.case.precondition') }}
        </div>
        <div class="flex-1 border-r px-2 flex justify-between">
          <div v-if="baseCase?.precondition">
            <RichEditor
              v-model:value="baseCase.precondition"
              mode="view"
              :toolbarOptions="toolbarOptions"
              :options="{theme: 'bubble'}"
              class="step-content"
              height="auto" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  flex justify-between" :class="preconditionClass">
          <div v-if="compareCase?.description">
            <RichEditor
              v-model:value="compareCase.precondition"
              mode="view"
              :toolbarOptions="toolbarOptions"
              :options="{theme: 'bubble'}"
              class="step-content"
              height="auto" />
          </div>
        </div>
      </div>

      <div v-show="!hideSameItem || !!stepsClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l flex flex-col justify-center  bg-gray-bg">
          {{ t('functionBaseline.case.testSteps') }}
        </div>
        <div class="flex-1 border-r">
          <div v-if="baseCase?.steps?.length" class="-mb-0.25">
            <div class="flex border-b">
              <span class="w-8 border-r"></span>
              <span class="flex-1 px-2 border-r">{{ t('functionBaseline.case.stepDescription') }}</span>
              <span class="flex-1 px-2">{{ t('functionBaseline.case.expectedResult') }}</span>
            </div>
            <div v-for="(step, idx) in baseCase.steps" class="flex border-b leading-5">
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
        <div class="flex-1 border-r" :class="stepsClass">
          <div v-if="compareCase?.steps?.length" class="-mb-0.25">
            <div class="flex border-b">
              <span class="w-8 border-r"></span>
              <span class="flex-1 px-2 border-r">{{ t('functionBaseline.case.stepDescription') }}</span>
              <span class="flex-1 px-2">{{ t('functionBaseline.case.expectedResult') }}</span>
            </div>
            <div v-for="(step, idx) in compareCase.steps" class="flex border-b leading-5">
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

      <div v-show="!hideSameItem || !!taskClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('functionBaseline.case.associatedTasks') }}
        </div>
        <div class="flex-1 border-r px-2 min-w-0">
          <div v-if="baseCase?.refTaskInfos" class="py-1">
            <AssocTasks
              hideTitle
              :dataSource="baseCase?.refTaskInfos"
              :projectId="props.projectId"
              :caseInfo="baseCase" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  min-w-0" :class="taskClass">
          <div v-if="compareCase?.refTaskInfos" class="py-1">
            <AssocTasks
              hideTitle
              :dataSource="compareCase?.refTaskInfos"
              :projectId="props.projectId"
              :caseInfo="compareCase" />
          </div>
        </div>
      </div>

      <div v-show="!hideSameItem || !!caseClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('functionBaseline.case.associatedCases') }}
        </div>
        <div class="flex-1 border-r px-2 min-w-0">
          <div v-if="baseCase?.refCaseInfos" class="py-1">
            <AssocCases
              hideTitle
              :dataSource="baseCase?.refCaseInfos"
              :projectId="props.projectId"
              :caseInfo="baseCase" />
          </div>
        </div>
        <div class="flex-1 border-r px-2  min-w-0" :class="caseClass">
          <div v-if="compareCase?.refCaseInfos" class="py-1">
            <AssocCases
              hideTitle
              :dataSource="compareCase?.refCaseInfos"
              :projectId="props.projectId"
              :caseInfo="compareCase" />
          </div>
        </div>
      </div>

      <div v-show="!hideSameItem || !!attachmentClass" class="flex leading-10 border-b">
        <div class="w-40 px-2 border-r border-l  bg-gray-bg">
          {{ t('functionBaseline.case.attachments') }}
        </div>
        <div class="flex-1 border-r px-2">
          <div v-if="baseCase?.attachment?.length" class="py-1">
            <Attachment
              hideTitle
              :caseInfo="baseCase" />
          </div>
        </div>
        <div class="flex-1 border-r px-2 ">
          <div
            v-if="compareCase?.attachment?.length"
            class="py-1"
            :class="attachmentClass">
            <Attachment
              hideTitle
              :caseInfo="compareCase" />
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>

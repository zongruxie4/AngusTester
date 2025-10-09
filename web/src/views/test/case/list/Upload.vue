<script lang="ts" setup>
import { Ref, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, UploadDragger } from 'ant-design-vue';
import { TESTER, enumOptionUtils } from '@xcan-angus/infra';
import { StrategyWhenDuplicated } from '@/enums/enums';
import { formatBytes } from '@/utils/common';
import { testCase } from '@/api/tester';

const { t } = useI18n();

// Component props interface
export interface Props{
  visible: boolean;
  downloadTemplate: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// Component emits
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

// Basic state management
const projectId = inject<Ref<string>>('projectId', ref(''));
const loading = ref(false);
const strategyWhenDuplicatedOpt = ref<{value: string; label: string}[]>([]);

// Form state management
const formRef = ref();
const formData = ref<{
  file: File | undefined;
  strategyWhenDuplicated: StrategyWhenDuplicated;
  planId: string | undefined;
}>({
  file: undefined,
  strategyWhenDuplicated: StrategyWhenDuplicated.COVER,
  planId: undefined
});

/**
 * Load strategy when duplicated enum options
 */
const loadEnums = () => {
  strategyWhenDuplicatedOpt.value = enumOptionUtils.loadEnumAsOptions(StrategyWhenDuplicated);
};

/**
 * Handle file selection
 * @param fileInfo - File information
 */
const handleFile = (fileInfo: any) => {
  formData.value.file = fileInfo.file;
  formRef.value.validateFields(['file']);
};

/**
 * Delete selected file
 */
const deleteFile = () => {
  formData.value.file = undefined;
};

/**
 * Validate file field
 * @returns Validation result
 */
const validateFile = async () => {
  if (formData.value.file) {
    return Promise.resolve();
  }
  return Promise.reject();
};

/**
 * Handle download template
 */
const handleDownloadTemplate = () => {
  if (typeof props.downloadTemplate === 'function') {
    props.downloadTemplate();
  }
};

/**
 * Cancel upload
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Confirm upload
 */
const ok = () => {
  formRef.value.validate()
    .then(async () => {
      const formParams = new FormData();
      formParams.append('planId', formData.value.planId || '');
      formParams.append('strategyWhenDuplicated', formData.value.strategyWhenDuplicated);
      if (formData.value.file) {
        formParams.append('file', formData.value.file);
      }
      loading.value = true;
      const [error] = await testCase.importCase(formParams);
      loading.value = false;
      if (error) {
        return;
      }
      emits('ok');
      cancel();
    });
};

// Watchers
watch(() => props.visible, newValue => {
  if (!newValue) {
    formData.value.file = undefined;
    formData.value.strategyWhenDuplicated = StrategyWhenDuplicated.COVER;
  }
  if (!strategyWhenDuplicatedOpt.value.length) {
    loadEnums();
  }
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('actions.upload')"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <Form
      ref="formRef"
      :model="formData"
      size="small">
      <FormItem
        :label="t('common.plan')"
        name="planId"
        required>
        <Select
          v-model:value="formData.planId"
          :disabled="!projectId"
          :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
          :defaultActiveFirstOption="true"
          :lazy="false"
          :placeholder="t('common.placeholders.selectPlan')"
          :fieldNames="{value: 'id', label: 'name'}" />
      </FormItem>

      <FormItem :rules="{message: t('testCase.messages.pleaseUploadFile'), validator: validateFile}" name="file">
        <Spin :spinning="loading">
          <UploadDragger
            v-show="!formData.file"
            accept=".xls,.xlsx"
            :multiple="false"
            :showUploadList="false"
            :customRequest="handleFile"
            class="text-3 leading-5">
            <div class="flex flex-col items-center justify-center text-3 leading-5">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
              <div class="mt-1 mb-1.5 text-text-link">{{ t('testCase.messages.selectFile') }}</div>
              <div class="text-theme-sub-content">{{ t('testCase.messages.uploadDescription') }}</div>
            </div>
          </UploadDragger>
        </Spin>

        <div
          v-show="!!formData.file"
          class="px-3.5 border rounded">
          <div class="flex py-2 text-3 leading-3">
            <div :title="formData.file?.name" class="flex-2 truncate">{{ formData.file?.name }}</div>
            <div class="flex-1 text-right">{{ formatBytes(Number(formData.file?.size)) }}</div>
            <div class="flex-1 text-right text-gray-500">
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer ml-2 text-3.5 text-theme-text-hover"
                @click="deleteFile" />
            </div>
          </div>
        </div>

        <div class="text-right">
          <Button
            type="link"
            size="small"
            @click="handleDownloadTemplate">
            <Icon icon="icon-daochu1" class="text-4 cursor-pointer mr-1" />
            {{ t('testCase.messages.caseImportTemplate') }}
          </Button>
        </div>
      </FormItem>

      <FormItem
        :label="t('testCase.messages.duplicateStrategy')"
        name="strategyWhenDuplicated"
        required>
        <RadioGroup
          v-model:value="formData.strategyWhenDuplicated"
          :options="strategyWhenDuplicatedOpt">
        </RadioGroup>
      </FormItem>
    </Form>
  </Modal>
</template>

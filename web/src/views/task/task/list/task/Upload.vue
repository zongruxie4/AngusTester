<script lang="ts" setup>
import { inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, RadioGroup, UploadDragger } from 'ant-design-vue';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { StrategyWhenDuplicated } from '@/enums/enums';
import { formatBytes } from '@/utils/common';
import { task } from '@/api/tester';

const { t } = useI18n();

// ===== Props and Emits Definition =====
export interface Props{
  visible: boolean;
  downloadTemplate: ()=> void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

// Injected Data
const projectId = inject<Ref<string>>('projectId', ref(''));
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>(
  'proTypeShowMap', ref({
    showTask: true,
    showBackLog: true,
    showMeeting: true,
    showSprint: true,
    showTasStatistics: true
  }
  ));

// Reactive Data
const isLoading = ref(false);
const strategyOptions = ref<{value: string; label: string}[]>([]);
const formRef = ref();

const formData = ref<{
  file: File|undefined;
  strategyWhenDuplicated: StrategyWhenDuplicated;
  sprintId: string|undefined;
}>({
  file: undefined,
  strategyWhenDuplicated: StrategyWhenDuplicated.COVER,
  sprintId: undefined
});

// Lifecycle Hooks
watch(() => props.visible, newValue => {
  if (!newValue) {
    formData.value.file = undefined;
    formData.value.strategyWhenDuplicated = StrategyWhenDuplicated.COVER;
  }

  if (!strategyOptions.value.length) {
    loadStrategyOptions();
  }
}, {
  immediate: true
});

/**
 * Handle download template button click
 */
const handleDownloadTemplate = () => {
  if (typeof props.downloadTemplate === 'function') {
    props.downloadTemplate();
  }
};

/**
 * Handle file selection
 */
const handleFileSelection = (fileInfo: any) => {
  formData.value.file = fileInfo.file;
  formRef.value.validateFields(['file']);
};

/**
 * Remove selected file
 */
const removeSelectedFile = () => {
  formData.value.file = undefined;
};

/**
 * Validate if file is selected
 */
const validateFileSelection = async () => {
  if (formData.value.file) {
    return Promise.resolve();
  }
  return Promise.reject();
};

/**
 * Load strategy options for duplicate handling
 */
const loadStrategyOptions = () => {
  const data = enumUtils.enumToMessages(StrategyWhenDuplicated);
  strategyOptions.value = data.map(i => ({ value: i.value, label: i.message }));
};

/**
 * Close the modal dialog
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * Submit the form to import tasks
 */
const submitImportForm = () => {
  formRef.value.validate()
    .then(async () => {
      const formParams = new FormData();
      formData.value.sprintId && formParams.append('sprintId', formData.value.sprintId);
      formParams.append('projectId', projectId.value);
      formParams.append('strategyWhenDuplicated', formData.value.strategyWhenDuplicated);
      formData.value.file && formParams.append('file', formData.value.file);
      isLoading.value = true;
      const [error] = await task.importTask(formParams);
      isLoading.value = false;
      if (error) {
        return;
      }
      emits('ok');
      closeModal();
    });
};
</script>
<template>
  <Modal
    :title="t('task.upload.title')"
    :visible="props.visible"
    @cancel="closeModal"
    @ok="submitImportForm">
    <Form
      ref="formRef"
      :model="formData"
      size="small">
      <FormItem
        v-if="proTypeShowMap.showSprint && props.visible"
        :label="t('task.upload.form.iteration')"
        name="sprintId">
        <Select
          v-model:value="formData.sprintId"
          allowClear
          :disabled="!projectId"
          :action="`${TESTER}/task/sprint?projectId=${projectId}&fullTextSearch=true`"
          :defaultActiveFirstOption="true"
          :lazy="false"
          :placeholder="t('task.upload.form.selectSprint')"
          :fieldNames="{value: 'id', label: 'name'}" />
      </FormItem>

      <FormItem
        :rules="{message: t('task.upload.form.uploadFile'), validator: validateFileSelection}"
        name="file">
        <Spin :spinning="isLoading">
          <UploadDragger
            v-show="!formData.file"
            accept=".xls,.xlsx"
            :multiple="false"
            :showUploadList="false"
            :customRequest="handleFileSelection"
            class="text-3 leading-5">
            <div class="flex flex-col items-center justify-center text-3 leading-5">
              <Icon icon="icon-shangchuan" class="text-5 text-text-link" />
              <div class="mt-1 mb-1.5 text-text-link">{{ t('task.upload.form.selectFile') }}</div>
              <div class="text-theme-sub-content">{{ t('task.upload.form.uploadTip') }}</div>
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
                @click="removeSelectedFile" />
            </div>
          </div>
        </div>

        <div class="text-right">
          <Button
            type="link"
            size="small"
            @click="handleDownloadTemplate">
            <Icon icon="icon-daochu1" class="text-4 cursor-pointer mr-1" />
            {{ t('task.upload.form.taskImportTemplate') }}
          </Button>
        </div>
      </FormItem>

      <FormItem
        :label="t('task.upload.form.duplicateStrategy')"
        name="strategyWhenDuplicated"
        required>
        <RadioGroup
          v-model:value="formData.strategyWhenDuplicated"
          :options="strategyOptions">
        </RadioGroup>
      </FormItem>
    </Form>
  </Modal>
</template>

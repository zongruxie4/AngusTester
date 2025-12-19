<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, NoData, Spin } from '@xcan-angus/vue-ui';
import { Checkbox, Input } from 'ant-design-vue';
import { services } from '@/api/tester';
import { deconstruct } from '@/utils/swagger';

interface Props {
  visible: boolean;
}

interface ModelOption {
  id: string;
  ref: string;
  key: string;
  model: string;
  modifier: string;
  modifiedDate: string;
  resolvedRefModels?: any;
  label: string;
  value: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const { t } = useI18n();
const isLoading = ref(false);
const searchKeywords = ref<string>();
const apiBaseInfo = inject('apiBaseInfo', ref());

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', value?: Record<string, any>): void;
}>();

/**
 * Close the modal
 */
const closeModal = () => {
  emits('update:visible', false);
};

/**
 * Handle model import
 */
const handleModelImport = async () => {
  if (!selectedModel.value) {
    closeModal();
    return;
  }

  const [error, response] = await services.getComponentRef(apiBaseInfo.value.serviceId, modelReference.value!);
  if (error) {
    return;
  }

  const result = deconstruct(response.data);
  emits('confirm', {
    schema: result || JSON.parse(response.data.model),
    ref: modelReference.value
  });
  closeModal();
};

// Model options and state management
const modelOptions = ref<ModelOption[]>([]);
const selectedModel = ref<string>();
const modelReference = ref<string>();
const modelData = ref<any>();
const resolvedRefModels = ref<any>();

/**
 * Load model options from API
 */
const loadModelOptions = async () => {
  const [error, response] = await services.getCompData(apiBaseInfo.value.serviceId, ['requestBodies'], [], {}, false);
  if (error) {
    return;
  }

  modelOptions.value = (response.data || []).map((item: any) => ({
    ...item,
    label: item.ref,
    value: item.id
  }));
};

/**
 * Handle model selection change
 * @param value - Selected model value
 */
const handleModelSelectionChange = (value: string) => {
  selectedModel.value = value;
  const modelOption = modelOptions.value.find(option => option.value === value);

  if (modelOption) {
    modelReference.value = modelOption.ref;
    modelData.value = JSON.parse(modelOption.model);
    resolvedRefModels.value = modelOption.resolvedRefModels;
  }
};

/**
 * Filtered model options based on search keywords
 */
const filteredModelOptions = computed(() => {
  if (searchKeywords.value) {
    return modelOptions.value.filter(option =>
      option.key.toLowerCase().includes(searchKeywords.value?.toLowerCase() || '')
    );
  }
  return modelOptions.value;
});

/**
 * Watch for modal visibility changes and load data when needed
 */
watch(() => props.visible, (isVisible) => {
  if (isVisible && !modelOptions.value.length) {
    loadModelOptions();
  }
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('service.apiRequestBody.modal.title')"
    @cancel="closeModal"
    @ok="handleModelImport">
    <Input
      v-model:value="searchKeywords"
      class="w-50 mb-2"
      :placeholder="t('common.placeholders.searchKeyword')"
      size="small" />
    <div class="leading-8 text-text-content">
      <div style="background-color: #fafafa;" class="flex items-center px-3 rounded">
        <div class="flex-1/3">{{ t('service.apiRequestBody.modal.columns.componentName') }}</div>
        <div class="flex-1/3">{{ t('common.modifier') }}</div>
        <div class="flex-1/3">{{ t('common.modifiedDate') }}</div>
      </div>
      <Spin :spinning="isLoading">
        <template v-if="!isLoading && !filteredModelOptions?.length">
          <NoData class="flex items-center min-h-26" />
        </template>
        <template v-else>
          <div style="min-height: 104px;max-height: 328px;" class="px-3 pt-2 overflow-y-auto">
            <div
              v-for="item in filteredModelOptions"
              :key="item.id"
              class="flex items-center">
              <div class="flex-1/3 flex items-center">
                <Checkbox
                  :checked="selectedModel === item.value"
                  @change="handleModelSelectionChange(item.value)">
                  {{ item.key }}
                </Checkbox>
              </div>
              <div class="flex-1/3">{{ item.modifier }}</div>
              <div class="flex-1/3">{{ item.modifiedDate }}</div>
            </div>
          </div>
        </template>
      </Spin>
    </div>
  </Modal>
</template>

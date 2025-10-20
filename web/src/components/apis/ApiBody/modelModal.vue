<script setup lang="ts">
// Vue core imports
import { watch, inject, ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Modal, Spin, NoData } from '@xcan-angus/vue-ui';
import { Checkbox, Input } from 'ant-design-vue';

// Infrastructure imports
import { http, TESTER } from '@xcan-angus/infra';

// Utility imports
import { deconstruct } from '@/utils/apis';

const { t } = useI18n();

// Component props interface
interface Props {
  visible: boolean;
  getRefInfo: (serviceId: string, ref: string) => Promise<any>;
  getCompData: (serviceId: string, type: string[]) => Promise<any>;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// Component state management
const isLoading = ref(false);
const searchKeywords = ref();
const apiBaseInfo = inject('apiBaseInfo', ref());

// Component events
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm', value?: Record<string, any>): void;
}>();

/**
 * Close the model modal
 */
const closeModelModal = () => {
  emit('update:visible', false);
};

/**
 * Handle model import confirmation
 */
const handleModelImportConfirmation = async () => {
  if (!selectedModel.value) {
    closeModelModal();
  }
  const [error, response] = await http.get(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/ref`, { ref: modelReference.value });
  if (error) {
    return;
  }
  const deconstructedResult = deconstruct(response.data);
  emit('confirm', { schema: deconstructedResult || JSON.parse(response.data.model), ref: modelReference.value });
  closeModelModal();
};

// Component data state
const componentOptions = ref<any[]>([]);

/**
 * Load component options from API
 */
const loadComponentOptions = async () => {
  const [error, response] = await http.get(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/type`, {
    types: ['requestBodies'],
    keys: [],
    ignoreModel: true
  });
  if (error) {
    return;
  }
  componentOptions.value = (response.data || []).map(option => {
    return {
      ...option,
      label: option.ref,
      value: option.id
    };
  });
};

// Model selection state
const selectedModelValue = ref();
const modelReference = ref();
const resolvedReferenceModels = ref();
const selectedModel = ref();

/**
 * Handle model selection change
 * @param value - Selected model value
 */
const handleModelSelectionChange = (value: any) => {
  selectedModel.value = value;
  const modelObject = componentOptions.value.find(option => option.value === value);
  let modelData = modelObject.model;
  modelReference.value = modelObject.ref;
  modelData = JSON.parse(modelData);
  selectedModelValue.value = modelData;
  resolvedReferenceModels.value = modelObject.resolvedRefModels;
};

/**
 * Computed property for filtered component options based on search keywords
 */
const filteredComponentOptions = computed(() => {
  if (searchKeywords.value) {
    return componentOptions.value.filter(option => option.key.includes(searchKeywords.value));
  } else {
    return componentOptions.value;
  }
});

// Watch for modal visibility changes and load options
watch(() => props.visible, (isVisible) => {
  if (isVisible && !componentOptions.value.length) {
    loadComponentOptions();
  }
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('xcan_apiBody.component')"
    @cancel="closeModelModal"
    @ok="handleModelImportConfirmation">
    <Input
      v-model:value="searchKeywords"
      class="w-50 mb-2"
      :placeholder="t('xcan_apiBody.searchComponentName')"
      size="small" />
    <div class="leading-8 text-theme-content">
      <div style="background-color: #fafafa;" class="flex items-center px-3 rounded">
        <div class="flex-1/3">{{ t('xcan_apiBody.componentName') }}</div>
        <div class="flex-1/3">{{ t('common.modifier') }}</div>
        <div class="flex-1/3">{{ t('xcan_apiBody.modifyTime') }}</div>
      </div>
      <Spin :spinning="isLoading">
        <template v-if="!isLoading && !filteredComponentOptions?.length">
          <NoData class="flex items-center min-h-26" />
        </template>
        <template v-else>
          <div style="min-height: 104px;max-height: 328px;" class="px-3 pt-2 overflow-y-auto">
            <div
              v-for="option in filteredComponentOptions"
              :key="option.id"
              class="flex items-center">
              <div class="flex-1/3 flex items-center">
                <Checkbox :checked="selectedModel === option.value" @change="handleModelSelectionChange(option.value)">{{ option.key }}</Checkbox>
              </div>
              <div class="flex-1/3">{{ option.lastModifiedByName }}</div>
              <div class="flex-1/3">{{ option.lastModifiedDate }}</div>
            </div>
          </div>
        </template>
      </Spin>
    </div>
  </Modal>
</template>

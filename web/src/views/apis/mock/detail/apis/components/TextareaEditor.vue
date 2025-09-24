<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Input, Validate } from '@xcan-angus/vue-ui';

import { isHtml, isJSON, isXML, isYAML } from '@/utils/dataFormat';

// Props & Emits
interface Props {
  value: string;
  showAction?:boolean;
  maxlength?:number;
  showCount?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  showAction: true,
  maxlength: 20 * 1024 * 1024,
  showCount: false
});

const { t } = useI18n();

// ==================== Reactive State ====================
const content = ref('');
const error = ref(false);
const errorMessage = ref('');

// ==================== Computed Properties ====================
/**
 * Textarea auto size configuration
 */
const autoSize = {
  minRows: 10,
  maxRows: 10
};

/**
 * Placeholder text with max length information
 */
const placeholder = computed(() => {
  return t('mock.detail.apis.components.textareaEditor.maxLengthPlaceholder', { max: props.maxlength });
});

// ==================== Methods ====================
/**
 * Detect content type and return appropriate language
 * @param value - Content to analyze
 * @returns Detected language type
 */
const detectContentType = (value:string) => {
  if (isJSON(value)) {
    return 'json';
  }

  if (isXML(value) || isHtml(value)) {
    return 'html';
  }

  if (isYAML(value)) {
    return 'yaml';
  }

  return 'text';
};

/**
 * Format the current content
 */
const formatContent = () => {
  const _lang = detectContentType(content.value);
  if (_lang === 'json') {
    content.value = JSON.stringify(JSON.parse(content.value), null, 2);
  }
};

/**
 * Clear the textarea content
 */
const clearContent = () => {
  content.value = '';
};

/**
 * Validate the current content
 * @returns Whether content is valid
 */
const isValid = ():boolean => {
  const length = content.value.length;
  error.value = length >= props.maxlength;
  if (error.value) {
    errorMessage.value = t('mock.detail.apis.components.textareaEditor.maxLengthError', { max: props.maxlength, current: length });
  } else {
    errorMessage.value = '';
  }

  return !error.value;
};

/**
 * Get current textarea data
 * @returns Current content value
 */
const getData = ():string => {
  return content.value;
};

// ==================== Watchers ====================
onMounted(() => {
  watch(() => props.value, (newValue) => {
    content.value = newValue;
  }, { immediate: true });
});

// ==================== Expose Methods ====================
defineExpose({
  format: formatContent,
  isValid,
  getData
});
</script>

<template>
  <div>
    <div v-if="props.showAction" class="flex items-center justify-between">
      <slot name="leftextra"></slot>
      <div class="flex-1 flex items-center justify-end space-x-2">
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="formatContent">
          <!-- <Icon icon="icon-geshihua" class="mr-0.5 text-3.25" /> -->
          <span>{{ t('mock.detail.apis.components.textareaEditor.format') }}</span>
        </Button>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="clearContent">
          <!-- <Icon icon="icon-qingchu" class="mr-0.5 text-3.25" /> -->
          <span>{{ t('actions.clear') }}</span>
        </Button>
      </div>
    </div>

    <Validate fixed :text="errorMessage">
      <Input
        v-model:value="content"
        type="textarea"
        autocomplete="off"
        :placeholder="placeholder"
        :maxlength="props.maxlength"
        :showCount="props.showCount"
        :autoSize="autoSize"
        :error="error" />
    </Validate>
  </div>
</template>

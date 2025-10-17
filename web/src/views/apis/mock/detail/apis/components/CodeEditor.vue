<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Validate } from '@xcan-angus/vue-ui';

import MonacoEditor from '@/components/MonacoEditor/index.vue';
import { isHtml, isJSON, isXML, isYAML } from '@/utils/dataFormat';

// Props & Emits
interface Props {
  value: string;
  readonly?:boolean;
  showAction?:boolean;
  language?:'json' | 'html' | 'typescript' | 'text' | 'yaml';
  maxlength?:number;
  showCount?:boolean;
  notify?:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  readonly: false,
  showAction: true,
  language: undefined,
  maxlength: 20 * 1024 * 1024,
  showCount: false,
  notify: 0
});

const { t } = useI18n();


const emit = defineEmits<{
  (e: 'clear'): void;
  (e: 'change', value:string): void;
}>();

const editorRef = ref();
const editorLanguage = ref<'json' | 'html' | 'typescript' | 'text' | 'yaml'>('text');
const content = ref('');
const error = ref(false);
const errorMessage = ref<string>();

/**
 * Get the current editor language
 */
const currentLanguage = computed(() => {
  return props.language || editorLanguage.value;
});

/**
 * Handle content change event
 * @param value - New content value
 */
const handleContentChange = (value:string) => {
  error.value = false;
  if (value.length <= props.maxlength) {
    errorMessage.value = undefined;
  }
  emit('change', value);
};

/**
 * Detect content type and set appropriate language
 * @param value - Content to analyze
 * @returns Detected language type
 */
const detectContentType = (value:string) => {
  if (isJSON(value)) {
    editorLanguage.value = 'json';
  } else if (isXML(value) || isHtml(value)) {
    editorLanguage.value = 'html';
  } else if (isYAML(value)) {
    editorLanguage.value = 'yaml';
  } else {
    editorLanguage.value = 'text';
  }

  return editorLanguage.value;
};

/**
 * Format the current content
 */
const formatContent = () => {
  const _lang = props.language || detectContentType(content.value);
  if (_lang === 'json') {
    content.value = JSON.stringify(JSON.parse(content.value), null, 2);
    return;
  }

  if (typeof editorRef.value?.format === 'function') {
    editorRef.value.format();
  }
};

/**
 * Clear the editor content
 */
const clearContent = () => {
  content.value = '';
  editorLanguage.value = 'text';
  error.value = false;
  errorMessage.value = undefined;
  emit('clear');
};

/**
 * Get current editor data
 * @returns Current content value
 */
const getData = () => {
  return content.value;
};

/**
 * Validate the current content
 * @returns Whether content is valid
 */
const isValid = ():boolean => {
  error.value = false;
  errorMessage.value = undefined;
  const value = content.value;
  if (!value) {
    error.value = true;
    return false;
  }

  if (value.length >= props.maxlength) {
    error.value = true;
    errorMessage.value = t('actions.tips.maxLengthError', { max: props.maxlength, current: value.length });
    return false;
  }
  return true;
};

onMounted(() => {
  watch(() => props.notify, () => {
    content.value = '';
    editorLanguage.value = 'text';
    error.value = false;
    errorMessage.value = undefined;
  });

  watch(() => props.value, (newValue) => {
    content.value = newValue;
    detectContentType(newValue);
  }, { immediate: true });
});

defineExpose({
  format: formatContent,
  getData,
  isValid,
  clear: clearContent
});
</script>

<template>
  <div>
    <div v-if="props.showAction" class="flex items-center justify-between">
      <slot name="leftextra"></slot>
      <div class="flex-1 flex items-center justify-end space-x-2">
        <slot name="rightextra"></slot>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="formatContent">
          <span>{{ t('actions.format') }}</span>
        </Button>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="clearContent">
          <span>{{ t('actions.clear') }}</span>
        </Button>
      </div>
    </div>
    <Validate
      fixed
      :text="errorMessage"
      :class="{'error-border':error}"
      class="h-50 py-2 rounded border border-solid border-border-input">
      <MonacoEditor
        ref="editorRef"
        v-model:value="content"
        :readOnly="props.readonly"
        :language="currentLanguage"
        class="w-full h-full"
        @change="handleContentChange" />
    </Validate>
  </div>
</template>
<style scoped>
.error-border {
  border-color: rgba(245, 34, 45, 100%);
}
</style>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Validate } from '@xcan-angus/vue-ui';

import MonacoEditor from '@/components/monacoEditor/index.vue';
import { isHtml, isJSON, isXML, isYAML } from './utils';

const { t } = useI18n();

interface Props {
  value: string;
  readonly?:boolean;
  showAction?:boolean;
  language?:'json' | 'html' | 'typescript' | 'text' | 'yaml';
  maxlength?:number;// 最大支持字符数
  showCount?:boolean;// 是否显示已输入字符串数量
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

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'clear'): void;
  (e: 'change', value:string): void;
}>();

const editorRef = ref();
const editorLanguage = ref<'json' | 'html' | 'typescript' | 'text' | 'yaml'>('text');
const content = ref('');
const error = ref(false);
const errorMessage = ref<string>();

const change = (value:string) => {
  error.value = false;
  if (value.length <= props.maxlength) {
    errorMessage.value = undefined;
  }
  emit('change', value);
};

const detectType = (value:string) => {
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

const format = () => {
  const _lang = props.language || detectType(content.value);
  if (_lang === 'json') {
    content.value = JSON.stringify(JSON.parse(content.value), null, 2);
    return;
  }

  if (typeof editorRef.value?.format === 'function') {
    editorRef.value.format();
  }
};

const clear = () => {
  content.value = '';
  editorLanguage.value = 'text';
  error.value = false;
  errorMessage.value = undefined;
  emit('clear');
};

const _language = computed(() => {
  return props.language || editorLanguage.value;
});

onMounted(() => {
  watch(() => props.notify, () => {
    content.value = '';
    editorLanguage.value = 'text';
    error.value = false;
    errorMessage.value = undefined;
  });

  watch(() => props.value, (newValue) => {
    content.value = newValue;
    detectType(newValue);
  }, { immediate: true });
});

const getData = () => {
  return content.value;
};

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
    errorMessage.value = t('mock.mockApisComp.codeEditor.errorMessage', { maxlength: props.maxlength, length: value.length });
    return false;
  }

  return true;
};

defineExpose({
  format,
  getData,
  isValid,
  clear
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
          @click="format">
          <span>{{ t('mock.mockApisComp.codeEditor.format') }}</span>
        </Button>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="clear">
          <span>{{ t('mock.mockApisComp.codeEditor.clear') }}</span>
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
        :language="_language"
        class="w-full h-full"
        @change="change" />
    </Validate>
  </div>
</template>
<style scoped>
.error-border {
  border-color: rgba(245, 34, 45, 100%);
}
</style>

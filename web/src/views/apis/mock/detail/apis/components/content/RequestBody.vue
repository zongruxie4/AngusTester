<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { AsyncComponent, Colon, Select } from '@xcan-angus/vue-ui';
import { Radio, RadioGroup } from 'ant-design-vue';

import { ContentType, Language, PushbackBody, RadioType } from './types';

interface Props {
  value?:PushbackBody;
  contentType?:ContentType;
  notify?:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  contentType: undefined,
  notify: 0
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'contentTypeChange', value:{value:string;name:'Content-Type', prevValue:string|undefined}):void;
}>();

const PureFormInput = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/PureFormInput.vue'));
const CodeEditor = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/CodeEditor.vue'));

const languageMap = {
  'application/json': 'json',
  'text/html': 'html',
  'application/xml': 'html',
  'application/javascript': 'typescript',
  'text/plain': 'text'
};

const formInputRef = ref();
const codeEditorRef = ref();

const radioValue = ref<RadioType>('none');
const requestBodyContentType = ref<ContentType>();
const language = ref<Language>('json');
const rawContent = ref('');
const formDataList = ref<PushbackBody['forms']>([]);

const radioChange = (event: { target: { value: RadioType } }) => {
  const value = event.target.value;
  const prevValue = requestBodyContentType.value;
  radioValue.value = value;
  if (value === 'none') {
    emit('contentTypeChange', { value: 'none', name: 'Content-Type', prevValue });
    return;
  }

  if (['application/x-www-form-urlencode', 'multipart/form-data'].includes(value)) {
    requestBodyContentType.value = value as ContentType;
    if (typeof formInputRef.value?.clear === 'function') {
      formInputRef.value.clear();
    }

    if (typeof formInputRef.value?.add === 'function') {
      formInputRef.value.add();
    }

    emit('contentTypeChange', { value, name: 'Content-Type', prevValue });
    return;
  }

  requestBodyContentType.value = 'application/json';
  language.value = 'json';
  emit('contentTypeChange', { value: 'application/json', name: 'Content-Type', prevValue });
};

const selectChange = (value: ContentType) => {
  const prevValue = requestBodyContentType.value;
  requestBodyContentType.value = value;
  language.value = languageMap[value];
  emit('contentTypeChange', { value, name: 'Content-Type', prevValue });
};

const reset = () => {
  radioValue.value = 'none';
  requestBodyContentType.value = 'application/json';
  language.value = 'json';
  rawContent.value = '';
  formDataList.value = [];
};

onMounted(() => {
  watch([() => props.value, () => props.contentType], ([newValue, _contentType]) => {
    reset();
    setTimeout(() => {
      if (newValue) {
        const { rawContent: _rawContent, forms } = newValue;
        rawContent.value = _rawContent;
        formDataList.value = forms;
      }

      requestBodyContentType.value = _contentType;
      if (_contentType === 'application/x-www-form-urlencode') {
        radioValue.value = 'application/x-www-form-urlencode';
      } else if (_contentType === 'multipart/form-data') {
        radioValue.value = 'multipart/form-data';
      } else if (_contentType) {
        radioValue.value = 'raw';
      } else {
        radioValue.value = 'none';
      }
    }, 0);
  }, { immediate: true });
});

defineExpose({
  getData: ():PushbackBody|undefined => {
    if (radioValue.value === 'none') {
      return undefined;
    }

    if (['application/x-www-form-urlencode', 'multipart/form-data'].includes(radioValue.value)) {
      let forms: {
        name: string;
        value: string;
      }[] = [];
      if (typeof formInputRef.value?.getData === 'function') {
        forms = formInputRef.value.getData();
      }

      return {
        contentType: radioValue.value,
        forms,
        rawContent: ''
      };
    }

    let rawContent = '';
    if (typeof codeEditorRef.value?.getData === 'function') {
      rawContent = codeEditorRef.value.getData();
    }

    return {
      contentType: requestBodyContentType.value,
      rawContent,
      forms: []
    };
  }
});

const radioOptions: readonly { label: string; value: RadioType }[] = [
  {
    label: 'none',
    value: 'none'
  },
  {
    label: 'application/x-www-form-urlencode',
    value: 'application/x-www-form-urlencode'
  },
  {
    label: 'multipart/form-data',
    value: 'multipart/form-data'
  },
  {
    label: 'raw',
    value: 'raw'
  }
];

const selectOptions: readonly { label: string; value: ContentType }[] = [
  {
    label: 'application/json',
    value: 'application/json'
  },
  {
    label: 'text/html',
    value: 'text/html'
  },
  {
    label: 'application/xml',
    value: 'application/xml'
  },
  {
    label: 'application/javascript',
    value: 'application/javascript'
  },
  {
    label: 'text/plain',
    value: 'text/plain'
  },
  {
    label: '*/*',
    value: '*/*'
  }
];

const fielaNames = { label: 'name', value: 'value' };
</script>
<template>
  <div>
    <div class="flex items-center mb-0.5">请求体</div>
    <div class="flex items-center h-7">
      <div class="mr-3.5">
        <span>Content-Type</span>
        <Colon />
      </div>
      <RadioGroup
        :value="radioValue"
        name="radioGroup"
        @change="radioChange">
        <Radio
          v-for="item in radioOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label }}
        </Radio>
      </RadioGroup>
      <Select
        v-if="radioValue === 'raw'"
        :value="requestBodyContentType"
        :options="selectOptions"
        class="w-38"
        @change="selectChange" />
    </div>
    <PureFormInput
      v-show="radioValue === 'application/x-www-form-urlencode' || radioValue === 'multipart/form-data'"
      ref="formInputRef"
      class="mt-2"
      :fielaNames="fielaNames"
      :value="formDataList"
      :notify="props.notify" />
    <AsyncComponent :visible="radioValue === 'raw'">
      <CodeEditor
        v-show="radioValue === 'raw'"
        ref="codeEditorRef"
        :value="rawContent"
        :language="language"
        :notify="props.notify"
        class="mt-2" />
    </AsyncComponent>
  </div>
</template>

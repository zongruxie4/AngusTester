<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Colon, Select } from '@xcan-angus/vue-ui';
import { Radio, RadioGroup } from 'ant-design-vue';

import { ContentType, Language, PushbackBody, RadioType } from './types';
import { CONTENT_TYPE, HTTP_HEADERS, LANGUAGE, RADIO_TYPE } from '@/utils/constant';

/**
 * <p>Props interface for RequestBody component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value?: PushbackBody;
  contentType?: ContentType;
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  contentType: undefined,
  notify: 0
});

const { t } = useI18n();

/**
 * <p>Component events interface</p>
 * <p>Defines the events that this component can emit</p>
 */
const emit = defineEmits<{
  (e: 'contentTypeChange', value: { value: string; name: HTTP_HEADERS.CONTENT_TYPE, prevValue: string | undefined }): void;
}>();

// ==================== Async Components ====================
const PureFormInput = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/PureFormInput.vue'));
const CodeEditor = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/CodeEditor.vue'));

// ==================== Configuration ====================
/**
 * <p>Language mapping for code editor</p>
 * <p>Maps content types to their corresponding language modes</p>
 */
const languageMap = {
  [CONTENT_TYPE.JSON]: LANGUAGE.JSON,
  [CONTENT_TYPE.HTML]: LANGUAGE.HTML,
  [CONTENT_TYPE.XML]: LANGUAGE.HTML,
  [CONTENT_TYPE.JAVASCRIPT]: LANGUAGE.TYPESCRIPT,
  [CONTENT_TYPE.TEXT_PLAIN]: LANGUAGE.TEXT
};

// ==================== Template Refs ====================
const formInputRef = ref();
const codeEditorRef = ref();

// ==================== Reactive State ====================
/**
 * <p>Selected radio button value</p>
 * <p>Determines which request body type is selected</p>
 */
const radioValue = ref<RadioType>(RADIO_TYPE.NONE);

/**
 * <p>Request body content type</p>
 * <p>Specifies the media type of the request body</p>
 */
const requestBodyContentType = ref<ContentType>();

/**
 * <p>Code editor language mode</p>
 * <p>Determines syntax highlighting for the code editor</p>
 */
const language = ref<Language>(LANGUAGE.JSON);

/**
 * <p>Raw content for code editor</p>
 * <p>Stores the actual request body content</p>
 */
const rawContent = ref('');

/**
 * <p>Form data list for form inputs</p>
 * <p>Stores form field data for form-encoded requests</p>
 */
const formDataList = ref<PushbackBody['forms']>([]);

// ==================== Event Handlers ====================
/**
 * <p>Handles radio button change event</p>
 * <p>Updates request body type and manages form state accordingly</p>
 *
 * @param event - Radio change event
 */
const handleRadioChange = (event: { target: { value: RadioType } }) => {
  const value = event.target.value;
  const prevValue = requestBodyContentType.value;
  radioValue.value = value;

  if (value === RADIO_TYPE.NONE) {
    emit('contentTypeChange', { value: RADIO_TYPE.NONE, name: HTTP_HEADERS.CONTENT_TYPE, prevValue });
    return;
  }

  if ([RADIO_TYPE.FORM_URLENCODED, RADIO_TYPE.MULTIPART_FORM_DATA].includes(value)) {
    requestBodyContentType.value = value as ContentType;
    if (typeof formInputRef.value?.clear === 'function') {
      formInputRef.value.clear();
    }

    if (typeof formInputRef.value?.add === 'function') {
      formInputRef.value.add();
    }

    emit('contentTypeChange', { value, name: HTTP_HEADERS.CONTENT_TYPE, prevValue });
    return;
  }

  requestBodyContentType.value = CONTENT_TYPE.JSON;
  language.value = LANGUAGE.JSON;
  emit('contentTypeChange', { value: CONTENT_TYPE.JSON, name: HTTP_HEADERS.CONTENT_TYPE, prevValue });
};

/**
 * <p>Handles content type select change event</p>
 * <p>Updates content type and language mode for raw content</p>
 *
 * @param value - Selected content type
 */
const handleSelectChange = (value: ContentType) => {
  const prevValue = requestBodyContentType.value;
  requestBodyContentType.value = value;
  language.value = languageMap[value];
  emit('contentTypeChange', { value, name: HTTP_HEADERS.CONTENT_TYPE, prevValue });
};

// ==================== Utility Methods ====================
/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and resets to default values</p>
 */
const resetComponent = () => {
  radioValue.value = RADIO_TYPE.NONE;
  requestBodyContentType.value = CONTENT_TYPE.JSON;
  language.value = LANGUAGE.JSON;
  rawContent.value = '';
  formDataList.value = [];
};

// ==================== Lifecycle Hooks ====================
/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
onMounted(() => {
  watch([() => props.value, () => props.contentType], ([newValue, _contentType]) => {
    resetComponent();
    setTimeout(() => {
      if (newValue) {
        const { rawContent: _rawContent, forms } = newValue;
        rawContent.value = _rawContent;
        formDataList.value = forms;
      }

      requestBodyContentType.value = _contentType;
      if (_contentType === CONTENT_TYPE.FORM_URLENCODED) {
        radioValue.value = RADIO_TYPE.FORM_URLENCODED;
      } else if (_contentType === CONTENT_TYPE.MULTIPART_FORM_DATA) {
        radioValue.value = RADIO_TYPE.MULTIPART_FORM_DATA;
      } else if (_contentType) {
        radioValue.value = RADIO_TYPE.RAW;
      } else {
        radioValue.value = RADIO_TYPE.NONE;
      }
    }, 0);
  }, { immediate: true });
});

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides getData method for external access</p>
 */
defineExpose({
  /**
   * <p>Gets the current request body configuration data</p>
   * <p>Returns formatted request body data based on current selection</p>
   *
   * @returns PushbackBody object with current configuration or undefined
   */
  getData: (): PushbackBody | undefined => {
    if (radioValue.value === RADIO_TYPE.NONE) {
      return undefined;
    }

    if ([RADIO_TYPE.FORM_URLENCODED, RADIO_TYPE.MULTIPART_FORM_DATA].includes(radioValue.value)) {
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

// ==================== Configuration ====================
/**
 * <p>Radio button options for request body type</p>
 * <p>Defines available request body types</p>
 */
const radioOptions: readonly { label: string; value: RadioType }[] = [
  {
    label: RADIO_TYPE.NONE,
    value: RADIO_TYPE.NONE
  },
  {
    label: RADIO_TYPE.FORM_URLENCODED,
    value: RADIO_TYPE.FORM_URLENCODED
  },
  {
    label: RADIO_TYPE.MULTIPART_FORM_DATA,
    value: RADIO_TYPE.MULTIPART_FORM_DATA
  },
  {
    label: RADIO_TYPE.RAW,
    value: RADIO_TYPE.RAW
  }
];

/**
 * <p>Select options for content type</p>
 * <p>Defines available content types for raw content</p>
 */
const selectOptions: readonly { label: string; value: ContentType }[] = [
  {
    label: CONTENT_TYPE.JSON,
    value: CONTENT_TYPE.JSON
  },
  {
    label: CONTENT_TYPE.HTML,
    value: CONTENT_TYPE.HTML
  },
  {
    label: CONTENT_TYPE.XML,
    value: CONTENT_TYPE.XML
  },
  {
    label: CONTENT_TYPE.JAVASCRIPT,
    value: CONTENT_TYPE.JAVASCRIPT
  },
  {
    label: CONTENT_TYPE.TEXT_PLAIN,
    value: CONTENT_TYPE.TEXT_PLAIN
  },
  {
    label: CONTENT_TYPE.WILDCARD,
    value: CONTENT_TYPE.WILDCARD
  }
];

/**
 * <p>Field name mapping for form components</p>
 * <p>Defines how form fields map to data properties</p>
 */
const fielaNames = { label: 'name', value: 'value' };
</script>
<template>
  <div>
    <div class="flex items-center mb-0.5">{{ t('mock.detail.apis.components.requestBody.title') }}</div>
    <div class="flex items-center h-7">
      <div class="mr-3.5">
        <span>{{ t('mock.detail.apis.components.requestBody.contentType') }}</span>
        <Colon />
      </div>
      <RadioGroup
        :value="radioValue"
        name="radioGroup"
        @change="handleRadioChange">
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
        @change="handleSelectChange" />
    </div>
    <PureFormInput
      v-show="radioValue === RADIO_TYPE.FORM_URLENCODED || radioValue === RADIO_TYPE.MULTIPART_FORM_DATA"
      ref="formInputRef"
      class="mt-2"
      :fielaNames="fielaNames"
      :value="formDataList"
      :notify="props.notify" />
    <AsyncComponent :visible="radioValue === RADIO_TYPE.RAW">
      <CodeEditor
        v-show="radioValue === RADIO_TYPE.RAW"
        ref="codeEditorRef"
        :value="rawContent"
        :language="language"
        :notify="props.notify"
        class="mt-2" />
    </AsyncComponent>
  </div>
</template>

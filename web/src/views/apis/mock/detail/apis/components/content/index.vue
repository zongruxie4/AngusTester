<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, IconRequired, notification, SelectInput, Tooltip, FunctionsButton } from '@xcan-angus/vue-ui';
import { ResponseDelayMode, ContentEncoding, HttpResponseHeader } from '@xcan-angus/infra';

import { DelayData, ResponseContentConfig, ResponseHeader } from './types';
import DelayParameter from './DelayParameter.vue';
import SelectInputForm from '@/views/apis/mock/detail/apis/components/content/SelectInputForm.vue';
import { HTTP_HEADERS } from '@/utils/constant';

/**
 * <p>Props interface for ResponseContentConfig component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: ResponseContentConfig;
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

// ==================== Async Components ====================
const CodeEditor = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/CodeEditor.vue'));
const ImportFileModal = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/content/ImportFileModal.vue'));

// ==================== Constants ====================
/**
 * <p>Maximum number of response headers allowed</p>
 * <p>Prevents excessive header configuration</p>
 */
const MAX_HEADER_NUM = 200;

// ==================== Template Refs ====================
const pureInputRef = ref();
const delayRef = ref();
const codeEditorRef = ref();

// ==================== Reactive State ====================
/**
 * <p>HTTP status code for response</p>
 * <p>Default value is 200 (OK)</p>
 */
const httpStatus = ref<string>('200');

/**
 * <p>List of header IDs for tracking</p>
 * <p>Used to manage header form state</p>
 */
const headerIds = ref<string[]>([]);

/**
 * <p>Response body content</p>
 * <p>Stores the actual response content data</p>
 */
const responseContent = ref('');

/**
 * <p>Content-Type header value</p>
 * <p>Specifies the media type of the response</p>
 */
const contentType = ref('');

/**
 * <p>Content encoding method</p>
 * <p>Determines how content is encoded (base64, gzip_base64, etc.)</p>
 */
const contentEncoding = ref<ContentEncoding>();

/**
 * <p>Response header parameters</p>
 * <p>Array of header name-value pairs</p>
 */
const headerParameters = ref<ResponseHeader[]>([]);

/**
 * <p>HTTP status input validation error state</p>
 * <p>Indicates if status code input is invalid</p>
 */
const httpStatusError = ref(false);

/**
 * <p>Import file modal visibility state</p>
 * <p>Controls whether file import modal is shown</p>
 */
const importFileVisible = ref(false);

/**
 * <p>Code editor readonly state</p>
 * <p>Prevents editing when file is imported</p>
 */
const codeEditorReadonly = ref(false);

// ==================== Event Handlers ====================
/**
 * <p>Handles HTTP status code change event</p>
 * <p>Updates status code and clears validation errors</p>
 *
 * @param value - The new status code value
 */
const handleHttpStatusChange = (value: string) => {
  httpStatus.value = value;
  httpStatusError.value = false;
};

/**
 * <p>Handles add response header button click</p>
 * <p>Adds a new header field if under the maximum limit</p>
 */
const handleAddResponseHeader = () => {
  if (typeof pureInputRef.value?.add === 'function') {
    if (headerIds.value.length >= MAX_HEADER_NUM) {
      notification.error(t('mock.detail.apis.components.content.maxHeadersTip', { max: MAX_HEADER_NUM }));
      return;
    }

    pureInputRef.value.add();
  }
};

/**
 * <p>Handles header form input change event</p>
 * <p>Updates the list of header IDs when headers are modified</p>
 *
 * @param ids - Array of header IDs
 */
const handleHeaderFormInputChange = (ids: string[]) => {
  headerIds.value = ids;
};

/**
 * <p>Handles insert file button click</p>
 * <p>Shows the file import modal</p>
 */
const handleInsertFile = () => {
  importFileVisible.value = true;
};

/**
 * <p>Handles file import confirmation</p>
 * <p>Processes imported file and updates content accordingly</p>
 *
 * @param value - Base64 encoded file content
 * @param file - The imported file object
 * @param encoding - Content encoding method used
 */
const handleImportFileOk = (value: string, file: File, encoding: ContentEncoding) => {
  const fileType = file.type;
  pureInputRef.value.add({ name: HTTP_HEADERS.CONTENT_TYPE, value: fileType, disabled: true, prevValue: contentType.value });
  responseContent.value = value;
  contentEncoding.value = encoding;
  codeEditorReadonly.value = true;
  contentType.value = fileType;
};

/**
 * <p>Handles code editor clear event</p>
 * <p>Removes Content-Type header and resets editor state</p>
 */
const handleCodeEditorClear = () => {
  pureInputRef.value.del({ name: HTTP_HEADERS.CONTENT_TYPE, prevValue: contentType.value });
  contentType.value = '';
  contentEncoding.value = undefined;
  codeEditorReadonly.value = false;
};

// ==================== Computed Properties ====================
/**
 * <p>Determines if add header button should be disabled</p>
 * <p>Returns true when maximum header limit is reached</p>
 */
const isAddHeaderDisabled = computed(() => {
  return headerIds.value.length >= MAX_HEADER_NUM;
});

// ==================== Utility Methods ====================
/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and resets to default values</p>
 */
const resetComponent = () => {
  httpStatus.value = '200';
  headerIds.value = [];
  responseContent.value = '';
  contentType.value = '';
  contentEncoding.value = undefined;
  headerParameters.value = [];
  httpStatusError.value = false;
  importFileVisible.value = false;
  codeEditorReadonly.value = false;
};

// ==================== Lifecycle Hooks ====================
/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
onMounted(() => {
  watch(() => props.value, (newValue) => {
    resetComponent();
    if (!newValue) {
      return;
    }

    const { contentEncoding: encoding, headers, status, content = '' } = newValue;
    if (headers?.length) {
      for (let i = 0, len = headers.length; i < len; i++) {
        const data = { ...headers[i], disabled: false };
        if (data.name === HTTP_HEADERS.CONTENT_TYPE) {
          contentType.value = data.value;
          if (encoding && [ContentEncoding.base64, ContentEncoding.gzip_base64].includes(encoding)) {
            codeEditorReadonly.value = true;
            data.disabled = true;
            headerParameters.value.push(data);
          }
        } else {
          headerParameters.value.push(data);
        }
      }
    }

    contentEncoding.value = encoding;
    httpStatus.value = status;
    setTimeout(() => {
      responseContent.value = content;
    }, 0);
  }, { immediate: true });
});

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides getData and isValid methods for external access</p>
 */
defineExpose({
  /**
   * <p>Gets the current response content configuration data</p>
   * <p>Collects data from all child components and returns complete config</p>
   *
   * @returns ResponseContentConfig object with current configuration
   */
  getData: (): ResponseContentConfig => {
    let content = '';
    if (typeof codeEditorRef.value?.getData === 'function') {
      content = codeEditorRef.value.getData();
    }

    let headers: ResponseHeader[] = [];
    if (typeof pureInputRef.value?.getData === 'function') {
      headers = pureInputRef.value.getData();
    }

    let delay: DelayData = {
      mode: ResponseDelayMode.NONE,
      fixedTime: undefined,
      maxRandomTime: undefined,
      minRandomTime: undefined
    };
    if (typeof delayRef.value?.getData === 'function') {
      delay = delayRef.value.getData();
    }

    return {
      content,
      headers,
      delay,
      contentEncoding: contentEncoding.value,
      status: httpStatus.value
    };
  },

  /**
   * <p>Validates the current form state</p>
   * <p>Checks if all required fields and child components are valid</p>
   *
   * @returns true if form is valid, false otherwise
   */
  isValid: (): boolean => {
    let errorNum = 0;
    httpStatusError.value = false;
    if (!httpStatus.value) {
      httpStatusError.value = true;
      errorNum++;
    }

    if (typeof pureInputRef.value?.isValid === 'function') {
      if (!pureInputRef.value.isValid()) {
        errorNum++;
      }
    }

    if (typeof delayRef.value?.isValid === 'function') {
      if (!delayRef.value.isValid()) {
        errorNum++;
      }
    }

    return !errorNum;
  }
});

// ==================== Configuration ====================
/**
 * <p>Field name mapping for input components</p>
 * <p>Defines how form fields map to data properties</p>
 */
const inputFieldNames = { label: 'name', value: 'value' };

/**
 * <p>Field name mapping for select components</p>
 * <p>Defines how select options map to data properties</p>
 */
const fieldNames = { label: '_value', value: 'id' };

/**
 * <p>Input properties configuration</p>
 * <p>Common properties applied to input components</p>
 */
const inputProps = {
  dataType: 'number',
  max: 1000,
  min: 1,
  trimAll: true
};
</script>
<template>
  <div>
    <div class="flex leading-5 mb-4 space-x-2">
      <div class="space-y-2">
        <div class="flex items-center h-7">
          <IconRequired />
          <span>{{ t('protocol.statusCode') }}</span>
        </div>
        <div class="flex items-center h-7">
          <span>{{ t('protocol.responseDelay') }}</span>
        </div>
      </div>
      <div class="space-y-2">
        <SelectInput
          :value="httpStatus"
          mode="combination"
          class="w-30"
          enumKey="HttpStatus"
          :error="httpStatusError"
          :maxlength="4"
          :inputProps="inputProps"
          :fieldNames="fieldNames"
          @change="handleHttpStatusChange">
          <template #option="record">
            <div class="truncate" :title="record._value + ' - ' + record.message">
              {{ record._value }} - {{ record.message }}
            </div>
          </template>
        </SelectInput>
        <DelayParameter
          ref="delayRef"
          :value="props.value?.delay"
          :notify="props.notify" />
      </div>
    </div>
    <div class="flex items-center">
      <Button
        type="default"
        size="small"
        :disabled="isAddHeaderDisabled"
        @click="handleAddResponseHeader">
        <Icon icon="icon-jia" class="mr-1" />
        <span>{{ t('protocol.responseHeader') }}</span>
      </Button>
      <Tooltip>
        <template #title>{{ t('mock.detail.apis.components.content.maxHeadersTooltip', { max: MAX_HEADER_NUM }) }}</template>
        <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
      </Tooltip>
    </div>
    <SelectInputForm
      ref="pureInputRef"
      :enumKey="HttpResponseHeader"
      :fielaNames="inputFieldNames"
      :value="headerParameters"
      :notify="props.notify"
      class="mt-2"
      @change="handleHeaderFormInputChange" />
    <CodeEditor
      ref="codeEditorRef"
      :notify="props.notify"
      :value="responseContent"
      :readonly="codeEditorReadonly"
      class="mt-4.5"
      @clear="handleCodeEditorClear">
      <template #leftextra><div>{{ t('protocol.responseBody') }}</div></template>
      <template #rightextra>
        <FunctionsButton>
          <Button
            style="padding: 0;"
            type="link"
            size="small">
            {{ t('mock.detail.apis.components.content.mockFunctionHelper') }}
          </Button>
        </FunctionsButton>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="handleInsertFile">
          <span>{{ t('mock.detail.apis.components.content.insertFile') }}</span>
        </Button>
      </template>
    </CodeEditor>
    <AsyncComponent :visible="importFileVisible">
      <ImportFileModal v-model:visible="importFileVisible" @ok="handleImportFileOk" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
.grid-container > :deep(div:first-child) > div:first-child > div:first-child {
  padding-left: 8px;
}
</style>

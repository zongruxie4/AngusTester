<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch, onMounted } from 'vue';
import { Icon, notification, Select, MonacoEditor } from '@xcan-angus/vue-ui';
import { Button, RadioGroup, Radio, Upload } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration, utils, codeUtils } from '@xcan-angus/infra';
import pretty from 'pretty';
import jsBeautify from 'js-beautify';
import { useI18n } from 'vue-i18n';

import { RequestBody, ContentType } from './PropsType';
const { gzip, ungzip } = codeUtils;
export interface Props {
  value: RequestBody;
  contentType: ContentType | null;
  maxFileSize?: number;// 总大小上传限额，单位字节
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  contentType: null,
  maxFileSize: 10485760
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: RequestBody): void;
  (e: 'contentTypeChange', value: ContentType): void;
  (e: 'update:errorNum', value: number): void;
}>();

const RequestBodyParamster = defineAsyncComponent(() => import('./RequestBodyParamster/index.vue'));

const ENCODED_TYPE = 'application/x-www-form-urlencoded';
const STREAM_TYPE = 'application/octet-stream';
const FORMDATA_TYPE = 'multipart/form-data';

const formDataFormRef = ref();
const urlencodeFormRef = ref();

const formDataList = ref<RequestBody['forms']>([]);
const encodeedList = ref<RequestBody['forms']>([]);
const rawContent = ref('');
const contentTypeValue = ref<ContentType>();
const fileName = ref(); // 文件名称
const binaryContent = ref<string>();
const gziping = ref(false);
const downloadUrl = ref<string>();

const radioGroupChange = (event: { target: { value: ContentType } }): void => {
  reset();

  const value = event.target.value;
  contentTypeValue.value = value;
  emit('contentTypeChange', value);
  emitChange();
};

const radioChange = (event: { target: { checked: boolean; } }) => {
  const checked = event.target.checked;
  if (checked) {
    contentTypeValue.value = rawSelectOptions.value[0].value;
  }

  emit('contentTypeChange', contentTypeValue.value);
  reset();
  emitChange();
};

const selectChange = (): void => {
  emit('contentTypeChange', contentTypeValue.value);
};

const changeEncodeedList = (index: number, value: RequestBody['forms'][number]): void => {
  encodeedList.value[index] = value;
  emitChange();
};

const addEncodeedList = (value: RequestBody['forms'][number]): void => {
  encodeedList.value.push(value);
  emitChange();
};

const delEncodeedList = (index: number): void => {
  encodeedList.value.splice(index, 1);
  emitChange();
};

const changeFormDataList = (index: number, value: RequestBody['forms'][number]) => {
  formDataList.value[index] = value;
  emitChange();
};

const addFormDataList = (value: RequestBody['forms'][number]) => {
  formDataList.value.push(value);
  emitChange();
};

const delFormDataList = (index: number) => {
  formDataList.value.splice(index, 1);
  emitChange();
};

const errorNumChange = (value: number) => {
  emit('update:errorNum', value);
};

const customRequest = async ({ file }) => {
  if (file.size > props.maxFileSize) {
    notification.error(t('httPlugin.uiConfig.httpConfigs.requestBodyConfig.maxFileSize', { size: utils.formatBytes(props.maxFileSize) }));
    return;
  }

  gziping.value = true;
  // 修复gzip时，页面渲染卡顿问题
  setTimeout(async () => {
    binaryContent.value = await gzip(file);
    gziping.value = false;
    fileName.value = file.name;
    downloadUrl.value = window.URL.createObjectURL(file);
    emitChange();
  }, 100);
};

const removeFile = () => {
  fileName.value = undefined;
  binaryContent.value = undefined;
  emitChange();
};

const editorChange = debounce(duration.delay, (value:string) => {
  rawContent.value = value;
  emitChange();
});

// 格式化文本
const formatRawContent = () => {
  if (contentTypeValue.value === 'application/json') {
    try {
      const json = JSON.parse(rawContent.value);
      rawContent.value = JSON.stringify(json, null, 2);
    } catch { }
    return;
  }

  if (contentTypeValue.value === 'application/xml') {
    try {
      rawContent.value = rawContent.value.replace(/(>)(<)(\/*)/g, '$1\n$2$3');
    } catch { }
    return;
  }

  if (contentTypeValue.value === 'text/html') {
    rawContent.value = pretty(rawContent.value, { indent_size: 2 });
    return;
  }

  if (contentTypeValue.value === 'application/javascript') {
    rawContent.value = jsBeautify(rawContent.value, { indent_size: 2, space_in_empty_paren: true });
  }
};

// 压缩文本
const compressRawContent = () => {
  if (contentTypeValue.value === 'application/json') {
    try {
      const json = JSON.parse(rawContent.value);
      rawContent.value = JSON.stringify(json, null, 0);
    } catch { }
    return;
  }

  if (contentTypeValue.value === 'application/xml') {
    try {
      rawContent.value = rawContent.value.replace(/(>)(\n)(<)(\/*)/g, '$1$3$4');
    } catch { }
    return;
  }

  if (contentTypeValue.value === 'text/html') {
    rawContent.value = pretty(rawContent.value, { indent_size: 0 });
    return;
  }

  if (contentTypeValue.value === 'application/javascript') {
    rawContent.value = jsBeautify(rawContent.value, { indent_size: 0, space_in_empty_paren: true });
    return;
  }

  rawContent.value = rawContent.value.replace(/\n|\t|\r|\n\r/g, '');
};

const emitChange = (): void => {
  const params = getData();
  emit('change', params);
};

const reset = () => {
  formDataList.value = [];
  encodeedList.value = [];
  rawContent.value = '';
  contentTypeValue.value = undefined;
  fileName.value = undefined; // 文件名称
  binaryContent.value = undefined;
  gziping.value = false;
  downloadUrl.value = undefined;
};

onMounted(() => {
  watch([() => props.value, () => props.contentType], () => {
    reset();

    const _contentType = props.contentType;
    contentTypeValue.value = _contentType;

    if (props.value) {
      const { forms, rawContent: _rawContent = '', fileName: _fileName } = props.value;
      const formList = forms || [];
      if (contentTypeValue.value === ENCODED_TYPE) {
        encodeedList.value = formList;
      } else if (contentTypeValue.value === FORMDATA_TYPE) {
        formDataList.value = formList;
      } else if (NO_BINARY_TYPES.includes(contentTypeValue.value)) {
        rawContent.value = _rawContent;
      } else {
        fileName.value = _fileName;
        binaryContent.value = _rawContent;
        setTimeout(() => {
          if (_fileName && _rawContent) {
            const arrayBuffer = ungzip(_rawContent);
            const file = new File([arrayBuffer], _fileName, { type: contentTypeValue.value });
            const dataURL = window.URL.createObjectURL(file);
            downloadUrl.value = dataURL;
          }
        }, 16.67);
      }
    }
  }, { immediate: true });
});

const isValid = () => {
  if (contentTypeValue.value === ENCODED_TYPE) {
    return urlencodeFormRef.value.isValid();
  }

  if (contentTypeValue.value === FORMDATA_TYPE) {
    return formDataFormRef.value.isValid();
  }

  return true;
};

const getData = (): RequestBody => {
  if (!contentTypeValue.value) {
    return {
      forms: null,
      rawContent: null,
      contentEncoding: null,
      format: null,
      type: null,
      fileName: null
    };
  }

  if (contentTypeValue.value === ENCODED_TYPE) {
    const forms = encodeedList.value.slice(0, -1);
    return {
      forms,
      rawContent: null,
      contentEncoding: null,
      format: null,
      type: null,
      fileName: null
    };
  }

  if (contentTypeValue.value === FORMDATA_TYPE) {
    const forms = formDataList.value.slice(0, -1);
    return {
      forms,
      rawContent: null,
      contentEncoding: null,
      format: null,
      type: null,
      fileName: null
    };
  }

  if (rawSelectOptionValues.value.includes(contentTypeValue.value)) {
    return {
      forms: null,
      rawContent: rawContent.value,
      contentEncoding: null,
      format: null,
      type: 'string',
      fileName: null
    };
  }

  return {
    forms: null,
    rawContent: binaryContent.value,
    contentEncoding: 'gzip_base64',
    format: 'binary',
    type: 'string',
    fileName: fileName.value
  };
};

defineExpose({ getData, isValid });

const CONTENT_TYPE: ContentType[] = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/octet-stream',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

const NO_BINARY_TYPES = [
  'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

const radioGroups = computed(() => {
  return [null, 'application/x-www-form-urlencoded', 'multipart/form-data', 'application/octet-stream'];
});

const rawSelectOptions = computed(() => {
  return CONTENT_TYPE.filter(item => !radioGroups.value.includes(item)).map(item => ({ label: item, value: item }));
});

const rawSelectOptionValues = computed(() => {
  return rawSelectOptions.value.map(item => item.value);
});

const radioGroupOptions = computed(() => {
  const options: { value: ContentType; label: string; }[] = CONTENT_TYPE.filter(item => radioGroups.value.includes(item)).map(item => ({ label: item, value: item }));
  options.unshift({ value: null, label: 'none' });
  return options;
});

const checkeRaw = computed(() => {
  return !!rawSelectOptions.value.find(item => item.value === contentTypeValue.value);
});

const isRaw = computed(() => {
  return ['application/json', 'text/html', 'application/javascript', 'application/xml', '*/*', 'text/plain'].includes(contentTypeValue.value);
});

const showEncodeedForm = computed(() => {
  return contentTypeValue.value === ENCODED_TYPE;
});

const showFormDataForm = computed(() => {
  return contentTypeValue.value === FORMDATA_TYPE;
});

const language = computed(() => {
  switch (contentTypeValue.value) {
    case 'application/json':
      return 'json';
    case 'text/html':
    case 'application/xml':
      return 'html';
    case 'application/javascript':
      return 'typescript';
    case 'text/plain':
      return 'text';
    default:
      return 'text';
  }
});
</script>
<template>
  <div>
    <div class="flex items-center flex-wrap whitespace-nowrap">
      <div class="text-3 leading-7 text-gray-content mr-3">
        <span>Content-Type</span>
      </div>
      <RadioGroup
        v-model:value="contentTypeValue"
        class="select-none mr-2"
        name="radioGroup"
        @change="radioGroupChange">
        <Radio
          v-for="item in radioGroupOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label === STREAM_TYPE ? 'binary' : item.label }}
        </Radio>
      </RadioGroup>
      <Radio :checked="checkeRaw" @change="radioChange">raw</Radio>
      <Select
        v-show="isRaw"
        v-model:value="contentTypeValue"
        class="w-40"
        :options="rawSelectOptions"
        @change="selectChange" />
      <template v-if="isRaw && rawContent">
        <Button
          type="link"
          size="small"
          class="ml-2"
          @click="formatRawContent">
          {{ t('httPlugin.uiConfig.httpConfigs.requestBodyConfig.formatRawContent') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="compressRawContent">
          {{ t('httPlugin.uiConfig.httpConfigs.requestBodyConfig.compressRawContent') }}
        </Button>
      </template>
    </div>
    <RequestBodyParamster
      v-if="showEncodeedForm"
      key="encoded"
      ref="urlencodeFormRef"
      class="mt-2"
      :value="encodeedList"
      @change="changeEncodeedList"
      @add="addEncodeedList"
      @del="delEncodeedList"
      @errorNumChange="errorNumChange" />
    <RequestBodyParamster
      v-else-if="showFormDataForm"
      ref="formDataFormRef"
      key="formData"
      class="mt-2"
      hasFileType
      :maxFileSize="props.maxFileSize"
      :value="formDataList"
      @change="changeFormDataList"
      @add="addFormDataList"
      @del="delFormDataList"
      @errorNumChange="errorNumChange" />

    <MonacoEditor
      v-else-if="isRaw"
      :value="rawContent"
      style="height: 220px;"
      class="w-full mt-2 py-2 border border-solid border-theme-divider rounded bg-white"
      :isFormat="false"
      :language="language"
      @change="editorChange" />
    <div v-else-if="contentTypeValue === STREAM_TYPE" class="mt-2 flex items-center">
      <Upload
        :disabled="!!fileName"
        :showUploadList="false"
        :customRequest="customRequest"
        :multiple="false">
        <Button
          :loading="gziping"
          :disabled="!!fileName"
          size="small">
          <Icon icon="icon-tuisongtongzhi" class="mr-1" />
          <span>上传文件</span>
        </Button>
      </Upload>
      <template v-if="!!fileName">
        <a
          class="ml-3"
          :download="fileName"
          :href="downloadUrl">{{ fileName }}</a>
        <Icon
          icon="icon-shanchuguanbi"
          class="ml-1.5 cursor-pointer text-theme-text-hover"
          @click="removeFile" />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, IconRequired, notification, SelectInput, Tooltip } from '@xcan-angus/vue-ui';

import { ContentEncoding, DelayData, ResponseContentConfig, ResponseHeader } from './PropsType';
import DelayParameter from './delayParameter.vue';
import SelectInputForm from '@/views/mock/detail/mockApis/components/contentForm/selectInputForm/index.vue';
import { FunctionsButton } from '@xcan-angus/vue-ui';

interface Props {
  value:ResponseContentConfig;
  notify:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const CodeEditor = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/codeEditor/index.vue'));
const ImportFileModal = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/contentForm/importFileModal/index.vue'));

const MAX_HEADER_NUM = 200;// 最大支持的响应头数量

const pureInputRef = ref();
const delayRef = ref();
const codeEditorRef = ref();

const httpStatus = ref<string>('200');
const headerIds = ref<string[]>([]);
const responseContent = ref('');
const contentType = ref('');
const contentEncoding = ref<ContentEncoding>();
const headerParameters = ref<ResponseHeader[]>([]);
const httpStatusError = ref(false);
const importFileVisible = ref(false);
const codeEditorReadonly = ref(false);

const httpStatusChange = (value:string) => {
  httpStatus.value = value;
  httpStatusError.value = false;
};

const addResponseHeader = () => {
  if (typeof pureInputRef.value?.add === 'function') {
    if (headerIds.value.length >= MAX_HEADER_NUM) {
      notification.error(`最大支持 ${MAX_HEADER_NUM} 个响应头`);
      return;
    }

    pureInputRef.value.add();
  }
};

const pureFormInputChange = (ids:string[]) => {
  headerIds.value = ids;
};

const insertFile = () => {
  importFileVisible.value = true;
};

const importFileOk = (value:string, file:File, encoding:ContentEncoding) => {
  const fileType = file.type;
  pureInputRef.value.add({ name: 'Content-Type', value: fileType, disabled: true, prevValue: contentType.value });
  responseContent.value = value;
  contentEncoding.value = encoding;
  codeEditorReadonly.value = true;
  contentType.value = fileType;
};

const codeEditorClear = () => {
  pureInputRef.value.del({ name: 'Content-Type', prevValue: contentType.value });
  contentType.value = '';
  contentEncoding.value = undefined;
  codeEditorReadonly.value = false;
};

const addHeaderDisabled = computed(() => {
  return headerIds.value.length >= MAX_HEADER_NUM;
});

const reset = () => {
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

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    const { contentEncoding: encoding, headers, status, content = '' } = newValue;
    if (headers?.length) {
      for (let i = 0, len = headers.length; i < len; i++) {
        const data = { ...headers[i], disabled: false };
        if (data.name === 'Content-Type') {
          contentType.value = data.value;
          if (encoding && ['base64', 'gzip_base64'].includes(encoding)) {
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

defineExpose({
  getData: ():ResponseContentConfig => {
    let content = '';
    if (typeof codeEditorRef.value?.getData === 'function') {
      content = codeEditorRef.value.getData();
    }

    let headers:ResponseHeader[] = [];
    if (typeof pureInputRef.value?.getData === 'function') {
      headers = pureInputRef.value.getData();
    }

    let delay:DelayData = {
      mode: 'NONE',
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
  isValid: ():boolean => {
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

const inputFieldNames = { label: 'name', value: 'value' };
const fieldNames = { label: '_value', value: 'id' };
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
          <span>状态码</span>
        </div>
        <div class="flex items-center h-7">
          <span>响应延时</span>
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
          @change="httpStatusChange">
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
        :disabled="addHeaderDisabled"
        @click="addResponseHeader">
        <Icon icon="icon-jia" class="mr-1" />
        <span>响应头</span>
      </Button>
      <Tooltip>
        <template #title>最大支持{{ MAX_HEADER_NUM }}个响应头</template>
        <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5 ml-1" />
      </Tooltip>
    </div>
    <SelectInputForm
      ref="pureInputRef"
      enumKey="HttpResponseHeader"
      :fielaNames="inputFieldNames"
      :value="headerParameters"
      :notify="props.notify"
      class="mt-2"
      @change="pureFormInputChange" />
    <CodeEditor
      ref="codeEditorRef"
      :notify="props.notify"
      :value="responseContent"
      :readonly="codeEditorReadonly"
      class="mt-4.5"
      @clear="codeEditorClear">
      <template #leftextra><div>响应体</div></template>
      <template #rightextra>
        <FunctionsButton>
          <Button
            style="padding: 0;"
            type="link"
            size="small">
            Mock函数助手
          </Button>
        </FunctionsButton>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="insertFile">
          <span>插入文件</span>
        </Button>
      </template>
    </CodeEditor>
    <AsyncComponent :visible="importFileVisible">
      <ImportFileModal v-model:visible="importFileVisible" @ok="importFileOk" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
.grid-container > :deep(div:first-child) > div:first-child > div:first-child {
  padding-left: 8px;
}
</style>

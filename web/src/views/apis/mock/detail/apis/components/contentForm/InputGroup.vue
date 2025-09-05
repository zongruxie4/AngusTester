<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

import { ContentType, ParametersType } from './PropsType';
import PureFormInput from '@/views/apis/mock/detail/apis/components/pureFormInput/index.vue';
import SelectInputForm from '@/views/apis/mock/detail/apis/components/contentForm/selectInputForm/index.vue';

const { t } = useI18n();

interface Props {
  value?: ParametersType[] | undefined;
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const queryFormRef = ref();
const headerFormRef = ref();
const cookieFormRef = ref();

const addQueryHandler = () => {
  if (typeof queryFormRef.value?.add === 'function') {
    queryFormRef.value.add();
  }
};

const addHeaderHandler = () => {
  if (typeof headerFormRef.value?.add === 'function') {
    headerFormRef.value.add();
  }
};

const addCookieHandler = () => {
  if (typeof cookieFormRef.value?.add === 'function') {
    cookieFormRef.value.add();
  }
};

const queryParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'query') || [];
});

const contentTypeList: ContentType[] = ['application/x-www-form-urlencode', 'multipart/form-data', 'application/json', 'text/html', 'application/xml', 'application/javascript', 'text/plain', '*/*'];
const headerParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'header')?.map(item => {
    return {
      ...item,
      disabled: item.name === 'Content-Type' && contentTypeList.includes(item.value)
    };
  }) || [];
});

const cookieParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'cookie') || [];
});

defineExpose({
  addHeader: (data: { value: string; name: string }) => {
    headerFormRef.value.add(data);
  },
  delHeader: (data: { value: string; name: string }) => {
    headerFormRef.value.del(data);
  },
  getData: (): ParametersType[] => {
    const parameters: ParametersType[] = [];
    if (typeof queryFormRef.value?.getData === 'function') {
      const data = queryFormRef.value.getData();
      if (data?.length) {
        data.forEach(item => {
          item.in = 'query';
        });
        parameters.push(...data);
      }
    }

    if (typeof headerFormRef.value?.getData === 'function') {
      const data = headerFormRef.value.getData();
      if (data?.length) {
        data.forEach(item => {
          item.in = 'header';
        });
        parameters.push(...data);
      }
    }

    if (typeof cookieFormRef.value?.getData === 'function') {
      const data = cookieFormRef.value.getData();
      if (data?.length) {
        data.forEach(item => {
          item.in = 'cookie';
        });
        parameters.push(...data);
      }
    }

    return parameters;
  },
  isValid: (): boolean => {
    let errorNum = 0;
    if (typeof queryFormRef.value?.isValid === 'function') {
      if (!queryFormRef.value.isValid()) {
        errorNum++;
      }
    }

    if (typeof headerFormRef.value?.isValid === 'function') {
      if (!headerFormRef.value.isValid()) {
        errorNum++;
      }
    }

    if (typeof cookieFormRef.value?.isValid === 'function') {
      if (!cookieFormRef.value.isValid()) {
        errorNum++;
      }
    }

    return !errorNum;
  }
});

const fielaNames = { label: 'name', value: 'value' };
</script>
<template>
  <div>
    <div class="flex items-center space-x-2">
      <Button
        type="default"
        size="small"
        @click="addQueryHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('mock.mockApisComp.contentForm.inputGroup.queryParameters') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="addHeaderHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('mock.mockApisComp.contentForm.inputGroup.requestHeaders') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="addCookieHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>Cookie</span>
        </div>
      </Button>
    </div>

    <PureFormInput
      ref="queryFormRef"
      :label="t('mock.mockApisComp.contentForm.inputGroup.queryParameters')"
      class="mt-4"
      :fielaNames="fielaNames"
      :value="queryParameters"
      :notify="props.notify" />

    <SelectInputForm
      ref="headerFormRef"
      :label="t('mock.mockApisComp.contentForm.inputGroup.requestHeaders')"
      class="mt-4"
      enumKey="HttpRequestHeader"
      :fielaNames="fielaNames"
      :value="headerParameters"
      :notify="props.notify" />

    <PureFormInput
      ref="cookieFormRef"
      label="Cookie"
      class="mt-4"
      valueRequired
      :fielaNames="fielaNames"
      :value="cookieParameters"
      :notify="props.notify" />
  </div>
</template>
<style scoped>
.error.ant-input-affix-wrapper {
  border-width: 1px;
  border-style: solid;
}
</style>

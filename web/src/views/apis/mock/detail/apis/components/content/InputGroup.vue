<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { HttpRequestHeader } from '@xcan-angus/infra';

import { ContentType, ParametersType } from './types';
import PureFormInput from '@/views/apis/mock/detail/apis/components/PureFormInput.vue';
import SelectInputForm from '@/views/apis/mock/detail/apis/components/content/SelectInputForm.vue';
import { HTTP_HEADERS } from '@/utils/constant';

/**
 * <p>Props interface for InputGroup component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value?: ParametersType[] | undefined;
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

// ==================== Template Refs ====================
const queryFormRef = ref();
const headerFormRef = ref();
const cookieFormRef = ref();

// ==================== Event Handlers ====================
/**
 * <p>Handles add query parameter button click</p>
 * <p>Adds a new query parameter field to the form</p>
 */
const handleAddQuery = () => {
  if (typeof queryFormRef.value?.add === 'function') {
    queryFormRef.value.add();
  }
};

/**
 * <p>Handles add header parameter button click</p>
 * <p>Adds a new header parameter field to the form</p>
 */
const handleAddHeader = () => {
  if (typeof headerFormRef.value?.add === 'function') {
    headerFormRef.value.add();
  }
};

/**
 * <p>Handles add cookie parameter button click</p>
 * <p>Adds a new cookie parameter field to the form</p>
 */
const handleAddCookie = () => {
  if (typeof cookieFormRef.value?.add === 'function') {
    cookieFormRef.value.add();
  }
};

// ==================== Computed Properties ====================
/**
 * <p>Query parameters filtered from props value</p>
 * <p>Returns only parameters with 'query' type</p>
 */
const queryParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'query') || [];
});

/**
 * <p>Supported content types for header validation</p>
 * <p>Used to determine if Content-Type header should be disabled</p>
 */
const contentTypeList: ContentType[] = [
  'application/x-www-form-urlencode',
  'multipart/form-data',
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

/**
 * <p>Header parameters filtered from props value</p>
 * <p>Returns only parameters with 'header' type and applies disabled state for Content-Type</p>
 */
const headerParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'header')?.map(item => {
    return {
      ...item,
      disabled: item.name === HTTP_HEADERS.CONTENT_TYPE && contentTypeList.includes(item.value as ContentType)
    };
  }) || [];
});

/**
 * <p>Cookie parameters filtered from props value</p>
 * <p>Returns only parameters with 'cookie' type</p>
 */
const cookieParameters = computed<ParametersType[]>((): ParametersType[] => {
  return props.value?.filter((item) => item.in === 'cookie') || [];
});

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides methods for managing parameters and validation</p>
 */
defineExpose({
  /**
   * <p>Adds a header parameter to the form</p>
   * <p>Programmatically adds a header with specified data</p>
   *
   * @param data - Header parameter data with name and value
   */
  addHeader: (data: { value: string; name: string }) => {
    headerFormRef.value.add(data);
  },

  /**
   * <p>Removes a header parameter from the form</p>
   * <p>Programmatically removes a header with specified data</p>
   *
   * @param data - Header parameter data to remove
   */
  delHeader: (data: { value: string; name: string }) => {
    headerFormRef.value.del(data);
  },

  /**
   * <p>Gets all parameter data from all form types</p>
   * <p>Collects data from query, header, and cookie forms</p>
   *
   * @returns Array of all parameters with their types
   */
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

  /**
   * <p>Validates all form components</p>
   * <p>Checks if all query, header, and cookie forms are valid</p>
   *
   * @returns true if all forms are valid, false otherwise
   */
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

// ==================== Configuration ====================
/**
 * <p>Field name mapping for form components</p>
 * <p>Defines how form fields map to data properties</p>
 */
const fielaNames = { label: 'name', value: 'value' };
</script>
<template>
  <div>
    <div class="flex items-center space-x-2">
      <Button
        type="default"
        size="small"
        @click="handleAddQuery">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('protocol.queryParameter') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="handleAddHeader">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('protocol.requestHeader') }}</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="handleAddCookie">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('protocol.cookie') }}</span>
        </div>
      </Button>
    </div>

    <PureFormInput
      ref="queryFormRef"
      :label="t('protocol.queryParameter')"
      class="mt-4"
      :fielaNames="fielaNames"
      :value="queryParameters.map(item => ({ name: item.name, value: item.value }))"
      :notify="props.notify" />

    <SelectInputForm
      ref="headerFormRef"
      :label="t('protocol.requestHeader')"
      class="mt-4"
      :enumKey="HttpRequestHeader"
      :fielaNames="fielaNames"
      :value="headerParameters"
      :notify="props.notify" />

    <PureFormInput
      ref="cookieFormRef"
      :label="t('protocol.cookie')"
      class="mt-4"
      valueRequired
      :fielaNames="fielaNames"
      :value="cookieParameters.map(item => ({ name: item.name, value: item.value }))"
      :notify="props.notify" />
  </div>
</template>
<style scoped>
.error.ant-input-affix-wrapper {
  border-width: 1px;
  border-style: solid;
}
</style>

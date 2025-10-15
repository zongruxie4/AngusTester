<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, notification } from '@xcan-angus/vue-ui';
import { FullMatchCondition, ParameterIn } from '@xcan-angus/infra';

import { ResponseMatchConfig } from './types';
import PathForm from './PathForm.vue';
import ParameterForm from './ParameterForm.vue';
import SelectInputForm from './SelectInputForm.vue';
import RequestBody from './RequestBody.vue';

/**
 * <p>Props interface for MatchConfig component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: ResponseMatchConfig;
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

/**
 * <p>Maximum number of parameters allowed for matching</p>
 * <p>Prevents excessive parameter configuration</p>
 */
const MAX_PARAMETERS_NUM = 20;

const pathFormRef = ref();
const queryFormRef = ref();
const headerFormRef = ref();
const requestBodyRef = ref();

/**
 * <p>List of query parameter IDs for tracking</p>
 * <p>Used to manage query form state</p>
 */
const queryIds = ref<string[]>([]);

/**
 * <p>List of header parameter IDs for tracking</p>
 * <p>Used to manage header form state</p>
 */
const headerIds = ref<string[]>([]);

/**
 * <p>Handles add path button click</p>
 * <p>Adds a new path matching condition</p>
 */
const handleAddPath = () => {
  if (typeof pathFormRef.value?.add === 'function') {
    pathFormRef.value.add();
  }
};

/**
 * <p>Handles add query parameter button click</p>
 * <p>Adds a new query parameter if under the maximum limit</p>
 */
const handleAddQuery = () => {
  if (!canAddParameters()) {
    return;
  }

  if (typeof queryFormRef.value?.add === 'function') {
    queryFormRef.value.add();
  }
};

/**
 * <p>Handles add header parameter button click</p>
 * <p>Adds a new header parameter if under the maximum limit</p>
 */
const handleAddHeader = () => {
  if (!canAddParameters()) {
    return;
  }

  if (typeof headerFormRef.value?.add === 'function') {
    headerFormRef.value.add();
  }
};

/**
 * <p>Handles add request body button click</p>
 * <p>Adds a new request body matching condition</p>
 */
const handleAddRequestBody = () => {
  if (typeof requestBodyRef.value?.add === 'function') {
    requestBodyRef.value.add();
  }
};

/**
 * <p>Handles query form change event</p>
 * <p>Updates the list of query parameter IDs</p>
 *
 * @param ids - Array of query parameter IDs
 */
const handleQueryFormChange = (ids: string[]) => {
  queryIds.value = ids;
};

/**
 * <p>Handles header form change event</p>
 * <p>Updates the list of header parameter IDs</p>
 *
 * @param ids - Array of header parameter IDs
 */
const handleHeaderFormChange = (ids: string[]) => {
  headerIds.value = ids;
};

/**
 * <p>Checks if more parameters can be added</p>
 * <p>Validates against maximum parameter limit</p>
 *
 * @returns true if parameters can be added, false otherwise
 */
const canAddParameters = (): boolean => {
  if (totalIdsNum.value >= MAX_PARAMETERS_NUM) {
    notification.warning(t('mock.detail.apis.components.match.maxParametersTip', { max: MAX_PARAMETERS_NUM }));
    return false;
  }

  return true;
};

/**
 * <p>Total number of parameter IDs across all forms</p>
 * <p>Calculates the sum of query and header parameter counts</p>
 */
const totalIdsNum = computed(() => {
  let num = 0;
  if (queryIds.value?.length) {
    num += queryIds.value.length;
  }

  if (headerIds.value?.length) {
    num += headerIds.value.length;
  }

  return num;
});

/**
 * <p>All parameters from props value</p>
 * <p>Returns the complete parameters array</p>
 */
const parameters = computed(() => {
  return props.value?.parameters;
});

/**
 * <p>Header parameters filtered from props value</p>
 * <p>Returns only parameters with 'header' type</p>
 */
const headerParameters = computed(() => {
  return parameters.value?.filter((item) => item.in === ParameterIn.header);
});

/**
 * <p>Query parameters filtered from props value</p>
 * <p>Returns only parameters with 'query' type</p>
 */
const queryParameters = computed(() => {
  return parameters.value?.filter((item) => item.in === ParameterIn.query);
});

/**
 * <p>Path matching configuration from props value</p>
 * <p>Returns the path matching condition</p>
 */
const path = computed(() => {
  return props.value?.path;
});

/**
 * <p>Request body matching configuration from props value</p>
 * <p>Returns the body matching condition</p>
 */
const body = computed(() => {
  return props.value?.body;
});

/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides getData and isValid methods for external access</p>
 */
defineExpose({
  /**
   * <p>Gets the current match configuration data</p>
   * <p>Collects data from all child components and returns complete config</p>
   *
   * @returns ResponseMatchConfig object with current configuration
   */
  getData: () => {
    let path: {
      condition: string;
      expected: string | undefined;
      expression: string | undefined;
    } | undefined;
    if (typeof pathFormRef.value?.getData === 'function') {
      path = pathFormRef.value.getData();
    }

    let body: {
      condition: string;
      expected: string | undefined;
      expression: string | undefined;
    } | undefined;
    if (typeof requestBodyRef.value?.getData === 'function') {
      body = requestBodyRef.value.getData();
    }

    let parameters: {
      condition: FullMatchCondition;
      expected: string | undefined;
      expression: string | undefined;
      in: ParameterIn.header | ParameterIn.query;
      name: string;
    }[] | undefined = [];
    if (typeof queryFormRef.value?.getData === 'function') {
      const _parameters = queryFormRef.value.getData();
      if (_parameters?.length) {
        parameters.push(..._parameters);
      }
    }

    if (typeof headerFormRef.value?.getData === 'function') {
      const _parameters = headerFormRef.value.getData();
      if (_parameters?.length) {
        parameters.push(..._parameters);
      }
    }

    parameters = parameters.length ? parameters : undefined;
    return {
      body,
      path,
      parameters
    };
  },

  /**
   * <p>Validates the current form state</p>
   * <p>Checks if all child components are valid</p>
   *
   * @returns true if all forms are valid, false otherwise
   */
  isValid: () => {
    let errorNum = 0;
    if (typeof pathFormRef.value?.isValid === 'function') {
      const validFlag = pathFormRef.value.isValid();
      if (!validFlag) {
        errorNum++;
      }
    }

    if (typeof queryFormRef.value?.isValid === 'function') {
      const validFlag = queryFormRef.value.isValid();
      if (!validFlag) {
        errorNum++;
      }
    }

    if (typeof headerFormRef.value?.isValid === 'function') {
      const validFlag = headerFormRef.value.isValid();
      if (!validFlag) {
        errorNum++;
      }
    }

    if (typeof requestBodyRef.value?.isValid === 'function') {
      const validFlag = requestBodyRef.value.isValid();
      if (!validFlag) {
        errorNum++;
      }
    }
    return !errorNum;
  }
});
</script>
<template>
  <div>
    <div class="flex items-center space-x-2">
      <Button
        type="default"
        size="small"
        @click="handleAddPath">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('common.path') }}</span>
        </div>
      </Button>
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
        @click="handleAddRequestBody">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>{{ t('protocol.requestBody') }}</span>
        </div>
      </Button>
    </div>
    <PathForm
      ref="pathFormRef"
      key="path"
      class="mt-4"
      :value="path"
      :notify="props.notify" />
    <ParameterForm
      ref="queryFormRef"
      key="query"
      :in="ParameterIn.query"
      class="mt-4"
      :value="queryParameters as any"
      :notify="props.notify"
      @change="handleQueryFormChange" />
    <SelectInputForm
      ref="headerFormRef"
      key="header"
      in="header"
      class="mt-4"
      :value="headerParameters"
      :notify="props.notify"
      @change="handleHeaderFormChange" />
    <RequestBody
      ref="requestBodyRef"
      key="body"
      class="mt-4"
      :value="body as any"
      :notify="props.notify" />
  </div>
</template>

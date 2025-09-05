<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, notification } from '@xcan-angus/vue-ui';

import { FullMatchCondition, ResponseMatchConfig } from './types';
import PathForm from './PathForm.vue';
import ParameterForm from './ParameterForm.vue';
import SelectInputForm from './SelectInputForm.vue';
import RequestBody from './RequestBody.vue';

interface Props {
  value:ResponseMatchConfig;
  notify:number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const MAX_PARAMETERS_NUM = 20;// 限制最大匹配参数数量

const pathFormRef = ref();
const queryFormRef = ref();
const headerFormRef = ref();
const requestBodyRef = ref();

const queryIds = ref<string[]>([]);
const headerIds = ref<string[]>([]);

const canAdd = ():boolean => {
  if (totalIdsNum.value >= MAX_PARAMETERS_NUM) {
    notification.warning(`匹配参数限制最大 ${MAX_PARAMETERS_NUM} 个`);
    return false;
  }

  return true;
};

const addPathHandler = () => {
  if (typeof pathFormRef.value?.add === 'function') {
    pathFormRef.value.add();
  }
};

const addQueryHandler = () => {
  if (!canAdd()) {
    return;
  }

  if (typeof queryFormRef.value?.add === 'function') {
    queryFormRef.value.add();
  }
};

const addHeaderHandler = () => {
  if (!canAdd()) {
    return;
  }

  if (typeof headerFormRef.value?.add === 'function') {
    headerFormRef.value.add();
  }
};

const addRequestBodyHandler = () => {
  if (typeof requestBodyRef.value?.add === 'function') {
    requestBodyRef.value.add();
  }
};

const queryFormChange = (ids:string[]) => {
  queryIds.value = ids;
};

const headerFormChange = (ids:string[]) => {
  headerIds.value = ids;
};

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

const parameters = computed(() => {
  return props.value?.parameters;
});

const headerParameters = computed(() => {
  return parameters.value?.filter((item) => item.in === 'header');
});

const queryParameters = computed(() => {
  return parameters.value?.filter((item) => item.in === 'query');
});

const path = computed(() => {
  return props.value?.path;
});

const body = computed(() => {
  return props.value?.body;
});

defineExpose({
  getData: () => {
    let path: {
      condition: string;
      expected: string | undefined;
      expression: string | undefined;
    }|undefined;
    if (typeof pathFormRef.value?.getData === 'function') {
      path = pathFormRef.value.getData();
    }

    let body: {
      condition: string;
      expected: string | undefined;
      expression: string | undefined;
    }|undefined;
    if (typeof requestBodyRef.value?.getData === 'function') {
      body = requestBodyRef.value.getData();
    }

    let parameters: {
      condition: FullMatchCondition;
      expected: string | undefined;
      expression: string | undefined;
      in: 'header' | 'query';
      name: string;
    }[]|undefined = [];
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
        @click="addPathHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>路径</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="addQueryHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>查询参数</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="addHeaderHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>请求头</span>
        </div>
      </Button>
      <Button
        type="default"
        size="small"
        @click="addRequestBodyHandler">
        <div class="flex items-center">
          <Icon icon="icon-jia" class="mr-1" /><span>请求体</span>
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
      in="query"
      class="mt-4"
      :value="queryParameters"
      :notify="props.notify"
      @change="queryFormChange" />
    <SelectInputForm
      ref="headerFormRef"
      key="header"
      in="header"
      class="mt-4"
      :value="headerParameters"
      :notify="props.notify"
      @change="headerFormChange" />
    <RequestBody
      ref="requestBodyRef"
      key="body"
      class="mt-4"
      :value="body"
      :notify="props.notify" />
  </div>
</template>

<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Input, Modal, notification, Select, SelectEnum } from '@xcan-angus/vue-ui';
import { SelectApisTable } from 'angus-design';
import { RadioGroup } from 'ant-design-vue';
import { TESTER, http } from '@xcan-angus/tools';
import qs from 'qs';

interface Props {
  visible: boolean;
  serviceId: string;
  type: 'batchAddParams' | 'batchModifyParams' | 'batchDelParams'| 'batchEnabledParams'| 'batchDisabledParams'| 'batchModifyAuth' | 'batchModifyServer' | 'batchLinkVariable' | 'batchDelVariable' | 'batchLinkDataSet'| 'batchDelDataSet',
  title: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: '',
  title: '',
  projectId: '',
  type: 'batchAddParams'
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

const addOrModifyParamsRef = ref();
const enableOrDelParamsRef = ref();
const modifyAuthRef = ref();
const modifyServerRef = ref();
const refVariableRef = ref();
const refDatasetRef = ref();

const modifyScopeOpt = ref([{
  value: 'ALL',
  label: '全部接口'
}, {
  value: 'SELECTED_APIS',
  label: '指定接口'
}, {
  value: 'MATCH_APIS',
  label: '匹配接口'
}]);

const scope = ref('ALL');
const selectedApisIds = ref([]);
const matchMethod = ref();
const matchEndpointRegex = ref();
const filterTags = ref([]);

const AddOrModifyParams = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/addOrModifyParams/index.vue'));
const EnabledOrdDelParams = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/enableOrDelParams/index.vue'));
const ModifyAuth = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/modifyAuth/index.vue'));
const ModifyServer = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/modifyServer/index.vue'));
const RefVariable = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/refVariable/index.vue'));
const RefDataset = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/refDataset/index.vue'));

const handleChangeSelectApis = (ids: string[]) => {
  selectedApisIds.value = ids || [];
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (newValue) {
      scope.value = 'ALL';
      selectedApisIds.value = [];
      matchMethod.value = undefined;
      matchEndpointRegex.value = undefined;
    }
  }, {
    immediate: true
  });
});

const apiFielsNames = {
  value: 'id',
  label: 'summary'
};

const cancel = () => {
  emits('update:visible', false);
};

const ok = () => {
  if (scope.value === 'MATCH_APIS') {
    if (!matchMethod.value && !matchEndpointRegex.value && !filterTags.value?.length) {
      notification.error('请至少选择一种匹配方式');
      return;
    }
  }
  if (scope.value === 'SELECTED_APIS') {
    if (!selectedApisIds.value) {
      notification.error('请至少选择一个接口');
      return;
    }
  }
  switch (props.type) {
    case 'batchAddParams':
      addParams();
      break;
    case 'batchModifyParams':
      modifyParams();
      break;
    case 'batchDelParams':
      delParams();
      break;
    case 'batchEnabledParams':
      enabledParams();
      break;
    case 'batchDisabledParams':
      disabledParams();
      break;
    case 'batchModifyAuth':
      modifyAuth();
      break;
    case 'batchModifyServer':
      modifyServer();
      break;
    case 'batchLinkVariable':
      referenceVariable();
      break;
    case 'batchLinkDataSet':
      referenceDataset();
      break;
    case 'batchDelVariable':
      delRefrenceVariable();
      break;
    case 'batchDelDataSet':
      delRefrenceDataset();
      break;
  }
};

const getConfig = () => {
  let queryParametersMap = {};
  if (scope.value === 'MATCH_APIS') {
    queryParametersMap = {
      matchMethod: matchMethod.value,
      matchEndpointRegex: matchEndpointRegex.value,
      scope: scope.value,
      filterTags: filterTags.value
    };
  } else if (scope.value === 'SELECTED_APIS') {
    queryParametersMap = {
      scope: scope.value,
      selectedApisIds: selectedApisIds.value
    };
  } else {
    queryParametersMap = {
      scope: scope.value
    };
  }
  return qs.stringify(queryParametersMap, { indices: false });
};

const addParams = async () => {
  const config = getConfig();
  const parameters = addOrModifyParamsRef.value.getData();
  if (!parameters.length) {
    cancel();
    return;
  }
  const [error] = await http.put(`${TESTER}/services/${props.serviceId}/apis/parameter?${config}`, parameters);
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const modifyParams = async () => {
  const config = getConfig();
  const parameters = addOrModifyParamsRef.value.getData();
  if (!parameters.length) {
    cancel();
    return;
  }
  const [error] = await http.patch(`${TESTER}/services/${props.serviceId}/apis/parameter?${config}`, parameters);
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const delParams = async () => {
  const config = getConfig();
  const parameters = enableOrDelParamsRef.value.getData();
  if (!parameters.length) {
    cancel();
    return;
  }
  const [error] = await http.del(`${TESTER}/services/${props.serviceId}/apis/parameter?${config}`, {
    names: parameters
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const enabledParams = async () => {
  const config = getConfig();
  const parameters = enableOrDelParamsRef.value.getData();
  if (!parameters.length) {
    cancel();
    return;
  }
  const [error] = await http.patch(`${TESTER}/services/${props.serviceId}/apis/parameter/enabled?${config}`, {
    enabled: true,
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const disabledParams = async () => {
  const config = getConfig();
  const parameters = enableOrDelParamsRef.value.getData();
  if (!parameters.length) {
    cancel();
    return;
  }
  const [error] = await http.patch(`${TESTER}/services/${props.serviceId}/apis/parameter/enabled?${config}`, {
    enabled: false,
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const modifyAuth = async () => {
  const config = getConfig();
  const parameters = modifyAuthRef.value.getData();
  const [error] = await http.patch(`${TESTER}/services/${props.serviceId}/apis/authentication?${config}`, parameters);
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const modifyServer = async () => {
  const config = getConfig();
  const parameters = modifyServerRef.value.getData();
  if (!parameters) {
    return;
  }
  const [error] = await http.patch(`${TESTER}/services/${props.serviceId}/apis/server?${config}`, parameters);
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const referenceVariable = async () => {
  const config = getConfig();
  const parameters = refVariableRef.value.getData();
  if (!parameters.length) {
    notification.error('请至少选择一个变量');
    return;
  }
  const [error] = await http.put(`${TESTER}/services/${props.serviceId}/apis/variable/reference?${config}`, {
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const referenceDataset = async () => {
  const config = getConfig();
  const parameters = refDatasetRef.value.getData();
  if (!parameters.length) {
    notification.error('请至少选择一个数据集');
    return;
  }
  const [error] = await http.put(`${TESTER}/services/${props.serviceId}/apis/dataset/reference?${config}`, {
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const delRefrenceVariable = async () => {
  const config = getConfig();
  const parameters = refVariableRef.value.getData();
  if (!parameters.length) {
    notification.error('请至少选择一个变量');
    return;
  }
  const [error] = await http.del(`${TESTER}/services/${props.serviceId}/apis/variable/reference?${config}`, {
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const delRefrenceDataset = async () => {
  const config = getConfig();
  const parameters = refDatasetRef.value.getData();
  if (!parameters.length) {
    notification.error('请至少选择一个数据集');
    return;
  }
  const [error] = await http.del(`${TESTER}/services/${props.serviceId}/apis/dataset/reference?${config}`, {
    names: parameters
  }, {
    paramsType: true
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

</script>
<template>
  <Modal
    :title="props.title || '批量修改'"
    :width="1000"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center">
      <label class="w-20">接口范围</label>
      <div>
        <RadioGroup
          v-model:value="scope"
          :options="modifyScopeOpt">
        </RadioGroup>
      </div>
    </div>

    <div v-if="scope === 'SELECTED_APIS'" class="flex mt-3">
      <label class="w-20">选择接口</label>
      <div class="flex-1">
        <div class="inline-flex items-center space-x-2">
          <SelectApisTable
            :serviceId="props.serviceId"
            :projectId="props.projectId"
            :scrollHeight="300"
            @change="handleChangeSelectApis" />
          <!-- <Select
            v-model:value="selectedApisIds"
            class="w-100 select-api"
            :action="`${TESTER}/apis/search?serviceId=${props.serviceId}&projectId=${props.projectId}`"
            :field-names="apiFielsNames"
            showSearch
            mode="multiple">
          </Select>
          <span>已选择{{ selectedApisIds.length }} 个接口</span> -->
        </div>
        <!-- <Tag v-for="api in selectedApisIds" :key="api.id">{{ api.introduce }}</Tag> -->
      </div>
    </div>

    <div v-if="scope === 'MATCH_APIS'" class="flex mt-3 items-center">
      <div class="inline-flex items-center">
        <label class="w-20">请求方法</label>
        <div class="flex-1 w-30">
          <SelectEnum
            v-model:value="matchMethod"
            class="method-select w-23"
            :allowClear="true"
            size="small"
            enumKey="HttpMethod" />
        </div>
      </div>

      <div class="inline-flex ml-10 items-center">
        <label class="w-24">匹配路径表达式</label>
        <div class="flex-1 w-60">
          <Input
            v-model:value="matchEndpointRegex"
            placeholder="请输入匹配正则表达式，如：.*?/search.*" />
        </div>
      </div>

      <div class="inline-flex ml-10 items-center">
        <label class="w-20">选择标签</label>
        <div class="flex-1 w-60">
          <Select
            v-model:value="filterTags"
            placeholder="选择匹配标签"
            :action="`${TESTER}/services/${props.serviceId}/schema/tag`"
            :fieldNames="{
              value: 'name',
              label: 'name'
            }"
            class="w-50"
            mode="multiple"
            :allowClear="true"
            size="small" />
        </div>
      </div>
    </div>

    <div v-if="['batchAddParams', 'batchModifyParams', ].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchAddParams'">添加参数</div>
      <div v-if="props.type === 'batchModifyParams'">修改参数</div>
      <div class="border px-2 pb-3 rounded">
        <AddOrModifyParams ref="addOrModifyParamsRef" />
      </div>
    </div>

    <div v-if="['batchDelParams', 'batchEnabledParams', 'batchDisabledParams'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchDelParams'">删除参数</div>
      <div v-if="props.type === 'batchEnabledParams'">启用参数</div>
      <div v-if="props.type === 'batchDisabledParams'">禁用参数</div>
      <EnabledOrdDelParams ref="enableOrDelParamsRef" />
    </div>

    <div v-if="props.type === 'batchModifyAuth' && props.visible" class="space-y-2 mt-3">
      <div>修改认证</div>
      <ModifyAuth ref="modifyAuthRef" />
    </div>

    <div v-if="props.type === 'batchModifyServer' && props.visible" class="space-y-2 mt-3">
      <div>修改服务器</div>
      <ModifyServer ref="modifyServerRef" />
    </div>

    <div v-if="['batchLinkVariable', 'batchDelVariable'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchLinkVariable'">引用变量</div>
      <div v-if="props.type === 'batchDelVariable'">取消引用变量</div>
      <RefVariable ref="refVariableRef" :projectId="props.projectId" />
    </div>

    <div v-if="['batchLinkDataSet', 'batchDelDataSet'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchLinkDataSet'">引用数据集</div>
      <div v-if="props.type === 'batchDelDataSet'">取消引用数据集</div>
      <RefDataset ref="refDatasetRef" :projectId="props.projectId" />
    </div>
  </Modal>
</template>
<style>
.select-api .ant-select-selection-overflow {
  max-height: 50px;
  overflow-y: auto;
}
</style>

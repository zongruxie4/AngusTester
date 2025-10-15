<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Input, Modal, notification, Select, SelectApisTable } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import qs from 'qs';
import { services } from '@/api/tester';
import SelectEnum from '@/components/enum/SelectEnum.vue';

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
const { t } = useI18n();
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void;}>();

const addOrModifyParamsRef = ref();
const enableOrDelParamsRef = ref();
const modifyAuthRef = ref();
const modifyServerRef = ref();
const refVariableRef = ref();
const refDatasetRef = ref();

const modifyScopeOpt = ref([{
  value: 'ALL',
  label: t('service.sidebar.batchModifyModal.modifyScope_all')
}, {
  value: 'SELECTED_APIS',
  label: t('service.sidebar.batchModifyModal.modifyScope_select')
}, {
  value: 'MATCH_APIS',
  label: t('service.sidebar.batchModifyModal.modifyScope_match')
}]);

const scope = ref('ALL');
const selectedApisIds = ref([]);
const matchMethod = ref();
const matchEndpointRegex = ref();
const filterTags = ref([]);

const AddOrModifyParams = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/AddOrModifyParams.vue'));
const EnabledOrdDelParams = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/EnableOrDelParams.vue'));
const ModifyAuth = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/ModifyAuth.vue'));
const ModifyServer = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/ModifyServer.vue'));
const RefVariable = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/RefVariable.vue'));
const RefDataset = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/batchModifyApi/RefDataset.vue'));

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

const cancel = () => {
  emits('update:visible', false);
};

const ok = () => {
  if (scope.value === 'MATCH_APIS') {
    if (!matchMethod.value && !matchEndpointRegex.value && !filterTags.value?.length) {
      notification.error(t('service.sidebar.batchModifyModal.matchMethodTip'));
      return;
    }
  }
  if (scope.value === 'SELECTED_APIS') {
    if (!selectedApisIds.value) {
      notification.error(t('service.sidebar.batchModifyModal.selectedApisTip'));
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
      delReferenceVariable();
      break;
    case 'batchDelDataSet':
      delReferenceDataset();
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
  const [error] = await services.batchAddParams(props.serviceId, config, parameters);
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
  const [error] = await services.batchUpdateParams(props.serviceId, config, parameters);
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
  const [error] = await services.batchDeleteParams(props.serviceId, config, {
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
  const [error] = await services.batchToggleEnabledParams(props.serviceId, config, {
    enabled: true,
    names: parameters
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
  const [error] = await services.batchToggleEnabledParams(props.serviceId, config, {
    enabled: false,
    names: parameters
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
  const [error] = await services.batchUpdateAuthentication(props.serviceId, config, parameters);
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
  const [error] = await services.batchUpdateServer(props.serviceId, config, parameters);
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
    notification.error(t('service.sidebar.batchModifyModal.referenceVariableTip'));
    return;
  }
  const [error] = await services.batchUpdateReferenceVariable(props.serviceId, config, {
    names: parameters
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
    notification.error(t('service.sidebar.batchModifyModal.referenceDatasetTip'));
    return;
  }
  const [error] = await services.batchAddReferenceDataset(props.serviceId, config, {
    names: parameters
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const delReferenceVariable = async () => {
  const config = getConfig();
  const parameters = refVariableRef.value.getData();
  if (!parameters.length) {
    notification.error(t('service.sidebar.batchModifyModal.delReferenceVariableTip'));
    return;
  }
  const [error] = await services.batchDeleteReferenceVariable(props.serviceId, config, {
    names: parameters
  });
  if (error) {
    return;
  }
  emits('ok');
  cancel();
};

const delReferenceDataset = async () => {
  const config = getConfig();
  const parameters = refDatasetRef.value.getData();
  if (!parameters.length) {
    notification.error(t('service.sidebar.batchModifyModal.delReferenceDatasetTip'));
    return;
  }
  const [error] = await services.batchDeleteReferenceDataset(props.serviceId, config, {
    names: parameters
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
    :title="props.title || t('service.sidebar.batchModifyModal.title')"
    :width="1000"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <div class="flex items-center">
      <label class="w-20">{{ t('service.sidebar.batchModifyModal.scopeLabel') }}</label>
      <div>
        <RadioGroup
          v-model:value="scope"
          :options="modifyScopeOpt">
        </RadioGroup>
      </div>
    </div>

    <div v-if="scope === 'SELECTED_APIS'" class="flex mt-3">
      <label class="w-20">{{ t('service.sidebar.batchModifyModal.apisLabel') }}</label>
      <div class="flex-1">
        <div class="inline-flex items-center space-x-2">
          <SelectApisTable
            :serviceId="props.serviceId"
            :projectId="props.projectId"
            :scrollHeight="300"
            @change="handleChangeSelectApis" />
        </div>
      </div>
    </div>

    <div v-if="scope === 'MATCH_APIS'" class="flex mt-3 items-center">
      <div class="inline-flex items-center">
        <label class="w-20">{{ t('service.sidebar.batchModifyModal.matchMethodLabel') }}</label>
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
        <label class="w-24">{{ t('service.sidebar.batchModifyModal.matchEndpointRegexLabel') }}</label>
        <div class="flex-1 w-60">
          <Input
            v-model:value="matchEndpointRegex"
            :placeholder="t('service.sidebar.batchModifyModal.matchEndpointRegexPlaceholder')" />
        </div>
      </div>

      <div class="inline-flex ml-10 items-center">
        <label class="w-20">{{ t('service.sidebar.batchModifyModal.tagsLabel') }}</label>
        <div class="flex-1 w-60">
          <Select
            v-model:value="filterTags"
            :placeholder="('service.sidebar.batchModifyModal.tagsPlaceholder')"
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
      <div v-if="props.type === 'batchAddParams'">{{ t('service.sidebar.batchModifyModal.addParams') }}</div>
      <div v-if="props.type === 'batchModifyParams'">{{ t('service.sidebar.batchModifyModal.modifyParams') }}</div>
      <div class="border px-2 pb-3 rounded">
        <AddOrModifyParams ref="addOrModifyParamsRef" />
      </div>
    </div>

    <div v-if="['batchDelParams', 'batchEnabledParams', 'batchDisabledParams'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchDelParams'">{{ t('service.sidebar.batchModifyModal.deleteParams') }}</div>
      <div v-if="props.type === 'batchEnabledParams'">{{ t('service.sidebar.batchModifyModal.enabledParams') }}</div>
      <div v-if="props.type === 'batchDisabledParams'">{{ t('service.sidebar.batchModifyModal.disabledParams') }}</div>
      <EnabledOrdDelParams ref="enableOrDelParamsRef" />
    </div>

    <div v-if="props.type === 'batchModifyAuth' && props.visible" class="space-y-2 mt-3">
      <div>{{ t('service.sidebar.batchModifyModal.modifyAuth') }}</div>
      <ModifyAuth ref="modifyAuthRef" />
    </div>

    <div v-if="props.type === 'batchModifyServer' && props.visible" class="space-y-2 mt-3">
      <div>{{ t('service.sidebar.batchModifyModal.modifyServer') }}</div>
      <ModifyServer ref="modifyServerRef" />
    </div>

    <div v-if="['batchLinkVariable', 'batchDelVariable'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchLinkVariable'">{{ t('service.sidebar.batchModifyModal.linkVariable') }}</div>
      <div v-if="props.type === 'batchDelVariable'">{{ t('service.sidebar.batchModifyModal.unlinkVariable') }}</div>
      <RefVariable ref="refVariableRef" :projectId="props.projectId" />
    </div>

    <div v-if="['batchLinkDataSet', 'batchDelDataSet'].includes(props.type) && props.visible" class="space-y-2 mt-3">
      <div v-if="props.type === 'batchLinkDataSet'">{{ t('service.sidebar.batchModifyModal.linkDataset') }}</div>
      <div v-if="props.type === 'batchDelDataSet'">{{ t('service.sidebar.batchModifyModal.unlinkDataset') }}</div>
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

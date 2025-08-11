<script setup lang='ts'>
import { computed, inject, onMounted, ref } from 'vue';
import { Input, notification, Select, TreeSelect } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';
import type { Rule } from 'ant-design-vue/es/form';
import { mock, services } from 'src/api/tester';
import { useI18n } from 'vue-i18n';

import ApiList from '@/views/mock/add/apiList.vue';
import HeadInfo from '@/components/layout/header/info/index.vue';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});
const { t } = useI18n();

const isPrivate = ref(false);
const serviceInfo = ref();

const createType = ref<boolean>();

const formRef = ref();
const router = useRouter();

const mockServiceInfo = ref();

const formState = ref<{
  name: string;
  serviceDomainUrl:string;
  servicePort:string;
  nodeId: string | undefined;
  serviceId: string;
  apiIds: string[];
}>({
  name: '',
  serviceDomainUrl: '',
  servicePort: '',
  nodeId: undefined,
  serviceId: '',
  apiIds: []
});

const loading = ref(false);
const loadInfo = async () => {
  loading.value = true;
  const [error, { data }] = await mock.getServiceDetail(serviceInfo.value?.mockServiceId);
  loading.value = false;
  if (error) { return; }
  mockServiceInfo.value = JSON.parse(JSON.stringify(data));
  const { name, serviceDomainUrl, servicePort, nodeId, apiIds } = data;
  formState.value.name = name;
  formState.value.serviceDomainUrl = serviceDomainUrl;
  formState.value.servicePort = servicePort;
  formState.value.nodeId = nodeId;
  formState.value.apiIds = apiIds;
};

const checkedIds = ref<string[]>([]);
const loadServiceApiIds = async () => {
  loading.value = true;
  const [error, { data }] = await mock.getServiceApiIds(serviceInfo.value?.mockServiceId);
  loading.value = false;
  if (error) { return; }
  const apiIds = data?.length ? data : [];
  formState.value.apiIds = apiIds;
  checkedIds.value = JSON.parse(JSON.stringify(apiIds));
};

const domainRegex = /^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])\.angusmock\.cloud$/;
const serviceDomainValidate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入域名'));
  } else if (!domainRegex.test(value + '.angusmock.cloud')) {
    return Promise.reject(new Error('请输入正确的域名'));
  } else {
    return Promise.resolve();
  }
};

const rules = computed(() => {
  const baseRule = {
    name: [{ required: true, message: t('service.mock.nameRule'), trigger: 'change' }],
    servicePort: [{ required: true, message: t('service.mock.servicePortRule'), trigger: 'change' }],
    nodeId: [{ required: true, message: t('service.mock.nodeRule'), trigger: 'change' }]
  };

  const privateRule = {
    ...baseRule
  };

  const publicRule:Record<string, any> = {
    ...baseRule,
    serviceDomainUrl: [{ required: true, validator: serviceDomainValidate, trigger: 'change' }]
  };

  return serviceInfo.value?.mockServiceId ? {} : isPrivate.value ? privateRule : publicRule;
});

const treeSelectChange = (id: string) => {
  formState.value.serviceId = id;
};

const handleSave = () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const addParams = {
      name: formState.value.name,
      serviceDomainUrl: !isPrivate.value ? formState.value.serviceDomainUrl + '.angusmock.cloud' : formState.value.serviceDomainUrl,
      servicePort: formState.value.servicePort,
      nodeId: formState.value.nodeId,
      serviceId: formState.value.serviceId,
      apiIds: formState.value.apiIds
    };

    const updateParams = { id: serviceInfo.value?.mockServiceId, apiIds: formState.value.apiIds };
    const [error] = serviceInfo.value?.mockServiceId ? await mock.patchService(updateParams) : await mock.addServiceByAssoc(addParams);
    loading.value = false;
    if (error) { return; }
    notification.success(serviceInfo.value?.mockServiceId ? t('tips.updateSuccess') : t('tips.addSuccess'));
    router.push('/apis#mock');
  }, () => { /** */ });
};

const loadProjectInfo = async () => {
  const [error, { data }] = await services.loadInfo(props.id);
  if (error) {
    return;
  }
  serviceInfo.value = data;
};

// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });
const handleCancel = () => {
  deleteTabPane([props.id + 'mock']);
};

onMounted(async () => {
  isPrivate.value = appContext.isPrivateEdition();
  formState.value.serviceId = props.id;
  await loadProjectInfo();
  createType.value = !serviceInfo.value?.mockServiceId;
  if (serviceInfo.value?.mockServiceId) {
    loadInfo();
    loadServiceApiIds();
  }
});
</script>
<template>
  <div class="p-4 overflow-y-auto h-full">
    <HeadInfo :text="t('service.mock.description')" />
    <Form
      ref="formRef"
      size="small"
      class="ml-3.5 mt-3.5"
      :model="formState"
      :rules="rules">
      <div class="flex">
        <div>
          <FormItem :label="t('service.mock.createTypeLabel')" required />
          <FormItem :label="t('service.mock.nameLabel')" required />
          <FormItem
            :label="t('service.mock.serviceDomainUrlLabel')"
            :required="!isPrivate"
            :class="isPrivate?'pl-2.25':''" />
          <FormItem :label="t('service.mock.servicePortLabel')" required />
          <FormItem :label="t('service.mock.nodeIdLabel')" required />
          <FormItem :label="t('service.mock.serviceLabel')" required />
          <template v-if="formState.serviceId && serviceInfo?.hasApis">
            <FormItem :label="t('service.mock.apiIdsLabel')" />
          </template>
        </div>
        <div class="w-150">
          <FormItem>
            <RadioGroup
              disabled
              :value="createType"
              class="h-6.25 mt-0.75 add-mock-type">
              <Radio :value="true">{{t('service.mock.mockNew')}}</Radio>
              <Radio :value="false">{{t('service.mock.mockUpdate')}}</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem name="name">
            <Input
              v-model:value="formState.name"
              :placeholder="t('service.mock.namePlaceholder')"
              :disabled="serviceInfo?.mockServiceId"
              :maxlength="100" />
          </FormItem>
          <FormItem name="serviceDomainUrl">
            <Input
              v-model:value="formState.serviceDomainUrl"
              :disabled="serviceInfo?.mockServiceId"
              :placeholder="t('service.mock.serviceDomainUrlPlaceholder')">
              <template v-if="!isPrivate && !serviceInfo?.mockServiceId" #addonAfter>
                <span>.angusmock.cloud</span>
              </template>
            </Input>
          </FormItem>
          <FormItem name="servicePort">
            <Input
              v-model:value="formState.servicePort"
              dataType="number"
              :placeholder="t('service.mock.servicePortPlaceholder')"
              :disabled="serviceInfo?.mockServiceId"
              :min="1"
              :max="65535" />
          </FormItem>
          <FormItem name="nodeId">
            <Select
              v-model:value="formState.nodeId"
              :disabled="serviceInfo?.mockServiceId"
              :action="`${TESTER}/node?fullTextSearch=true`"
              :fieldNames="{label:'name',value:'id'}"
              :maxlength="100"
              showSearch
              :placeholder="t('service.mock.nodePlaceholder')"
              size="small">
              <template #option="item">
                {{ `${item.name} ( ${item.ip} )` }}
              </template>
            </Select>
          </FormItem>
          <FormItem name="serviceId">
            <TreeSelect
              :action="`${TESTER}/services?fullTextSearch=true`"
              :fieldNames="{label:'name',value:'id'}"
              :defaultValue="serviceInfo"
              :virtual="false"
              size="small"
              disabled
              showSearch
              allowClear
              :placeholder="t('service.mock.servicePlaceholder')"
              @change="treeSelectChange">
              <template #title="item">
                <div class="text-3 leading-3 flex items-center h-6.5">
                  <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1 bg-blue-badge-s">S</label>
                  <div :title="item.name" class="truncate">{{ item.name }}</div>
                </div>
              </template>
            </TreeSelect>
          </FormItem>
          <template v-if="formState.serviceId && serviceInfo?.hasApis">
            <FormItem name="apiIds">
              <ApiList
                v-model:apiIds="formState.apiIds"
                :serviceId="formState.serviceId"
                :checkedIds="checkedIds" />
            </FormItem>
          </template>
          <FormItem>
            <Button size="small" @click="handleCancel">{{t('actions.confirm')}}</Button>
            <Button
              size="small"
              type="primary"
              class="ml-3"
              :loading="loading"
              @click="handleSave">
              {{t('actions.cancel')}}
            </Button>
          </FormItem>
        </div>
      </div>
    </Form>
  </div>
</template>
<style scoped>
.add-mock-type :deep(.ant-radio-disabled + span) {
  @apply text-text-content;
}
</style>

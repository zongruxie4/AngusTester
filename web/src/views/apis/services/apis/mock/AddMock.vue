<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, notification, Select, Spin, TreeSelect } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { mock, services } from '@/api/tester';
import type { Rule } from 'ant-design-vue/es/form';
import { ServicesDetail } from '@/views/apis/services/services/types';

import ApiList from '@/views/apis/mock/add/ApiList.vue';

interface Props {
  visible: boolean;
  serviceId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined
});

const { t } = useI18n();

const emits = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'reload', value:string):void;
}>();

const isPrivate = ref(false);
const formRef = ref();
const serviceDetail = ref<ServicesDetail>();

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
  serviceId: props.serviceId,
  apiIds: []
});

const loading = ref(false);

const domainRegex = /^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9\\-]{0,61}[a-z0-9])\.angusmock\.cloud$/;
const serviceDomainValidate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('service.mockService.validation.domainRequired')));
  } else if (!domainRegex.test(value + '.angusmock.cloud')) {
    return Promise.reject(new Error(t('service.mockService.validation.domainInvalid')));
  } else {
    return Promise.resolve();
  }
};

const rules = computed(() => {
  const baseRule = {
    name: [
      {
        required: true,
        message: t('service.mockService.validation.nameRequired'),
        trigger: 'change'
      }],
    servicePort: [
      {
        required: true,
        message: t('service.mockService.validation.portRequired'),
        trigger: 'change'
      }],
    nodeId: [
      {
        required: true,
        message: t('service.mockService.validation.nodeRequired'),
        trigger: 'change'
      }]
  };

  const privateRule = {
    ...baseRule
  };

  const publicRule:Record<string, any> = {
    ...baseRule,
    serviceDomainUrl: [{ required: true, validator: serviceDomainValidate, trigger: 'change' }]
  };

  return serviceDetail.value?.mockServiceId ? {} : isPrivate.value ? privateRule : publicRule;
});

const treeSelectChange = (id: string) => {
  formState.value.serviceId = id;
};

const handleSave = () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    let addParams:any = {
      name: formState.value.name,
      serviceDomainUrl: !isPrivate.value ? formState.value.serviceDomainUrl + '.angusmock.cloud' : formState.value.serviceDomainUrl,
      servicePort: formState.value.servicePort,
      nodeId: formState.value.nodeId,
      serviceId: formState.value.serviceId
    };

    if (formState.value.apiIds?.length) {
      addParams = { ...addParams, apiIds: formState.value.apiIds };
    }

    const updateParams = { id: serviceDetail.value?.mockServiceId, apiIds: formState.value.apiIds };
    const [error, { data }] = serviceDetail.value?.mockServiceId
      ? await mock.patchService(updateParams)
      : await mock.addServiceByAssoc(addParams);
    loading.value = false;
    if (error) { return; }
    notification.success(serviceDetail.value?.mockServiceId
      ? t('actions.tips.updateSuccess')
      : t('actions.tips.addSuccess'));
    emits('update:visible', false);
    emits('reload', data.id);
    reset();
  }, () => { /** */ });
};

const cancel = () => {
  emits('update:visible', false);
  reset();
};

const reset = () => {
  formRef.value.clearValidate();
  formState.value = {
    name: '',
    serviceDomainUrl: '',
    servicePort: '',
    nodeId: undefined,
    serviceId: serviceDetail.value?.id,
    apiIds: []
  };
};

const loadServiceInfo = async () => {
  loading.value = true;
  const [error, { data }] = await services.loadDetail(props.serviceId);
  loading.value = false;
  if (error) {
    return;
  }
  serviceDetail.value = data;
};

watch(() => props.visible, (newValue) => {
  if (newValue && props.serviceId !== serviceDetail.value?.id) {
    loadServiceInfo();
  }
}, {
  deep: true,
  immediate: true
});

onMounted(() => {
  isPrivate.value = appContext.isPrivateEdition();
});
</script>
<template>
  <Modal
    :title="t('service.mockService.form.title')"
    :visible="props.visible"
    :width="760"
    :footer="null"
    @cancel="cancel">
    <Spin :spinning="loading" :delay="0">
      <Form
        ref="formRef"
        size="small"
        class="ml-3.5 mt-3.5"
        :model="formState"
        :rules="rules">
        <div class="flex">
          <div>
            <FormItem :label="t('common.name')" required />
            <FormItem
              :label="t('protocol.domain')"
              :required="!isPrivate"
              :class="isPrivate?'pl-2.25':''" />
            <FormItem :label="t('protocol.port')" required />
            <FormItem :label="t('common.node')" required />
            <FormItem :label="t('common.service')" required />
            <template v-if="formState.serviceId && serviceDetail?.hasApis">
              <FormItem :label="t('common.api')" />
            </template>
          </div>
          <div class="w-150">
            <FormItem name="name">
              <Input
                v-model:value="formState.name"
                :placeholder="t('common.placeholders.searchKeyword')"
                :disabled="serviceDetail?.mockServiceId"
                :maxlength="100" />
            </FormItem>
            <FormItem name="serviceDomainUrl">
              <Input
                v-model:value="formState.serviceDomainUrl"
                :disabled="serviceDetail?.mockServiceId"
                :placeholder="t('service.mockService.form.domainPlaceholder')">
                <template v-if="!isPrivate && !serviceDetail?.mockServiceId" #addonAfter>
                  <span>{{ t('service.mockService.form.domainSuffix') }}</span>
                </template>
              </Input>
            </FormItem>
            <FormItem name="servicePort">
              <Input
                v-model:value="formState.servicePort"
                dataType="number"
                :placeholder="t('service.mockService.form.portPlaceholder')"
                :disabled="serviceDetail?.mockServiceId"
                :min="1"
                :max="65535" />
            </FormItem>
            <FormItem name="nodeId">
              <Select
                v-model:value="formState.nodeId"
                :disabled="serviceDetail?.mockServiceId"
                :action="`${TESTER}/node?fullTextSearch=true`"
                :fieldNames="{label:'name',value:'id'}"
                :maxlength="100"
                showSearch
                :placeholder="t('service.mockService.form.nodePlaceholder')"
                size="small">
                <template #option="item">
                  {{ t('service.mockService.form.nodeFormat', { name: item.name, ip: item.ip }) }}
                </template>
              </Select>
            </FormItem>
            <FormItem name="serviceId">
              <TreeSelect
                :action="`${TESTER}/services?fullTextSearch=true`"
                :fieldNames="{label:'name',value:'id'}"
                :defaultValue="serviceDetail"
                :virtual="false"
                size="small"
                disabled
                showSearch
                allowClear
                :placeholder="t('service.mockService.form.servicePlaceholder')"
                @change="treeSelectChange">
                <template #title="item">
                  <div class="text-3 leading-3 flex items-center h-6.5">
                    <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1" :class="`bg-blue-badge-s`">
                      {{ t('service.mockService.form.serviceFormat') }}
                    </label>
                    <div :title="item.name" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>
            </FormItem>
            <template v-if="formState.serviceId && serviceDetail?.hasApis">
              <FormItem name="apiIds">
                <ApiList
                  v-model:apiIds="formState.apiIds"
                  :serviceId="formState.serviceId" />
              </FormItem>
            </template>
          </div>
        </div>
      </Form>
      <div class="flex justify-end space-x-5 mt-5">
        <Button
          size="small"
          @click="cancel">
          {{ t('actions.cancel') }}
        </Button>
        <Button
          size="small"
          type="primary"
          @click="handleSave">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </Spin>
  </Modal>
</template>

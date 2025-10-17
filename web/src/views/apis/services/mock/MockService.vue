<script setup lang='ts'>
import { computed, inject, onMounted, ref } from 'vue';
import { Input, notification, Select, TreeSelect } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';
import type { Rule } from 'ant-design-vue/es/form';
import { mock, services } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ApiMenuKey } from '@/views/apis/menu';
import { ServicesDetail } from '@/views/apis/services/services/types';
import { ANGUS_MOCK_DOMAIN, ANGUS_MOCK_DOMAIN_REGEX, MockServiceEditForm } from '@/views/apis/mock/types';

import ApiList from '@/views/apis/mock/add/ApiList.vue';
import HeadInfo from '@/components/layout/header/info/index.vue';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const { t } = useI18n();
const router = useRouter();

const isPrivateEdition = ref(false);
const currentServiceInfo = ref<ServicesDetail>();
const isCreateMode = ref<boolean>();
const formRef = ref();
const mockServiceDetails = ref();
const isLoading = ref(false);
const selectedApiIds = ref<string[]>([]);

const mockServiceForm = ref<MockServiceEditForm>({
  name: '',
  serviceDomainUrl: '',
  servicePort: '',
  nodeId: undefined,
  serviceId: '',
  apiIds: []
});

/**
 * Validates the service domain URL format
 * @param _rule - Validation rule object
 * @param value - Domain value to validate
 * @returns Promise that resolves or rejects based on validation
 */
const validateServiceDomain = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('service.mockService.validation.domainRequired')));
  } else if (!ANGUS_MOCK_DOMAIN_REGEX.test(value + ANGUS_MOCK_DOMAIN)) {
    return Promise.reject(new Error(t('service.mockService.validation.domainInvalid')));
  } else {
    return Promise.resolve();
  }
};

const formValidationRules = computed(() => {
  const baseValidationRules = {
    name: [{ required: true, message: t('service.mock.nameRule'), trigger: 'change' }],
    servicePort: [{ required: true, message: t('service.mock.servicePortRule'), trigger: 'change' }],
    nodeId: [{ required: true, message: t('service.mock.nodeRule'), trigger: 'change' }]
  };

  const privateEditionRules = {
    ...baseValidationRules
  };

  const publicEditionRules = {
    ...baseValidationRules,
    serviceDomainUrl: [{ required: true, validator: validateServiceDomain, trigger: 'change' }]
  };

  return currentServiceInfo.value?.mockServiceId ? {} : isPrivateEdition.value ? privateEditionRules : publicEditionRules;
});

/**
 * Loads mock service details for editing
 */
const loadMockServiceDetails = async () => {
  isLoading.value = true;
  const [error, { data }] = await mock.getServiceDetail(currentServiceInfo.value?.mockServiceId as string);
  isLoading.value = false;
  if (error) { return; }

  mockServiceDetails.value = JSON.parse(JSON.stringify(data));
  const { name, serviceDomainUrl, servicePort, nodeId, apiIds } = data;
  mockServiceForm.value.name = name;
  mockServiceForm.value.serviceDomainUrl = serviceDomainUrl;
  mockServiceForm.value.servicePort = servicePort;
  mockServiceForm.value.nodeId = nodeId;
  mockServiceForm.value.apiIds = apiIds;
};

/**
 * Loads API IDs associated with the mock service
 */
const loadMockServiceApiIds = async () => {
  isLoading.value = true;
  const [error, { data }] = await mock.getServiceApiIds(currentServiceInfo.value?.mockServiceId as string);
  isLoading.value = false;
  if (error) { return; }

  const apiIds = data?.length ? data : [];
  mockServiceForm.value.apiIds = apiIds;
  selectedApiIds.value = JSON.parse(JSON.stringify(apiIds));
};

/**
 * Loads service information by ID
 */
const loadServiceDetails = async () => {
  const [error, { data }] = await services.loadDetail(props.id);
  if (error) {
    return;
  }
  currentServiceInfo.value = data;
};

/**
 * Handles service selection change in tree select
 * @param serviceId - Selected service ID
 */
const handleServiceSelectionChange = (serviceId: string) => {
  mockServiceForm.value.serviceId = serviceId;
};

/**
 * Handles form submission for creating or updating mock service
 */
const handleFormSubmit = () => {
  formRef.value.validate().then(async () => {
    isLoading.value = true;

    const createServiceParams = {
      name: mockServiceForm.value.name,
      serviceDomainUrl: !isPrivateEdition.value
        ? mockServiceForm.value.serviceDomainUrl + ANGUS_MOCK_DOMAIN
        : mockServiceForm.value.serviceDomainUrl,
      servicePort: mockServiceForm.value.servicePort,
      nodeId: mockServiceForm.value.nodeId,
      serviceId: mockServiceForm.value.serviceId,
      apiIds: mockServiceForm.value.apiIds
    };

    const updateServiceParams = {
      id: currentServiceInfo.value?.mockServiceId,
      apiIds: mockServiceForm.value.apiIds
    };

    const [error] = currentServiceInfo.value?.mockServiceId
      ? await mock.patchService(updateServiceParams)
      : await mock.addServiceByAssoc(createServiceParams);

    isLoading.value = false;
    if (error) { return; }

    notification.success(
      currentServiceInfo.value?.mockServiceId
        ? t('actions.tips.updateSuccess')
        : t('actions.tips.addSuccess')
    );
    await router.push(`/apis#${ApiMenuKey.MOCK}`);
  }, () => { /** */ });
};

/**
 * Handles cancel action and closes the tab
 */
const handleCancelAction = () => {
  deleteTabPane([props.id + 'mock']);
};

// ===== INJECTED DEPENDENCIES =====
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

// ===== LIFECYCLE HOOKS =====
onMounted(async () => {
  isPrivateEdition.value = appContext.isPrivateEdition();
  mockServiceForm.value.serviceId = props.id;
  await loadServiceDetails();
  isCreateMode.value = !currentServiceInfo.value?.mockServiceId;

  if (currentServiceInfo.value?.mockServiceId) {
    await loadMockServiceDetails();
    await loadMockServiceApiIds();
  }
});
</script>
<template>
  <div class="p-4 overflow-y-auto h-full">
    <HeadInfo :text="t('common.description')" />
    <Form
      ref="formRef"
      size="small"
      class="ml-3.5 mt-3.5"
      :model="mockServiceForm"
      :rules="formValidationRules">
      <div class="flex">
        <div>
          <FormItem :label="t('service.mock.createTypeLabel')" required />
          <FormItem :label="t('service.mock.nameLabel')" required />
          <FormItem
            :label="t('service.mock.serviceDomainUrlLabel')"
            :required="!isPrivateEdition"
            :class="isPrivateEdition?'pl-2.25':''" />
          <FormItem :label="t('service.mock.servicePortLabel')" required />
          <FormItem :label="t('service.mock.nodeIdLabel')" required />
          <FormItem :label="t('service.mock.serviceLabel')" required />
          <template v-if="mockServiceForm.serviceId && currentServiceInfo?.hasApis">
            <FormItem :label="t('service.mock.apiIdsLabel')" />
          </template>
        </div>
        <div class="w-150">
          <FormItem>
            <RadioGroup
              disabled
              :value="isCreateMode"
              class="h-6.25 mt-0.75 add-mock-type">
              <Radio :value="true">{{ t('service.mock.mockNew') }}</Radio>
              <Radio :value="false">{{ t('service.mock.mockUpdate') }}</Radio>
            </RadioGroup>
          </FormItem>
          <FormItem name="name">
            <Input
              v-model:value="mockServiceForm.name"
              :placeholder="t('common.placeholders.searchKeyword')"
              :disabled="!!currentServiceInfo?.mockServiceId"
              :maxlength="100" />
          </FormItem>
          <FormItem name="serviceDomainUrl">
            <Input
              v-model:value="mockServiceForm.serviceDomainUrl"
              :disabled="!!currentServiceInfo?.mockServiceId"
              :placeholder="t('service.mock.serviceDomainUrlPlaceholder')">
              <template v-if="!isPrivateEdition && !currentServiceInfo?.mockServiceId" #addonAfter>
                <span>{{ ANGUS_MOCK_DOMAIN }}</span>
              </template>
            </Input>
          </FormItem>
          <FormItem name="servicePort">
            <Input
              v-model:value="mockServiceForm.servicePort"
              dataType="number"
              :placeholder="t('service.mock.servicePortPlaceholder')"
              :disabled="!!currentServiceInfo?.mockServiceId"
              :min="1"
              :max="65535" />
          </FormItem>
          <FormItem name="nodeId">
            <Select
              v-model:value="mockServiceForm.nodeId"
              :disabled="!!currentServiceInfo?.mockServiceId"
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
              :fieldNames="{label:'name',value:'id',children:'children'}"
              :defaultValue="currentServiceInfo?.id ? { id: currentServiceInfo.id, name: currentServiceInfo.name } : undefined"
              :virtual="false"
              size="small"
              disabled
              showSearch
              allowClear
              :placeholder="t('service.mock.servicePlaceholder')"
              @change="handleServiceSelectionChange">
              <template #title="item">
                <div class="text-3 leading-3 flex items-center h-6.5">
                  <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1 bg-blue-badge-s">S</label>
                  <div :title="item.name" class="truncate">{{ item.name }}</div>
                </div>
              </template>
            </TreeSelect>
          </FormItem>
          <template v-if="mockServiceForm.serviceId && currentServiceInfo?.hasApis">
            <FormItem name="apiIds">
              <ApiList
                v-model:apiIds="mockServiceForm.apiIds"
                :serviceId="mockServiceForm.serviceId"
                :checkedIds="selectedApiIds" />
            </FormItem>
          </template>
          <FormItem>
            <Button size="small" @click="handleCancelAction">{{ t('actions.confirm') }}</Button>
            <Button
              size="small"
              type="primary"
              class="ml-3"
              :loading="isLoading"
              @click="handleFormSubmit">
              {{ t('actions.cancel') }}
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

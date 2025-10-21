<script setup lang="ts">
import { computed, inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
import { Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

import { apis, mock } from '@/api/tester';

interface Props {
  disabled: boolean;
  id: string; // API ID
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const { t } = useI18n();

/**
 * Injected project information
 */
const projectId = inject<Ref<string>>('projectId', ref(''));

const mockApiCreationType = ref();
const mockApiId = ref();
const mockServiceId = ref();
const mockApiInformation = ref();

const isLoading = ref(true);

/**
 * Loads mock API information
 * <p>
 * Fetches mock API data for the specified API ID
 * </p>
 * @param apiId - API ID to load mock information for
 */
const loadMockApiInformation = async (apiId: string) => {
  const [error, { data }] = await apis.getMockApiByApiId(apiId);
  isLoading.value = false;
  if (error) {
    return;
  }
  mockApiInformation.value = JSON.parse(JSON.stringify(data));
};

/**
 * API summary for mock creation
 */
const apiSummary = ref('');

/**
 * Mock API creation loading state
 */
const isCreatingMockApi = ref(false);

/**
 * Creates a new mock API by API ID
 * <p>
 * Generates a new mock API based on the current API configuration
 * </p>
 */
const createMockApiByApiId = async () => {
  isCreatingMockApi.value = true;
  const requestParams = apiSummary.value
    ? { mockServiceId: mockServiceId.value, summary: apiSummary.value }
    : { mockServiceId: mockServiceId.value };
  const [error, { data }] = await apis.addMockApiByApiId(props.id, requestParams);
  isCreatingMockApi.value = false;
  if (error) {
    return;
  }
  mockApiId.value = data?.id;
  notification.success(t('service.mockApi.messages.generateSuccess'));
  await loadMockApiInformation(props.id);
};

/**
 * Selected mock API ID for association
 */
const selectedMockApiId = ref();

/**
 * Mock API association loading state
 */
const isAssociatingMockApi = ref(false);

/**
 * Associates an existing mock API with the current API
 * <p>
 * Links an existing mock API to the current API for testing purposes
 * </p>
 */
const associateMockApiWithCurrentApi = async () => {
  isAssociatingMockApi.value = true;
  const [error] = await mock.assocMockApi(selectedMockApiId.value, props.id);
  isAssociatingMockApi.value = false;
  if (error) {
    return;
  }
  mockApiId.value = selectedMockApiId.value;
  notification.success(t('service.mockApi.messages.associateSuccess'));
  await loadMockApiInformation(props.id);
};

/**
 * Cancels mock API association
 * <p>
 * Removes the association between the current API and its mock API
 * </p>
 */
const cancelMockApiAssociation = async () => {
  isLoading.value = true;
  const [error] = await mock.cancelMockApiAssoc([mockApiInformation.value.id]);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('service.mockApi.messages.cancelAssociationSuccess'));
  mockApiInformation.value = undefined;
  mockServiceId.value = undefined;
};

/**
 * Handles modal confirmation
 * <p>
 * Executes the appropriate action based on the selected creation type
 * </p>
 */
const handleModalConfirmation = () => {
  if (mockApiCreationType.value === '1') {
    createMockApiByApiId();
  } else if (mockApiCreationType.value === '2') {
    associateMockApiWithCurrentApi();
  }
};

/**
 * Handles modal cancellation
 * <p>
 * Resets the creation type when modal is cancelled
 * </p>
 */
const handleModalCancellation = () => {
  mockApiCreationType.value = undefined;
};

/**
 * Loads API information
 * <p>
 * Fetches API details and initializes mock service configuration
 * </p>
 * @param apiId - API ID to load information for
 */
const loadApiInformation = async (apiId: string) => {
  const [error, { data }] = await apis.getApiDetail(apiId);
  if (error) {
    return;
  }
  mockServiceId.value = data?.mockServiceId || undefined;
  apiSummary.value = data?.summary || '';
};

/**
 * Computed mock API parameters
 * <p>
 * Returns parameters for mock API requests
 * </p>
 */
const mockApiRequestParams = computed(() => {
  return { mockServiceId: mockServiceId.value };
});

/**
 * Formats mock API data for display
 * <p>
 * Adds disabled state based on association status
 * </p>
 * @param data - Mock API data to format
 * @returns Formatted data with disabled state
 */
const formatMockApiData = (data: any) => {
  return { ...data, disabled: !!data.assocApisId };
};

/**
 * Watches for API ID changes
 * <p>
 * Loads mock API information when API ID changes
 * </p>
 */
watch(() => props.id, async (newApiId) => {
  mockApiInformation.value = undefined;
  if (newApiId) {
    await loadMockApiInformation(newApiId);
    await loadApiInformation(newApiId);
  }
}, {
  immediate: true
});

/**
 * Mock service statistics configuration
 * <p>
 * Configuration for displaying mock service statistics
 * </p>
 */
const mockServiceStatisticsConfig = [
  {
    name: t('service.mockApi.statistics.requestNum'),
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: t('service.mockApi.statistics.pushbackNum'),
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: t('service.mockApi.statistics.simulateErrorNum'),
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: t('service.mockApi.statistics.successNum'),
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: t('service.mockApi.statistics.exceptionNum'),
    key: 'exceptionNum',
    icon: 'icon-yichangshu1'
  }
];

/**
 * Grid columns configuration
 * <p>
 * Configuration for displaying mock API information in a grid format
 * </p>
 */
const gridColumnsConfiguration = [[
  { label: t('service.mockApi.columns.apiName'), dataIndex: 'name' },
  { label: t('service.mockApi.columns.mockServiceName'), dataIndex: 'mockServiceName' }
], [
  { label: t('common.method'), dataIndex: 'method' },
  { label: t('service.mockApi.columns.mockServiceHostUrl'), dataIndex: 'mockServiceHostUrl' }
], [
  { label: t('common.createdBy'), dataIndex: 'createdByName' },
  { label: t('common.createdDate'), dataIndex: 'createdDate' }
]];
</script>
<template>
  <div class="h-full w-full">
    <Spin
      class="h-full w-full"
      :spinning="isLoading"
      :delay="0">
      <template v-if="mockApiInformation">
        <Grid
          :columns="gridColumnsConfiguration"
          :dataSource="mockApiInformation"
          marginBottom="18px">
          <template #name="{text}">
            <div class="flex items-start text-3 leading-5">
              <span>{{ text }}</span>
              <template v-if="props.disabled">
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">{{ t('service.mockApi.actions.cancelAssociation') }}</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cancelMockApiAssociation">{{ t('service.mockApi.actions.cancelAssociation') }}</a>
              </template>
            </div>
          </template>
          <template #method="{text}">
            {{ text?.message }}
          </template>
          <template #mockServiceHostUrl="{text}">
            <template v-if="!text && !mockApiInformation?.mockServiceDomainUrl">
              --
            </template>
            <template v-else>
              <div>
                <div v-if="mockApiInformation?.mockServiceDomainUrl">
                  <span>{{ mockApiInformation.mockServiceDomainUrl }}</span>
                  <span :title="t('actions.copy')"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="mockApiInformation.mockServiceDomainUrl" /></span>
                </div>
                <div v-if="text" class="flex items-start">
                  <span>{{ text }}</span>
                  <span :title="t('actions.copy')"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" /></span>
                </div>
              </div>
            </template>
          </template>
        </Grid>
        <Divider />
        <div class="text-3 space-x-2 pt-1 leading-5 flex">
          <div
            v-for="(item,index) in mockServiceStatisticsConfig"
            :key="index"
            class="inline-flex space-x-3 items-center border border-border-divider rounded bg-blue-bg-light px-3.5 py-1.5">
            <Icon :icon="item.icon" class="mr-2" />
            <div class="flex flex-1 justify-between items-center space-x-3">
              <span>{{ item.name }}</span>
              <span>{{ mockApiInformation?.[item.key] }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-if="!mockApiInformation && !isLoading">
        <div class="p-2 border border-blue-border rounded bg-blue-bg-light">
          <Hints :text="t('service.mockApi.description')" />
        </div>
        <div class=" mt-4">
          <RadioGroup
            v-model:value="mockApiCreationType"
            :disabled="props.disabled"
            class="flex flex-col space-y-3.5">
            <Radio value="1">
              <span class="font-semibold">{{ t('service.mockApi.createTypes.generate.title') }}</span>
              <div>{{ t('service.mockApi.createTypes.generate.description') }}</div>
            </Radio>
            <Radio value="2">
              <span class="font-semibold">{{ t('service.mockApi.createTypes.associate.title') }}</span>
              <div>{{ t('service.mockApi.createTypes.associate.description') }}</div>
            </Radio>
          </RadioGroup>
          <Modal
            :visible="!!mockApiCreationType"
            :title="mockApiCreationType === '1' ? t('service.mockApi.modal.generateTitle') : t('service.mockApi.modal.associateTitle')"
            :okButtonProps="{loading: isCreatingMockApi || isAssociatingMockApi, disabled: !mockServiceId || (mockApiCreationType === '2' && !selectedMockApiId)}"
            @ok="handleModalConfirmation"
            @cancel="handleModalCancellation">
            <Select
              v-model:value="mockServiceId"
              :action="`${TESTER}/mock/service?projectId=${projectId}&fullTextSearch=true`"
              :fieldNames="{label:'name',value:'id'}"
              :maxlength="100"
              class="w-full mb-4"
              :placeholder="t('service.mockApi.modal.mockServicePlaceholder')"
              showSearch />
            <template v-if="mockApiCreationType === '1'">
              <Hints :text="t('service.mockApi.modal.generateHint')" class="mb-2" />
              <Input v-model:value="apiSummary" :placeholder="t('service.mockApi.modal.summaryPlaceholder')" />
            </template>
            <template v-else>
              <Hints :text="t('service.mockApi.modal.associateHint')" class="mb-2" />
              <Select
                v-model:value="selectedMockApiId"
                :disabled="!mockServiceId"
                :action="`${TESTER}/mock/apis`"
                :params="mockApiRequestParams"
                :fieldNames="{label:'summary',value:'id'}"
                class="w-full"
                :format="formatMockApiData"
                :maxlength="100"
                allowClear
                :placeholder="t('service.mockApi.modal.mockApiPlaceholder')"
                showSearch />
            </template>
          </Modal>
        </div>
      </template>
    </Spin>
  </div>
</template>

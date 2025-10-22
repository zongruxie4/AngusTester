<script setup lang="ts">
import { computed, inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

import { apis, mock } from '@/api/tester';

interface Props {
  disabled: boolean;
  id: string; // API id
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const { t } = useI18n();

// Inject project id for fetching mock services
const projectId = inject<Ref<string>>('projectId', ref(''));

// UI and data states
const createType = ref('1');
const mockApisId = ref<string | undefined>();
const mockServiceId = ref<string>('');
const mockApiInfo = ref<Record<string, any> | undefined>();
const loading = ref(true);
// Load associated mock API info by API id
const loadMockApiInfo = async (id: string) => {
  const [error, { data }] = await apis.getMockApiByApiId(id);
  loading.value = false;
  if (error) { return; }
  mockApiInfo.value = JSON.parse(JSON.stringify(data));
};

const summary = ref('');
const createApiLoading = ref(false);
// Create a new mock API under selected mock service and associate it
const createMockApiById = async () => {
  createApiLoading.value = true;
  if (!mockServiceId.value) {
    createApiLoading.value = false;
    return;
  }
  const params: { mockServiceId: string; summary?: string } = summary.value
    ? { mockServiceId: mockServiceId.value, summary: summary.value }
    : { mockServiceId: mockServiceId.value };
  const [error, { data }] = await apis.addMockApiByApiId(props.id, params);
  createApiLoading.value = false;
  if (error) { return; }
  mockApisId.value = data?.id;
  notification.success(t('actions.tips.createSuccess'));
  loadMockApiInfo(props.id);
};

const selectedMockApiId = ref();

const relatedLoading = ref(false);
// Associate selected mock API with current API
const relatedMockServiceApi = async () => {
  relatedLoading.value = true;
  const [error] = await mock.assocMockApi(selectedMockApiId.value, props.id);
  relatedLoading.value = false;
  if (error) {
    return;
  }
  mockApisId.value = selectedMockApiId.value;
  notification.success(t('service.mockApi.messages.associateSuccess'));
  await loadMockApiInfo(props.id);
};

// Cancel association with current mock API
const cancelServiceMock = async () => {
  loading.value = true;
  const id = mockApiInfo.value?.id;
  if (!id) {
    loading.value = false;
    return;
  }
  const [error] = await mock.cancelMockApiAssoc([id]);
  loading.value = false;
  if (error) { return; }
  notification.success(t('service.mockApi.messages.cancelAssociationSuccess'));
  mockApiInfo.value = undefined;
  mockServiceId.value = '';
};

// Load API basic info to prefill mockServiceId and name
const loadApiInfo = async (_id: string) => {
  const [error, { data }] = await apis.getApiDetail(_id);
  if (error) {
    return;
  }
  mockServiceId.value = data?.mockServiceId || '';
  summary.value = data?.summary || '';
};

// Params for fetching mock API list under a mock service
const mockApiParams = computed(() => {
  return { mockServiceId: mockServiceId.value };
});

// Disable options already associated to other APIs
const format = (data: Record<string, any>) => ({ ...data, disabled: !!data.assocApisId });

// When API id changes, reload mock info and base API info
watch(() => props.id, async (newValue) => {
  mockApiInfo.value = undefined;
  if (newValue) {
    await loadMockApiInfo(newValue);
    loadApiInfo(newValue);
  }
}, {
  immediate: true
});

const columns = [[
  { label: t('common.id'), dataIndex: 'id' },
  { label: t('common.name'), dataIndex: 'name' },
  { label: t('service.mockApi.columns.mockServiceId'), dataIndex: 'mockServiceId' },
  { label: t('service.mockApi.columns.mockServiceName'), dataIndex: 'mockServiceName' },
  { label: t('service.mockApi.columns.mockServiceHostUrl'), dataIndex: 'mockServiceHostUrl' },
  { label: t('common.createdBy'), dataIndex: 'createdBy' },
  { label: t('common.createdDate'), dataIndex: 'createdDate' }
]];

const mockServiceCount = [
  {
    name: t('service.mockApi.stats.requestNum'),
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: t('service.mockApi.stats.pushbackNum'),
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: t('service.mockApi.stats.simulateErrorNum'),
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: t('service.mockApi.stats.successNum'),
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: t('service.mockApi.stats.exceptionNum'),
    key: 'exceptionNum',
    icon: 'icon-yichangshu1'
  }
];
</script>
<template>
  <div class="h-full w-full">
    <Spin
      class="h-full w-full"
      :spinning="loading"
      :delay="0">
      <template v-if="mockApiInfo">
        <Grid
          :columns="columns"
          :dataSource="mockApiInfo"
          marginBottom="18px">
          <template #name="{text}">
            <div class="flex items-start text-3 leading-5">
              <span>{{ text }}</span>
              <template v-if="props.disabled">
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">
                  {{ t('service.mockApi.actions.cancelAssociate') }}
                </a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cancelServiceMock">
                  {{ t('service.mockApi.actions.cancelAssociate') }}
                </a>
              </template>
            </div>
          </template>
          <template #mockServiceHostUrl="{text}">
            <template v-if="!text && !mockApiInfo?.mockServiceDomainUrl">
              --
            </template>
            <template v-else>
              <div>
                <div v-if="mockApiInfo?.mockServiceDomainUrl">
                  <span>{{ mockApiInfo.mockServiceDomainUrl }}</span>
                  <span :title="t('actions.copy')">
                    <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="mockApiInfo.mockServiceDomainUrl" />
                  </span>
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
        <div class="text-3 space-y-2 pt-1 leading-5">
          <div
            v-for="(item,index) in mockServiceCount"
            :key="index"
            class="flex items-center border border-border-divider rounded bg-gray-100 px-3.5 py-1.5">
            <Icon :icon="item.icon" class="mr-2" />
            <div class="flex flex-1 justify-between items-center">
              <span>{{ item.name }}</span>
              <span>{{ mockApiInfo?.[item.key] }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-if="!mockApiInfo && !loading">
        <div class="flex flex-col space-y-3.5">
          <Hints :text="t('service.mockApi.hints.selectMockServiceFirst')" />
          <RadioGroup v-model:value="createType">
            <Radio value="1">{{ t('service.mockApi.generateMockApi') }}</Radio>
            <Radio value="2">{{ t('service.mockApi.associateMockApi') }}</Radio>
          </RadioGroup>
          <Select
            v-model:value="mockServiceId"
            :action="`${TESTER}/mock/service?projectId=${projectId}&fullTextSearch=true`"
            :fieldNames="{label:'name',value:'id'}"
            :maxlength="100"
            :placeholder="t('service.mockApi.placeholder.selectMockService')"
            showSearch />
          <template v-if="createType === '1'">
            <Hints :text="t('service.mockApi.hints.generateMockApi')" />
            <Input v-model:value="summary" :placeholder="t('service.mockApi.placeholder.inputMockApiName')" />
            <div class="flex justify-end">
              <Button
                :disabled="!mockServiceId || props.disabled"
                :loading="createApiLoading"
                size="small"
                type="primary"
                @click="createMockApiById">
                {{ t('actions.confirm') }}
              </Button>
            </div>
          </template>
          <template v-else>
            <Hints :text="t('service.mockApi.hints.associateMockApi')" />
            <Select
              v-model:value="selectedMockApiId"
              :disabled="!mockServiceId"
              :action="`${TESTER}/mock/apis`"
              :params="mockApiParams"
              :fieldNames="{label:'summary',value:'id'}"
              :format="format"
              :maxlength="100"
              allowClear
              :placeholder="t('service.mockApi.placeholder.selectMockApi')"
              showSearch />
            <div class="flex justify-end">
              <Button
                :disabled="!mockServiceId || !selectedMockApiId || props.disabled"
                size="small"
                type="primary"
                @click="relatedMockServiceApi">
                {{ t('actions.confirm') }}
              </Button>
            </div>
          </template>
        </div>
      </template>
    </Spin>
  </div>
</template>

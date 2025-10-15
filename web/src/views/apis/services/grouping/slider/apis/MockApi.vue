<script setup lang="ts">
import { computed, inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

import { apis, mock } from '@/api/tester';

interface Props {
  disabled:boolean;
  id: string; // api id
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const createType = ref('1');
const mockApisId = ref();
const mockServiceId = ref();
const mockApiInfo = ref();
const loading = ref(true);
const loadMockApiInfo = async (id:string) => {
  const [error, { data }] = await apis.getMockApiByApiId(id);
  loading.value = false;
  if (error) { return; }
  mockApiInfo.value = JSON.parse(JSON.stringify(data));
};

const columns = [[
  { label: t('common.id'), dataIndex: 'id' },
  { label: t('common.name'), dataIndex: 'name' },
  { label: t('service.ApiMock.columns.mockServiceId'), dataIndex: 'mockServiceId' },
  { label: t('service.ApiMock.columns.mockServiceName'), dataIndex: 'mockServiceName' },
  { label: t('service.ApiMock.columns.mockServiceHostUrl'), dataIndex: 'mockServiceHostUrl' },
  { label: t('common.createdBy'), dataIndex: 'createdBy' },
  { label: t('common.createdDate'), dataIndex: 'createdDate' }
]];

const summary = ref('');
const createApiLoading = ref(false);
const createMockApiById = async () => {
  createApiLoading.value = true;
  const params = summary.value ? { mockServiceId: mockServiceId.value, summary: summary.value } : { mockServiceId: mockServiceId.value };
  const [error, { data }] = await apis.addMockApiByApiId(props.id, params);
  createApiLoading.value = false;
  if (error) { return; }
  mockApisId.value = data?.id;
  notification.success(t('actions.tips.createSuccess'));
  loadMockApiInfo(props.id);
};

const selectedMockApiId = ref();

const relatedLoading = ref(false);
const relatedMockServiceApi = async () => {
  relatedLoading.value = true;
  const [error] = await mock.assocMockApi(selectedMockApiId.value, props.id);
  relatedLoading.value = false;
  if (error) {
    return;
  }
  mockApisId.value = selectedMockApiId.value;
  notification.success(t('service.ApiMock.messages.associateSuccess'));
  loadMockApiInfo(props.id);
};

const mockServiceCount = [
  {
    name: t('service.ApiMock.stats.requestNum'),
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: t('service.ApiMock.stats.pushbackNum'),
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: t('service.ApiMock.stats.simulateErrorNum'),
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: t('service.ApiMock.stats.successNum'),
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: t('service.ApiMock.stats.exceptionNum'),
    key: 'exceptionNum',
    icon: 'icon-yichangshu1'
  }
];

const cencelProjcetMock = async () => {
  loading.value = true;
  const [error] = await mock.cancelMockApiAssoc([mockApiInfo.value.id]);
  loading.value = false;
  if (error) { return; }
  notification.success(t('service.ApiMock.messages.cancelAssociateSuccess'));
  mockApiInfo.value = undefined;
  mockServiceId.value = undefined;
};

const loadApiInfo = async (_id) => {
  const [error, { data }] = await apis.getApiDetail(_id);
  if (error) {
    return;
  }
  mockServiceId.value = data?.mockServiceId || undefined;
  summary.value = data?.summary || '';
};

watch(() => props.id, async (newValue) => {
  mockApiInfo.value = undefined;
  if (newValue) {
    await loadMockApiInfo(newValue);
    loadApiInfo(newValue);
  }
}, {
  immediate: true
});

const mockApiParams = computed(() => {
  return { mockServiceId: mockServiceId.value };
});

const format = (data) => {
  return { ...data, disabled: !!data.assocApisId };
};
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
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">{{ t('service.ApiMock.actions.cancelAssociate') }}</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cencelProjcetMock">{{ t('service.ApiMock.actions.cancelAssociate') }}</a>
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
                  <span :title="t('actions.copy')"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="mockApiInfo.mockServiceDomainUrl" /></span>
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
          <Hints :text="t('service.ApiMock.hints.selectMockServiceFirst')" />
          <RadioGroup v-model:value="createType">
            <Radio value="1">{{ t('service.ApiMock.options.generateMockApi') }}</Radio>
            <Radio value="2">{{ t('service.ApiMock.options.associateMockApi') }}</Radio>
          </RadioGroup>
          <Select
            v-model:value="mockServiceId"
            :action="`${TESTER}/mock/service?projectId=${projectId}&fullTextSearch=true`"
            :fieldNames="{label:'name',value:'id'}"
            :maxlength="100"
            :placeholder="t('service.ApiMock.placeholder.selectMockService')"
            showSearch />
          <template v-if="createType === '1'">
            <Hints :text="t('service.ApiMock.hints.generateMockApi')" />
            <Input v-model:value="summary" :placeholder="t('service.ApiMock.placeholder.inputMockApiName')" />
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
            <Hints :text="t('service.ApiMock.hints.associateMockApi')" />
            <Select
              v-model:value="selectedMockApiId"
              :disabled="!mockServiceId"
              :action="`${TESTER}/mock/apis`"
              :params="mockApiParams"
              :fieldNames="{label:'summary',value:'id'}"
              :format="format"
              :maxlength="100"
              allowClear
              :placeholder="t('service.ApiMock.placeholder.selectMockApi')"
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

<script setup lang="ts">
import { computed, inject, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
import { Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';

import { apis, mock } from '@/api/tester';

interface Props {
  disabled:boolean;
  id: string; // api id
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const createType = ref();
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
  { label: t('service.mockApi.columns.apiName'), dataIndex: 'name' },
  { label: t('service.mockApi.columns.mockServiceName'), dataIndex: 'mockServiceName' }
], [
  { label: t('service.mockApi.columns.method'), dataIndex: 'method' },
  { label: t('service.mockApi.columns.mockServiceHostUrl'), dataIndex: 'mockServiceHostUrl' }
],
[
  { label: t('service.mockApi.columns.createdBy'), dataIndex: 'createdByName' },
  { label: t('service.mockApi.columns.createdDate'), dataIndex: 'createdDate' }
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
  notification.success(t('service.mockApi.messages.generateSuccess'));
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
  notification.success(t('service.mockApi.messages.associateSuccess'));
  loadMockApiInfo(props.id);
};

const mockServiceCount = [
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

const cencelProjcetMock = async () => {
  loading.value = true;
  const [error] = await mock.cancelMockApiAssoc([mockApiInfo.value.id]);
  loading.value = false;
  if (error) { return; }
  notification.success(t('service.mockApi.messages.cancelAssociationSuccess'));
  mockApiInfo.value = undefined;
  mockServiceId.value = undefined;
};

const onOk = () => {
  if (createType.value === '1') {
    createMockApiById();
  } else if (createType.value === '2') {
    relatedMockServiceApi();
  }
};
const cancel = () => {
  createType.value = undefined;
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
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">{{ t('service.mockApi.actions.cancelAssociation') }}</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cencelProjcetMock">{{ t('service.mockApi.actions.cancelAssociation') }}</a>
              </template>
            </div>
          </template>
          <template #method="{text}">
            {{ text?.message }}
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
        <div class="text-3 space-x-2 pt-1 leading-5 flex">
          <div
            v-for="(item,index) in mockServiceCount"
            :key="index"
            class="inline-flex space-x-3 items-center border border-border-divider rounded bg-blue-bg-light px-3.5 py-1.5">
            <Icon :icon="item.icon" class="mr-2" />
            <div class="flex flex-1 justify-between items-center space-x-3">
              <span>{{ item.name }}</span>
              <span>{{ mockApiInfo?.[item.key] }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-if="!mockApiInfo && !loading">
        <div class="p-2 border border-blue-border rounded bg-blue-bg-light">
          <Hints :text="t('common.description')" />
        </div>
        <div class=" mt-4">
          <RadioGroup
            v-model:value="createType"
            :disabled="props.disabled"
            class="flex flex-col space-y-3.5">
            <Radio value="1">
              <span class="font-semibold">{{ t('service.mockApi.createTypes.generate.title') }}</span>
              <div>{{ t('common.description') }}</div>
            </Radio>
            <Radio value="2">
              <span class="font-semibold">{{ t('service.mockApi.createTypes.associate.title') }}</span>
              <div>{{ t('common.description') }}</div>
            </Radio>
          </RadioGroup>
          <Modal
            :visible="!!createType"
            :title="createType === '1' ? t('service.mockApi.modal.generateTitle') : t('service.mockApi.modal.associateTitle')"
            :okButtonProps="{loading: createApiLoading || relatedLoading, disabled: !mockServiceId || (createType === '2' && !selectedMockApiId)}"
            @ok="onOk"
            @cancel="cancel">
            <Select
              v-model:value="mockServiceId"
              :action="`${TESTER}/mock/service?projectId=${projectId}&fullTextSearch=true`"
              :fieldNames="{label:'name',value:'id'}"
              :maxlength="100"
              class="w-full mb-4"
              :placeholder="t('service.mockApi.modal.mockServicePlaceholder')"
              showSearch />
            <template v-if="createType === '1'">
              <Hints :text="t('service.mockApi.modal.generateHint')" class="mb-2" />
              <Input v-model:value="summary" :placeholder="t('service.mockApi.modal.summaryPlaceholder')" />
            </template>
            <template v-else>
              <Hints :text="t('service.mockApi.modal.associateHint')" class="mb-2" />
              <Select
                v-model:value="selectedMockApiId"
                :disabled="!mockServiceId"
                :action="`${TESTER}/mock/apis`"
                :params="mockApiParams"
                :fieldNames="{label:'summary',value:'id'}"
                class="w-full"
                :format="format"
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

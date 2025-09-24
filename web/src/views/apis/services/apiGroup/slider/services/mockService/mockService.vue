<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Grid, Hints, Icon, IconCopy, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
import { Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';

import { analysis, mock, services } from '@/api/tester';

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const CreateMock = defineAsyncComponent(() => import('./addMock.vue'));

interface Props {
  disabled: boolean;
  id: string;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const router = useRouter();
const appInfo = ref(appContext.getAccessApp()) as Ref<Record<string, any>>;
const mockServiceInfo = ref();
const loading = ref();

const columns = [[
  { label: t('service.mockService.columns.name'), dataIndex: 'name' },
  { label: t('service.mockService.columns.nodeName'), dataIndex: 'nodeName' },
  { label: t('service.mockService.columns.auth'), dataIndex: 'auth' }
], [

  { label: t('service.mockService.columns.status'), dataIndex: 'status' },
  { label: t('service.mockService.columns.servicePort'), dataIndex: 'servicePort' },
  { label: t('service.mockService.columns.serviceDomainUrl'), dataIndex: 'serviceDomainUrl' }
], [
  { label: t('service.mockService.columns.createdByName'), dataIndex: 'createdByName' },
  { label: t('service.mockService.columns.createdDate'), dataIndex: 'createdDate' }
]];

const createType = ref();

const selectedMockServiceId = ref();
const createVisible = ref(false);
const createMockServiceById = async () => {
  createVisible.value = true;
};

watch(() => createType.value, () => {
  if (createType.value === '1') {
    createMockServiceById();
  }
});

watch(() => createVisible.value, () => {
  if (!createVisible.value) {
    createType.value = undefined;
  }
});

const handelReload = () => {
  loadProjectMockService();
};
const relatedLoading = ref(false);
const relatedMockService = async () => {
  relatedLoading.value = true;
  const [error] = await mock.assocMockService(selectedMockServiceId.value, props.id);
  relatedLoading.value = false;
  createType.value = undefined;
  if (error) {
    return;
  }
  notification.success(t('service.mockService.messages.associateSuccess'));
  loadProjectMockService();
};

const handleCloseModal = () => {
  createType.value = undefined;
};

const apisCount = ref({
  apisNum: '0',
  requestNum: '0',
  pushbackNum: '0',
  simulateErrorNum: '0',
  successNum: '0',
  exceptionNum: '0'
});

const mockServiceCount = [
  {
    name: t('service.mockService.stats.apisNum'),
    key: 'apisNum',
    icon: 'icon-jiekoushu'
  },
  {
    name: t('service.mockService.stats.requestNum'),
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: t('service.mockService.stats.pushbackNum'),
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: t('service.mockService.stats.simulateErrorNum'),
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: t('service.mockService.stats.successNum'),
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: t('service.mockService.stats.exceptionNum'),
    key: 'exceptionNum',
    icon: 'icon-yichangshu1'
  }
];

const getAnalysisMockService = async (id) => {
  const [error, { data }] = await analysis.getAnalysisMockService(id);
  if (error) { return; }
  apisCount.value = data;
};

const authVisible = ref(false);
const openAuth = () => {
  authVisible.value = true;
};

const authFlagChange = ({ auth }: { auth: boolean }) => {
  mockServiceInfo.value.auth = auth;
};

const cancelProjectMock = async () => {
  loading.value = true;
  const [error] = await mock.cancelMockServiceAssoc(mockServiceInfo.value.id);
  loading.value = false;
  if (error) { return; }
  notification.success(t('service.mockService.messages.cancelAssociateSuccess'));
  mockServiceInfo.value = undefined;
};

const loadProjectMockService = async () => {
  loading.value = true;
  const [error, { data }] = await services.loadServicesMockService(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  mockServiceInfo.value = data;
  if (mockServiceInfo.value?.id) {
    getAnalysisMockService(mockServiceInfo.value.id);
  }
};

onMounted(() => {
  loadProjectMockService();
});

const gotoMock = () => {
  router.push(`/mockservice?sid=${mockServiceInfo.value.id}`);
};

const countIconColor = {
  apisNum: 'text-status-pending',
  requestNum: 'text-status-process',
  pushbackNum: 'text-status-orange',
  simulateErrorNum: 'text-status-error1',
  successNum: 'text-status-success',
  exceptionNum: 'text-status-error'
};

const statusStyleMap = {
  NOT_STARTED: '#B7BBC2',
  RUNNING: '#52c41a',
  STOPPED: '#abd3ff'
};
</script>
<template>
  <div class="h-full">
    <Spin :spinning="loading" :delay="0">
      <template v-if="mockServiceInfo">
        <Grid
          :columns="columns"
          :dataSource="mockServiceInfo"
          marginBottom="18px">
          <template #name="{ text }">
            <div class="flex items-start text-3 leading-5">
              <a class="text-text-link" @click="gotoMock">{{ text }}</a>
              <template v-if="props.disabled">
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">{{ t('service.mockService.actions.cancelAssociate') }}</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cancelProjectMock">{{ t('service.mockService.actions.cancelAssociate') }}</a>
              </template>
            </div>
          </template>
          <template #status="{ text }">
            <div class="flex items-center">
              <div class="w-1.5 h-1.5 rounded-xl mr-1" :style="'background-color:' + statusStyleMap[text.value]"></div>
              <span>{{ text?.message || "--" }}</span>
            </div>
          </template>
          <template #auth="{ text }">
            <div class="flex items-center text-3">
              <div>{{ text ? t('service.mockService.auth.hasPermission') : t('service.mockService.auth.noPermission') }}</div>
              <template v-if="props.disabled">
                <Icon icon="icon-shuxie" class="text-text-disabled text-3 cursor-not-allowed ml-2" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="text-text-link text-3 cursor-pointer ml-2"
                  @click="openAuth" />
              </template>
            </div>
          </template>
          <template #serviceDomainUrl="{ text }">
            <template v-if="!text && !mockServiceInfo?.serviceHostUrl">
              --
            </template>
            <template v-else>
              <div>
                <div v-if="text" class="flex items-start">
                  <span>{{ text }}</span>
                  <span :title="t('actions.copy')">
                    <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" />
                  </span>
                </div>
                <div v-if="mockServiceInfo?.serviceHostUrl">
                  <span>{{ mockServiceInfo.serviceHostUrl }}</span>
                  <span :title="t('actions.copy')">
                    <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="mockServiceInfo.serviceHostUrl" />
                  </span>
                </div>
              </div>
            </template>
          </template>
        </Grid>
        <Divider />
        <div class="text-3 space-x-2 pt-1 leading-5 flex items-center">
          <div
            v-for="(item, index) in mockServiceCount"
            :key="index"
            class="inline-flex justify-around flex-1 items-center border border-border-divider rounded bg-blue-bg-light px-3.5 py-1.5">
            <Icon
              :icon="item.icon"
              :class="countIconColor[item.key]"
              class="mr-2 text-4" />
            <div class="flex flex-1 justify-between items-center">
              <span>{{ item.name }}</span>
              <span class="font-semibold">{{ apisCount?.[item.key] }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="p-2 border border-blue-border rounded bg-blue-bg-light">
          <Hints :text="t('service.mockService.description.title')" />
        </div>
        <div class=" mt-3">
          <RadioGroup
            v-model:value="createType"
            :disabled="props.disabled"
            class="flex flex-col space-y-3.5">
            <Radio value="1">
              <span class="font-semibold">{{ t('service.mockService.options.generateMockService') }}</span>
              <div>{{ t('service.mockService.description.generateMockService') }}</div>
            </Radio>
            <Radio value="2">
              <span class="font-semibold">{{ t('service.mockService.options.associateMockService') }}</span>
              <div>{{ t('service.mockService.description.associateMockService') }}</div>
            </Radio>
          </RadioGroup>
          <Modal
            :title="t('service.mockService.modal.associateTitle')"
            :visible="createType === '2'"
            :okButtonProps="{text: t('service.mockService.actions.associate'), loading: relatedLoading, disabled: !selectedMockServiceId}"
            @ok="relatedMockService"
            @cancel="handleCloseModal">
            <Hints :text="t('service.mockService.hints.associateMockService')" />
            <Select
              v-model:value="selectedMockServiceId"
              class="w-full mt-2"
              :action="`${TESTER}/mock/service?projectId=${projectId}&fullTextSearch=true`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxlength="100"
              :placeholder="t('service.mockService.placeholder.selectMockService')"
              showSearch />
          </Modal>
        </div>
      </template>
    </Spin>
    <AsyncComponent :visible="createVisible">
      <CreateMock
        v-model:visible="createVisible"
        :serviceId="props.id"
        @reload="handelReload" />
    </AsyncComponent>
    <AsyncComponent :visible="authVisible">
      <AuthorizeModal
        v-model:visible="authVisible"
        enumKey="MockServicePermission"
        :appId="appInfo?.id"
        :listUrl="`${TESTER}/mock/service/${mockServiceInfo.id}/auth`"
        :delUrl="`${TESTER}/mock/service/auth`"
        :addUrl="`${TESTER}/mock/service/${mockServiceInfo.id}/auth`"
        :updateUrl="`${TESTER}/mock/service/auth`"
        :enabledUrl="`${TESTER}/mock/service/${mockServiceInfo.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/mock/service/${mockServiceInfo.id}/auth/status`"
        :onTips="t('service.mockService.modal.onTips')"
        :offTips="t('service.mockService.modal.offTips')"
        :title="t('service.mockService.modal.title')"
        @change="authFlagChange" />
    </AsyncComponent>
  </div>
</template>

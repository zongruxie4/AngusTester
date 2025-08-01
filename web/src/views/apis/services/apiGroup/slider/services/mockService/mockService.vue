<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { AsyncComponent, AuthorizeModal, Grid, Hints, Icon, IconCopy, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
import { Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { useRouter } from 'vue-router';

import { analysis, mock, services } from 'src/api/tester';

const CreateMock = defineAsyncComponent(() => import('./addMock.vue'));

interface Props {
  disabled: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const projectInfo = inject('projectInfo', ref({ id: '' }));
const router = useRouter();
const appInfo = ref(appContext.getAccessApp()) as Ref<Record<string, any>>;
const mockServiceInfo = ref();
const loading = ref();

const columns = [[
  { label: '名称', dataIndex: 'name' },
  { label: '节点', dataIndex: 'nodeName' },
  { label: '权限', dataIndex: 'auth' }
], [

  { label: '状态', dataIndex: 'status' },
  { label: '端口', dataIndex: 'servicePort' },
  { label: '服务地址', dataIndex: 'serviceDomainUrl' }
], [
  { label: '添加人', dataIndex: 'createdByName' },
  { label: '添加时间', dataIndex: 'createdDate' }
]];

const createType = ref();

const selectedMockServiceId = ref();
const createVisible = ref(false);
const createMockSeviceById = async () => {
  createVisible.value = true;
};

watch(() => createType.value, () => {
  if (createType.value === '1') {
    createMockSeviceById();
  }
});

watch(() => createVisible.value, () => {
  if (!createVisible.value) {
    createType.value = undefined;
  }
});

const handelRoload = () => {
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
  notification.success('关联成功');
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
    name: '接口数',
    key: 'apisNum',
    icon: 'icon-jiekoushu'
  },
  {
    name: '请求数',
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: '回推数',
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: '模拟异常数',
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: '成功数',
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: '异常数',
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

const cencelProjcetMock = async () => {
  loading.value = true;
  const [error] = await mock.cancelMockServiceAssoc(mockServiceInfo.value.id);
  loading.value = false;
  if (error) { return; }
  notification.success('取消关联成功');
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
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">取消关联</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cencelProjcetMock">取消关联</a>
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
              <div>{{ text ? '有权限控制' : '无权限控制' }}</div>
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
                  <span title="复制">
                    <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" />
                  </span>
                </div>
                <div v-if="mockServiceInfo?.serviceHostUrl">
                  <span>{{ mockServiceInfo.serviceHostUrl }}</span>
                  <span title="复制">
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
          <Hints text="Mock服务可快速生成并模拟您所依赖的API，使开发和测试能够并行进行，从而加快开发速度，实现更全面的测试，并更早地交付稳定的产品或应用。您可以通过“生成Mock服务”或“关联Mock服务”两种方式，为当前服务的接口创建对应的模拟接口。这些模拟接口将提供模拟数据，便于进行接口调试和测试。" />
        </div>
        <div class=" mt-3">
          <RadioGroup
            v-model:value="createType"
            :disabled="props.disabled"
            class="flex flex-col space-y-3.5">
            <Radio value="1">
              <span class="font-semibold">生成Mock服务</span>
              <div>基于当前服务接口创建新的Mock服务。创建后，您可以使用这些Mock接口进行数据模拟和状态测试，从而实现更高效的接口调试。</div>
            </Radio>
            <Radio value="2">
              <span class="font-semibold">关联Mock服务</span>
              <div>将当前服务与已有的Mock服务关联。关联后，系统会在关联的Mock服务中自动生成对应的Mock接口。这些接口可用于接口调试中的数据模拟和状态测试。</div>
            </Radio>
          </RadioGroup>
          <Modal
            title="关联Mock服务"
            :visible="createType === '2'"
            :okButtonProps="{text: '关联', loading: relatedLoading, disabled: !selectedMockServiceId}"
            @ok="relatedMockService"
            @cancel="handleCloseModal">
            <Hints text="当前服务关联已存在的Mock服务。" />
            <Select
              v-model:value="selectedMockServiceId"
              class="w-full mt-2"
              :action="`${TESTER}/mock/service?projectId=${projectInfo?.id}&fullTextSearch=true`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxlength="100"
              placeholder="请选择Mock服务"
              showSearch />
          </Modal>
        </div>
      </template>
    </Spin>
    <AsyncComponent :visible="createVisible">
      <CreateMock
        v-model:visible="createVisible"
        :serviceId="props.id"
        @reload="handelRoload" />
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
        onTips="开启权限控制后，需要手动授权后才会有相应操作权限。"
        offTips="无权限限制，账号中的所有用户都可以查看、操作，默认不开启权限控制。"
        title="Mock服务权限"
        @change="authFlagChange" />
    </AsyncComponent>
  </div>
</template>

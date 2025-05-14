<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, Ref, ref } from 'vue';
import { AsyncComponent, AuthorizeModal, Grid, Hints, Icon, IconCopy, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';
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
const appInfo = inject('appInfo') as Ref<Record<string, any>>;
const mockServiceInfo = ref();
const loading = ref();

const columns = [[
  { label: '名称', dataIndex: 'name' },
  { label: '服务地址', dataIndex: 'serviceDomainUrl' },
  { label: '端口', dataIndex: 'servicePort' },
  { label: '节点', dataIndex: 'nodeName' },
  { label: '状态', dataIndex: 'status' },
  { label: '权限', dataIndex: 'auth' },
  { label: '添加人', dataIndex: 'createdByName' },
  { label: '添加时间', dataIndex: 'createdDate' }
]];

const selectedMockServiceId = ref();
const createVisible = ref(false);
const createMockSeviceById = async () => {
  createVisible.value = true;
};

const handelRoload = () => {
  loadProjectMockService();
};
const relatedLoading = ref(false);
const relatedMockService = async () => {
  relatedLoading.value = true;
  const [error] = await mock.addServicesMockService(selectedMockServiceId.value, props.id);
  relatedLoading.value = false;
  if (error) {
    return;
  }
  notification.success('关联成功');
  loadProjectMockService();
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
  const [error] = await mock.cancelServiceMock(mockServiceInfo.value.id);
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

const createType = ref('1');

const gotoMock = () => {
  router.push(`/mockservice?sid=${mockServiceInfo.value.id}`);
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
        <div class="text-3 space-y-2 pt-1 leading-5">
          <div
            v-for="(item, index) in mockServiceCount"
            :key="index"
            class="flex items-center border border-border-divider rounded bg-gray-100 px-3.5 py-1.5">
            <Icon :icon="item.icon" class="mr-2" />
            <div class="flex flex-1 justify-between items-center">
              <span>{{ item.name }}</span>
              <span>{{ apisCount?.[item.key] }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="flex flex-col space-y-3.5">
          <RadioGroup v-model:value="createType">
            <Radio value="1">生成Mock服务</Radio>
            <Radio value="2">关联Mock服务</Radio>
          </RadioGroup>
          <template v-if="createType === '1'">
            <Hints text="基于当前服务生成Mock服务。" />
            <div class="flex justify-end">
              <Button
                size="small"
                type="primary"
                :disabled="props.disabled"
                @click="createMockSeviceById">
                生成
              </Button>
            </div>
          </template>
          <template v-else>
            <Hints text="当前服务关联已存在的Mock服务。" />
            <Select
              v-model:value="selectedMockServiceId"
              :action="`${TESTER}/mock/service/search?projectId=${projectInfo?.id}`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxlength="100"
              placeholder="请选择Mock服务"
              showSearch />
            <div class="flex justify-end">
              <Button
                :disabled="!selectedMockServiceId || props.disabled"
                size="small"
                type="primary"
                @click="relatedMockService">
                关联
              </Button>
            </div>
          </template>
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

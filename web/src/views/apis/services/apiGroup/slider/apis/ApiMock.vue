<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { Button, Divider, Radio, RadioGroup } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';

import { apis, mock } from 'src/api/tester';

interface Props {
  disabled:boolean;
  id: string; // api id
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const projectInfo = inject('projectInfo', ref({ id: '' }));

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
  { label: 'ID', dataIndex: 'id' },
  { label: '名称', dataIndex: 'name' },
  { label: '服务ID', dataIndex: 'mockServiceId' },
  { label: '服务名称', dataIndex: 'mockServiceName' },
  { label: '服务地址', dataIndex: 'mockServiceHostUrl' },
  { label: '服务人', dataIndex: 'createdBy' },
  { label: '添加时间', dataIndex: 'createdDate' }
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
  notification.success('生成Mock接口成功');
  loadMockApiInfo(props.id);
};

const selectedMockApiId = ref();

const relatedLoading = ref(false);
const relatedMockServiceApi = async () => {
  relatedLoading.value = true;
  const [error] = await mock.addApiMockServiceApi(selectedMockApiId.value, props.id);
  relatedLoading.value = false;
  if (error) {
    return;
  }
  mockApisId.value = selectedMockApiId.value;
  notification.success('关联成功');
  loadMockApiInfo(props.id);
};

const mockServiceCount = [
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

const cencelProjcetMock = async () => {
  loading.value = true;
  const [error] = await mock.cancelMockApi([mockApiInfo.value.id]);
  loading.value = false;
  if (error) { return; }
  notification.success('取消关联成功');
  mockApiInfo.value = undefined;
  mockServiceId.value = undefined;
};

const loadApiInfo = async (_id) => {
  const [error, { data }] = await apis.getDetail(_id);
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
    if (!mockApiInfo.value) {
      loadApiInfo(newValue);
    }
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
                <a class="whitespace-nowrap text-text-disabled cursor-not-allowed ml-2">取消关联</a>
              </template>
              <template v-else>
                <a class="whitespace-nowrap text-text-link ml-2" @click="cencelProjcetMock">取消关联</a>
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
                  <span title="复制"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="mockApiInfo.mockServiceDomainUrl" /></span>
                </div>
                <div v-if="text" class="flex items-start">
                  <span>{{ text }}</span>
                  <span title="复制"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" /></span>
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
          <Hints text="生成或关联Mock接口请先选择Mock服务。" />
          <RadioGroup v-model:value="createType">
            <Radio value="1">生成Mock接口</Radio>
            <Radio value="2">关联Mock接口</Radio>
          </RadioGroup>
          <Select
            v-model:value="mockServiceId"
            :action="`${TESTER}/mock/service?projectId=${projectInfo?.id}&fullTextSearch=true`"
            :fieldNames="{label:'name',value:'id'}"
            :maxlength="100"
            placeholder="请选择Mock服务"
            showSearch />
          <template v-if="createType === '1'">
            <Hints text="基于当前接口生成Mock接口。" />
            <Input v-model:value="summary" placeholder="请输入对应Mock接口名称，不指定时默认使用当前接口名称" />
            <div class="flex justify-end">
              <Button
                :disabled="!mockServiceId || props.disabled"
                :loading="createApiLoading"
                size="small"
                type="primary"
                @click="createMockApiById">
                确定
              </Button>
            </div>
          </template>
          <template v-else>
            <Hints text="关联Mock服务下已存在的接口。" />
            <Select
              v-model:value="selectedMockApiId"
              :disabled="!mockServiceId"
              :action="`${TESTER}/mock/apis`"
              :params="mockApiParams"
              :fieldNames="{label:'summary',value:'id'}"
              :format="format"
              :maxlength="100"
              allowClear
              placeholder="请选择Mock接口"
              showSearch />
            <div class="flex justify-end">
              <Button
                :disabled="!mockServiceId || !selectedMockApiId || props.disabled"
                size="small"
                type="primary"
                @click="relatedMockServiceApi">
                确定
              </Button>
            </div>
          </template>
        </div>
      </template>
    </Spin>
  </div>
</template>

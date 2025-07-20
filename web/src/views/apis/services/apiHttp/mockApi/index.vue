<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { Grid, Hints, Icon, IconCopy, Input, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
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
  { label: '接口名称', dataIndex: 'name' },
  { label: '模拟服务名称', dataIndex: 'mockServiceName' }
], [
  { label: '方法', dataIndex: 'method' },
  { label: '服务地址', dataIndex: 'mockServiceHostUrl' }
],
[
  { label: '添加人', dataIndex: 'createdByName' },
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
          <Hints text="Mock接口可快速生成并模拟您所依赖的API，使开发和测试能够并行进行，从而加快开发速度，实现更全面的测试，并更早地交付稳定的产品或应用。您可以通过“生成Mock接口”或“关联Mock接口”两种方式，为当前接口创建对应的模拟接口。模拟接口将提供模拟数据，便于进行接口调试和测试。" />
        </div>
        <div class=" mt-4">
          <RadioGroup
            v-model:value="createType"
            :disabled="props.disabled"
            class="flex flex-col space-y-3.5">
            <Radio value="1">
              <span class="font-semibold">生成Mock接口</span>
              <div>基于当前接口创建新的模拟版本。创建后，您可以使用Mock接口进行数据模拟和状态测试，从而实现更高效的接口调试。</div>
            </Radio>
            <Radio value="2">
              <span class="font-semibold">关联Mock接口</span>
              <div>将当前接口与已有的Mock服务接口关联。关联后可以在接口调试中进行数据模拟和状态测试。</div>
            </Radio>
          </RadioGroup>
          <Modal
            :visible="!!createType"
            :title="createType === '1' ? '生成Mock接口' : '关联Mock接口'"
            :okButtonProps="{loading: createApiLoading || relatedLoading, disabled: !mockServiceId || (createType === '2' && !selectedMockApiId)}"
            @ok="onOk"
            @cancel="cancel">
            <Select
              v-model:value="mockServiceId"
              :action="`${TESTER}/mock/service/search?projectId=${projectInfo?.id}`"
              :fieldNames="{label:'name',value:'id'}"
              :maxlength="100"
              class="w-full mb-4"
              placeholder="请选择Mock服务"
              showSearch />
            <template v-if="createType === '1'">
              <Hints text="基于当前接口生成Mock接口。" class="mb-2" />
              <Input v-model:value="summary" placeholder="请输入对应Mock接口名称，不指定时默认使用当前接口名称" />
            </template>
            <template v-else>
              <Hints text="关联Mock服务下已存在的接口。" class="mb-2" />
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
                placeholder="请选择Mock接口"
                showSearch />
            </template>
          </Modal>
        </div>
      </template>
    </Spin>
  </div>
</template>

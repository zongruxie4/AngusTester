<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Input, notification, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, utils } from '@xcan-angus/tools';
import { func } from '@/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

const Table = defineAsyncComponent(() => import('./table.vue'));

const activeKey = ref<'PLAN' | 'CASE'>('PLAN');
const loading = ref(false);
const inputValue = ref<string>();
const refreshNotify = ref<string>();

const inputChange = debounce(duration.search, (event) => {
  inputValue.value = event.target.value;
});

const recoverAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await func.backAllTrash(params);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部还原成功');
  refreshNotify.value = utils.uuid();
};

const deleteAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await func.deleteAllTrash(params);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部删除成功');
  refreshNotify.value = utils.uuid();
};

const toRefresh = () => {
  refreshNotify.value = utils.uuid();
};

const serviceParams = computed(() => {
  const params :{
    targetType:'PLAN';
    targetName?:string;
  } = {
    targetType: 'PLAN'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

const apiParams = computed(() => {
  const params :{
    targetType:'CASE';
    targetName?:string;
  } = {
    targetType: 'CASE'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

const buttonDisabled = computed(() => {
  return !tableDataMap.value[activeKey.value]?.length;
});

const tableDataMap = ref({});
const handleTableChange = (listData, key) => {
  tableDataMap.value[key] = listData;
};

</script>
<template>
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <div class="flex items-center justify-between mb-1">
      <div class="flex items-center">
        <Input
          :value="inputValue"
          :allowClear="true"
          :maxlength="200"
          trim
          placeholder="请输入查询关键字"
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
          </template>
        </Input>
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon icon="icon-tishi1" class="text-3.5 text-tips" />
          <span>只允许管理员、删除人还原和删除回收站。</span>
        </div>
      </div>
      <div class="space-x-2.5">
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          @click="recoverAll">
          <Icon icon="icon-zhongzhi" class="text-3.5 mr-1" />
          <span class>全部还原</span>
        </Button>
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          danger
          @click="deleteAll">
          <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
          <span class>全部删除</span>
        </Button>
        <Button
          size="small"
          type="default"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span class>刷新</span>
        </Button>
      </div>
    </div>

    <Tabs v-model:activeKey="activeKey">
      <TabPane key="PLAN" tab="计划">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="serviceParams"
          @tableChange="handleTableChange($event, 'PLAN')" />
      </TabPane>

      <TabPane key="CASE" tab="用例">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="apiParams"
          @tableChange="handleTableChange($event, 'CASE')" />
      </TabPane>
    </Tabs>
  </Spin>
</template>

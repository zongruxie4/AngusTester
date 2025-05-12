<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, inject, Ref, onMounted, watch } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Input, notification, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, utils } from '@xcan-angus/tools';
import { task } from '@/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  refreshNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  refreshNotify: undefined
});
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTaskStatistics: true }));

const Table = defineAsyncComponent(() => import('./table.vue'));

const activeKey = ref<'TASK_SPRINT' | 'TASK'>('TASK_SPRINT');
const loading = ref(false);
const inputValue = ref<string>();
const notify = ref<string>();

const inputChange = debounce(duration.search, () => {
  notify.value = utils.uuid();
});

const recoverAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await task.backTrashAllTask(params);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部还原成功');
};

const deleteAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await task.deleteAllTrash(params);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部删除成功');
  notify.value = utils.uuid();
};

const toRefresh = () => {
  notify.value = utils.uuid();
};

const serviceParams = computed(() => {
  const params :{
    targetType:'TASK_SPRINT';
    targetName?:string;
  } = {
    targetType: 'TASK_SPRINT'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

onMounted(() => {
  watch(() => proTypeShowMap.value.showSprint, (newValue) => {
    if (!newValue) {
      activeKey.value = 'TASK';
    }
  }, { immediate: true });
});

const apiParams = computed(() => {
  const params :{
    targetType:'TASK';
    targetName?:string;
  } = {
    targetType: 'TASK'
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
const handleChange = (listData, key) => {
  tableDataMap.value[key] = listData;
};
</script>
<template>
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <div class="flex items-center justify-between mb-1">
      <div class="flex items-center">
        <Input
          v-model:value="inputValue"
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
      <TabPane
        v-if="proTypeShowMap.showSprint"
        key="TASK_SPRINT"
        tab="迭代">
        <Table
          v-model:spinning="loading"
          :notify="notify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="serviceParams"
          @listChange="handleChange($event, 'TASK_SPRINT')" />
      </TabPane>

      <TabPane key="TASK" tab="任务">
        <Table
          v-model:spinning="loading"
          :notify="notify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="apiParams"
          @listChange="handleChange($event, 'TASK')" />
      </TabPane>
    </Tabs>
  </Spin>
</template>

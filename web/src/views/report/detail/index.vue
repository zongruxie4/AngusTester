<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';
// import { useRoute } from 'vue-router';
import { ActivityTimeline, Colon, Modal } from '@xcan-angus/vue-ui';
import { TabPane, Tabs } from 'ant-design-vue';

import { reportMenus } from '@/views/report/add/config';

interface Props {
  visible: boolean;
  reportId: string;
  permissions: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  permissions: () => []
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const GenerateTime = defineAsyncComponent(() => import('@/views/report/detail/generateTime/index.vue'));
const GenerateRecord = defineAsyncComponent(() => import('@/views/report/detail/generateRecord/index.vue'));
const Basic = defineAsyncComponent(() => import('@/views/report/add/basic/index.vue'));
const Content = defineAsyncComponent(() => import('@/views/report/detail/content/index.vue'));

// const route = useRoute();

const report = ref<{[key: string]: any}>({});
const reportId = ref();

const reportTypeObj = computed(() => {
  const targetGroup = reportMenus.find(group => group.label === report.value?.category?.message);
  return targetGroup?.children.find(menu => menu.key === report.value?.template?.value);
});

const loadReportDetail = async () => {
  const [error, { data }] = await http.get(`${TESTER}/report/${reportId.value}`);
  if (error) {
    return;
  }
  report.value = data;
};

const cancel = () => {
  emits('update:visible', false);
};

onMounted(() => {
  // const id = route.params.id;
  watch(() => props.visible, () => {
    if (props.reportId) {
      reportId.value = props.reportId;
      loadReportDetail();
    }
  }, {
    immediate: true
  });
});

const activityType = ['REPORT'];
</script>
<template>
  <Modal
    :visible="props.visible"
    title="查看报告"
    :width="1000"
    :footer="null"
    @cancel="cancel">
    <div class="text-3 h-full flex flex-col" style="height: 80vh">
      <div class="border-blue-border border rounded bg-blue-bg4  px-6 py-4 flex items-center space-x-7">
        <img src="../add/image/default.png" class="rounded-full w-15 h-15" />
        <div class="space-y-2 flex-1 min-w-0">
          <div class="font-semibold text-3.5">{{ report?.template?.message }}</div>
          <div class="flex justify-between">
            <div class="text-3 flex-1">分类： {{ report.category?.message }}</div>
            <div class="text-3 flex-1">报告人： {{ report.createdByName }}</div>
          </div>
          <div class="text-3">描述： {{ reportTypeObj?.description }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-4 mt-5">
        <div class="flex-1/2 inline-flex min-w-0">
          <div class="w-12 px-1 text-right h-7" style="line-height: 28px; ">
            名称
          </div>
          <Colon class="h-7" style="line-height: 28px;" />
          <div class="px-1" style="line-height: 28px;">
            {{ report.name }}
          </div>
        </div>
        <div class="flex-1/2 inline-flex min-w-0">
          <div class="w-12 px-1 text-right h-7" style="line-height: 28px;">
            版本号
          </div>
          <Colon class="h-7" style="line-height: 28px;" />
          <div class="px-1" style="line-height: 28px;">
            {{ report.version }}
          </div>
        </div>
      </div>
      <div class="flex leading-7">
        <div class="w-12 px-1 text-right h-7" style="line-height: 28px;">
          描述
        </div>
        <Colon class="h-7" style="line-height: 28px;" />
        <div class="px-1">
          {{ report.description || '--' }}
        </div>
      </div>

      <Tabs size="small" class="mt-4 flex-1">
        <TabPane key="createdDate" tab="添加时间">
          <GenerateTime :createTimeSetting="report.createTimeSetting" />
        </TabPane>
        <TabPane key="basic" tab="基本信息">
          <Basic
            :basicInfoSetting="report.basicInfoSetting"
            :viewType="true"
            class="overflow-y-auto h-full" />
        </TabPane>
        <TabPane key="content" tab="内容">
          <Content
            class="overflow-y-auto h-full"
            :contentSetting="report.contentSetting?.filter"
            :projectId="report.projectId"
            :projectName="report.projectId"
            :template="report.template?.value" />
        </TabPane>
        <TabPane key="record" tab="报告记录">
          <GenerateRecord
            class="overflow-y-auto h-full"
            :projectId="report.projectId"
            :reportId="reportId"
            :permissions="props.permissions" />
        </TabPane>
        <TabPane key="activity" tab="活动">
          <ActivityTimeline
            :id="reportId"
            class="w-120"
            :types="activityType"
            :showUserName="false" />
        </TabPane>
      </Tabs>
    </div>
  </Modal>
</template>

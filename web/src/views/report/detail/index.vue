<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ActivityTimeline, Colon, Modal } from '@xcan-angus/vue-ui';
import { TabPane, Tabs } from 'ant-design-vue';
import { report as reportApi } from '@/api/tester';

import { reportMenus } from '@/views/report/add/config';

const { t } = useI18n();

// Component props definition
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

// Async components
const GenerateTime = defineAsyncComponent(() => import('@/views/report/detail/GenerateTime.vue'));
const GenerateRecord = defineAsyncComponent(() => import('@/views/report/detail/GenerateRecord.vue'));
const Basic = defineAsyncComponent(() => import('@/views/report/add/Basic.vue'));
const Content = defineAsyncComponent(() => import('@/views/report/detail/Content.vue'));

// Reactive variables
const report = ref<{[key: string]: any}>({});
const reportIdRef = ref(); // Renamed from reportId to avoid conflict with prop

/**
 * Compute report type object based on report category and template
 * @returns Report type object
 */
const reportTypeObj = computed(() => {
  const targetGroup = reportMenus.find(group => group.label === report.value?.category?.message);
  return targetGroup?.children.find(menu => menu.key === report.value?.template?.value);
});

/**
 * Load report detail information
 * Fetches report data based on report ID
 */
const loadReportDetail = async () => {
  const [error, { data }] = await reportApi.getReportDetail(reportIdRef.value);
  if (error) {
    return;
  }
  report.value = data;
};

/**
 * Cancel and close the modal
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Lifecycle hook - Initialize component
 * Watch for visibility changes and load report detail
 */
onMounted(() => {
  watch(() => props.visible, () => {
    if (props.reportId) {
      reportIdRef.value = props.reportId;
      loadReportDetail();
    }
  }, {
    immediate: true
  });
});

// Activity type configuration
const activityType = ['REPORT'];
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('reportHome.reportDetail.title')"
    :width="1000"
    :footer="null"
    @cancel="cancel">
    <div class="text-3 h-full flex flex-col" style="height: 80vh">
      <div class="border-blue-border border rounded bg-blue-bg4  px-6 py-4 flex items-center space-x-7">
        <img src="../../../assets/images/default.png" class="rounded-full w-15 h-15" />
        <div class="space-y-2 flex-1 min-w-0">
          <div class="font-semibold text-3.5">{{ report?.template?.message }}</div>
          <div class="flex justify-between">
            <div class="text-3 flex-1">{{ t('common.category') }}： {{ report.category?.message }}</div>
            <div class="text-3 flex-1">{{ t('common.creator') }}： {{ report.createdByName }}</div>
          </div>
          <div class="text-3">{{ t('common.description') }}： {{ reportTypeObj?.description }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-4 mt-5">
        <div class="flex-1/2 inline-flex min-w-0">
          <div class="w-12 px-1 text-right h-7" style="line-height: 28px; ">
            {{ t('common.name') }}
          </div>
          <Colon class="h-7" style="line-height: 28px;" />
          <div class="px-1" style="line-height: 28px;">
            {{ report.name }}
          </div>
        </div>
        <div class="flex-1/2 inline-flex min-w-0">
          <div class="w-12 px-1 text-right h-7" style="line-height: 28px;">
            {{ t('common.version') }}
          </div>
          <Colon class="h-7" style="line-height: 28px;" />
          <div class="px-1" style="line-height: 28px;">
            {{ report.version }}
          </div>
        </div>
      </div>
      <div class="flex leading-7">
        <div class="w-12 px-1 text-right h-7" style="line-height: 28px;">
          {{ t('common.description') }}
        </div>
        <Colon class="h-7" style="line-height: 28px;" />
        <div class="px-1">
          {{ report.description || '--' }}
        </div>
      </div>

      <Tabs size="small" class="mt-4 flex-1">
        <TabPane key="createdDate" :tab="t('common.createdDate')">
          <GenerateTime :createTimeSetting="report.createTimeSetting" />
        </TabPane>
        <TabPane key="basic" :tab="t('common.basicInfo')">
          <Basic
            :basicInfoSetting="report.basicInfoSetting"
            :viewType="true"
            class="overflow-y-auto h-full" />
        </TabPane>
        <TabPane key="content" :tab="t('reportHome.reportDetail.tabs.content')">
          <Content
            class="overflow-y-auto h-full"
            :contentSetting="report.contentSetting?.filter"
            :projectId="report.projectId"
            :projectName="report.projectId"
            :targetName="report.targetName"
            :template="report.template?.value" />
        </TabPane>
        <TabPane key="record" :tab="t('reportHome.reportDetail.tabs.record')">
          <GenerateRecord
            class="overflow-y-auto h-full"
            :projectId="report.projectId"
            :reportId="reportIdRef"
            :permissions="props.permissions" />
        </TabPane>
        <TabPane key="activity" :tab="t('reportHome.reportDetail.tabs.activity')">
          <ActivityTimeline
            :id="reportIdRef"
            class="w-120"
            :types="activityType"
            :showUserName="false" />
        </TabPane>
      </Tabs>
    </div>
  </Modal>
</template>

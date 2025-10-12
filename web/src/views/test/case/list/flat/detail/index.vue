<script lang="ts" setup>
import { defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { duration } from '@xcan-angus/infra';
import elementResizeDetector, { Erd } from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';

import {
  bigApisInfoColumns,
  bigDateInfoColumns,
  bigPeopleInfoColumns,
  bigReviewInfoColumns,
  bigTestInfoColumns,
  minApisInfoColumns,
  minDateInfoColumns,
  minPeopleInfoColumns,
  minReviewInfoColumns,
  minTestInfoColumns
} from './config';

// Import info components
const BasicInfo = defineAsyncComponent(() => import('./info/Basic.vue'));
const WorkloadInfo = defineAsyncComponent(() => import('./info/Workload.vue'));
const PreconditionInfo = defineAsyncComponent(() => import('./info/Precondition.vue'));
const StepsInfo = defineAsyncComponent(() => import('./info/Steps.vue'));
const DescriptionInfo = defineAsyncComponent(() => import('./info/Description.vue'));

const PersonnelInfo = defineAsyncComponent(() => import('./info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('./info/Date.vue'));
const ReviewInfo = defineAsyncComponent(() => import('./info/Review.vue'));
const TestInfo = defineAsyncComponent(() => import('./info/Test.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('./info/Attachment.vue'));

interface Props {
  caseDetail: CaseDetail;
  actionAuth: CaseActionAuth[]
}

withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' } as unknown as CaseDetail),
  actionAuth: () => ([])
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'editSuccess'):void;
  (e: 'loadingChange', value:boolean):void;
}>();

const projectId = inject<Ref<string>>('projectId', ref(''));

const isWideLayout = ref(true);
const detailContainerRef = ref();
let resizeDetector:Erd;
const handleContainerResize = debounce(duration.resize, (element) => {
  isWideLayout.value = element.offsetWidth >= 960;
});

export type GridColumns = {
  label: string;
  dataIndex: string;
  colon?:boolean;
  customRender?: ({ text }: { text: any; }) => string;
}

const peopleInfoColumns = ref<GridColumns[][]>(bigPeopleInfoColumns);
const dateInfoColumns = ref<GridColumns[][]>(bigDateInfoColumns);
const reviewInfoColumns = ref<GridColumns[][]>(bigReviewInfoColumns);
const testInfoColumns = ref<GridColumns[][]>(bigTestInfoColumns);
const apisInfoColumns = ref<GridColumns[][]>(bigApisInfoColumns);

/**
 * <p>Handles case data change events from child components.</p>
 * <p>Forwards the change event to parent components.</p>
 */
const handleCaseDataChange = () => {
  emit('editSuccess');
};

watch(() => isWideLayout.value, (isWide) => {
  peopleInfoColumns.value = isWide ? bigPeopleInfoColumns : minPeopleInfoColumns;
  dateInfoColumns.value = isWide ? bigDateInfoColumns : minDateInfoColumns;
  reviewInfoColumns.value = isWide ? bigReviewInfoColumns : minReviewInfoColumns;
  testInfoColumns.value = isWide ? bigTestInfoColumns : minTestInfoColumns;
  apisInfoColumns.value = isWide ? bigApisInfoColumns : minApisInfoColumns;
}, {
  immediate: true
});

onMounted(() => {
  resizeDetector = elementResizeDetector();
  resizeDetector.listenTo(detailContainerRef.value, handleContainerResize);
});

onBeforeUnmount(() => {
  resizeDetector.removeListener(detailContainerRef.value, handleContainerResize);
});
</script>
<template>
  <div
    ref="detailContainerRef"
    :class="isWideLayout?'flex pr-2':'pr-3.5'"
    class="overflow-y-auto h-full">
    <div class="flex-1 space-y-6">
      <!-- Main Task Info Section -->
      <div class="space-y-4">
        <BasicInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <WorkloadInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </div>

      <!-- Metrics Section - Personnel, Date, Review and Test Info for small layout -->
      <template v-if="!isWideLayout">
        <PersonnelInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          :columns="peopleInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <DateInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          :columns="dateInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <ReviewInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          :columns="reviewInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <TestInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          :columns="testInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </template>

      <!-- Additional Info Section -->
      <div class="space-y-4">
        <PreconditionInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <StepsInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <DescriptionInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </div>

      <template v-if="!isWideLayout">
        <AttachmentInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </template>
    </div>

    <div v-if="isWideLayout" class="w-75 flex-none ml-2 space-y-4">
      <PersonnelInfo
        :id="caseDetail.id"
        :dataSource="caseDetail"
        :projectId="projectId"
        :actionAuth="actionAuth"
        :columns="peopleInfoColumns"
        @change="handleCaseDataChange"
        @loadingChange="emit('loadingChange', $event)" />

      <DateInfo
        :id="caseDetail.id"
        :dataSource="caseDetail"
        :projectId="projectId"
        :actionAuth="actionAuth"
        :columns="dateInfoColumns"
        @change="handleCaseDataChange"
        @loadingChange="emit('loadingChange', $event)" />

      <ReviewInfo
        :id="caseDetail.id"
        :dataSource="caseDetail"
        :projectId="projectId"
        :actionAuth="actionAuth"
        :columns="reviewInfoColumns"
        @change="handleCaseDataChange"
        @loadingChange="emit('loadingChange', $event)" />

      <TestInfo
        :id="caseDetail.id"
        :dataSource="caseDetail"
        :projectId="projectId"
        :actionAuth="actionAuth"
        :columns="testInfoColumns"
        @change="handleCaseDataChange"
        @loadingChange="emit('loadingChange', $event)" />

      <AttachmentInfo
        :id="caseDetail.id"
        :dataSource="caseDetail"
        :projectId="projectId"
        :actionAuth="actionAuth"
        @change="handleCaseDataChange"
        @loadingChange="emit('loadingChange', $event)" />
    </div>
  </div>
</template>
<style scoped>
</style>

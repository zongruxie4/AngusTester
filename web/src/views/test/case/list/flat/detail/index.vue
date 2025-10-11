<script lang="ts" setup>
import { defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { duration, appContext } from '@xcan-angus/infra';
import elementResizeDetector, { Erd } from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

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
const PersonnelInfo = defineAsyncComponent(() => import('./info/Personnel.vue'));
const DateInfo = defineAsyncComponent(() => import('./info/Date.vue'));
const ReviewInfo = defineAsyncComponent(() => import('./info/Review.vue'));
const TestInfo = defineAsyncComponent(() => import('./info/Test.vue'));
const DescriptionInfo = defineAsyncComponent(() => import('./info/Description.vue'));
const AttachmentInfo = defineAsyncComponent(() => import('./info/Attachment.vue'));
const PreconditionInfo = defineAsyncComponent(() => import('./info/Precondition.vue'));
const StepsInfo = defineAsyncComponent(() => import('./info/Steps.vue'));

interface Props {
  caseDetail: CaseDetail;
  actionAuth: {[key: string]: any}
}

withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' } as unknown as CaseDetail),
  actionAuth: () => ({})
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'editSuccess'):void;
  (e: 'loadingChange', value:boolean):void;
}>();

const { t } = useI18n();

const userInfo = ref(appContext.getUser());
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

const infoExpand = ref(true);

/**
 * <p>Handles case data change events from child components.</p>
 * <p>Forwards the change event to parent components.</p>
 * @param data - Partial case information that has been changed
 */
const handleCaseDataChange = (data: Partial<CaseDetail>) => {
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
    <div class="flex-1">
      <!-- Main Task Info Section -->
      <div class="space-y-4">
        <Toggle
          v-model:open="infoExpand"
          :title="t('common.basicInfo')">
          <BasicInfo
            :id="caseDetail.id"
            :dataSource="caseDetail"
            :projectId="projectId"
            :userInfo="userInfo"
            :taskId="caseDetail.id"
            :actionAuth="actionAuth"
            @change="handleCaseDataChange"
            @loadingChange="emit('loadingChange', $event)" />
        </Toggle>
      </div>

      <!-- Metrics Section - Personnel, Date, Review and Test Info for small layout -->
      <template v-if="!isWideLayout">
        <Toggle
          :title="t('common.personnel')"
          class="mt-3.5">
          <PersonnelInfo
            :id="caseDetail.id"
            :dataSource="caseDetail"
            :projectId="projectId"
            :userInfo="userInfo"
            :taskId="caseDetail.id"
            :actionAuth="actionAuth"
            :columns="peopleInfoColumns"
            @change="handleCaseDataChange"
            @loadingChange="emit('loadingChange', $event)" />
        </Toggle>

        <Toggle
          :title="t('common.date')"
          class="mt-3.5">
          <DateInfo
            :id="caseDetail.id"
            :dataSource="caseDetail"
            :projectId="projectId"
            :userInfo="userInfo"
            :taskId="caseDetail.id"
            :actionAuth="actionAuth"
            :columns="dateInfoColumns"
            @change="handleCaseDataChange"
            @loadingChange="emit('loadingChange', $event)" />
        </Toggle>

        <Toggle
          :title="t('common.reviewInfo')"
          class="mt-3.5">
          <ReviewInfo
            :id="caseDetail.id"
            :dataSource="caseDetail"
            :projectId="projectId"
            :userInfo="userInfo"
            :taskId="caseDetail.id"
            :actionAuth="actionAuth"
            :columns="reviewInfoColumns"
            @change="handleCaseDataChange"
            @loadingChange="emit('loadingChange', $event)" />
        </Toggle>

        <Toggle
          :title="t('common.testInfo')"
          class="mt-3.5">
          <TestInfo
            :id="caseDetail.id"
            :dataSource="caseDetail"
            :projectId="projectId"
            :userInfo="userInfo"
            :taskId="caseDetail.id"
            :actionAuth="actionAuth"
            :columns="testInfoColumns"
            @change="handleCaseDataChange"
            @loadingChange="emit('loadingChange', $event)" />
        </Toggle>
      </template>

      <!-- Additional Info Section -->
      <div class="space-y-4">
        <PreconditionInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <StepsInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />

        <DescriptionInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </div>

      <template v-if="!isWideLayout">
        <AttachmentInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </template>
    </div>

    <div v-if="isWideLayout" class="w-75 flex-none ml-2">
      <Toggle :title="t('common.personnel')">
        <PersonnelInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          :columns="peopleInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </Toggle>

      <Toggle
        :title="t('common.date')"
        class="mt-3.5">
        <DateInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          :columns="dateInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </Toggle>

      <Toggle
        :title="t('common.reviewInfo')"
        class="mt-3.5">
        <ReviewInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          :columns="reviewInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </Toggle>

      <Toggle
        :title="t('common.testInfo')"
        class="mt-3.5">
        <TestInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          :columns="testInfoColumns"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </Toggle>

      <Toggle
        :title="t('common.attachment')"
        class="mt-3.5">
        <AttachmentInfo
          :id="caseDetail.id"
          :dataSource="caseDetail"
          :projectId="projectId"
          :userInfo="userInfo"
          :taskId="caseDetail.id"
          :actionAuth="actionAuth"
          @change="handleCaseDataChange"
          @loadingChange="emit('loadingChange', $event)" />
      </Toggle>
    </div>
  </div>
</template>
<style scoped>
</style>

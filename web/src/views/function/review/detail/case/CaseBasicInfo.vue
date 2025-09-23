<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, ReviewStatus } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';

import TaskPriority from '@/components/TaskPriority/index.vue';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any}
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const infoColumns = computed(() => [
  [
    { label: t('caseReview.comp.caseBasicInfo.name'), dataIndex: 'name' },
    { label: t('caseReview.comp.caseBasicInfo.id'), dataIndex: 'id' },
    { label: t('caseReview.comp.caseBasicInfo.code'), dataIndex: 'code' },
    {
      label: t('caseReview.comp.caseBasicInfo.reviewStatus'),
      dataIndex: 'reviewStatus'
    },
    {
      label: t('caseReview.comp.caseBasicInfo.version'),
      dataIndex: 'version'
    },
    {
      label: t('caseReview.comp.caseBasicInfo.softwareVersion'),
      dataIndex: 'softwareVersion'
    },
    { label: t('caseReview.comp.caseBasicInfo.priority'), dataIndex: 'priority' },
    { label: t('caseReview.comp.caseBasicInfo.tags'), dataIndex: 'tags' },

    { label: t('caseReview.comp.caseBasicInfo.planName'), dataIndex: 'planName' },
    { label: t('caseReview.comp.caseBasicInfo.moduleName'), dataIndex: 'moduleName' },
    {
      label: t('caseReview.comp.caseBasicInfo.testResult'),
      dataIndex: 'testResult'
    },
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('caseReview.comp.caseBasicInfo.evalWorkload') : t('caseReview.comp.caseBasicInfo.evalWorkload'), dataIndex: 'evalWorkload', customRender: ({ text }) => text || '--' },
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('caseReview.comp.caseBasicInfo.actualWorkload') : t('caseReview.comp.caseBasicInfo.actualWorkload'), dataIndex: 'actualWorkload', customRender: ({ text }) => text || '--' },
    { label: t('caseReview.comp.caseBasicInfo.unplannedCase'), dataIndex: 'unplanned', customRender: ({ text }) => text ? t('status.yes') : t('status.no') }
  ]
]);

</script>
<template>
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div class="flex items-center mb-4">
      <Icon icon="icon-jibenxinxi" class="text-blue-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.caseBasicInfo.title') }}
      </h3>
    </div>
    <Grid
      :columns="infoColumns"
      :dataSource="caseInfo"
      :spacing="24"
      :marginBottom="6"
      labelSpacing="12px"
      font-size="14px"
      class="case-basic-info-grid">
      <template #priority="{text}">
        <TaskPriority :value="text" />
      </template>
      <template #tags="{text}">
        <div class="flex flex-wrap gap-2">
          <Tag
            v-for="(tag,index) in (text || [])"
            :key="tag.id"
            class="px-3 py-1 text-sm font-medium bg-blue-50 text-blue-700 border-blue-200 rounded-full">
            {{ tag.name }}
          </Tag>
          <span v-if="!text?.length" class="text-gray-400 text-sm">--</span>
        </div>
      </template>
      <template #evalWorkload="{text}">
        {{ text || '--' }}
      </template>
      <template #actualWorkload="{text}">
        {{ text || '--' }}
      </template>
      <template #planName="{text}">
        <div class="flex items-center text-gray-700">
          <Icon icon="icon-jihua" class="mr-2 text-blue-500" />
          <span class="font-medium">{{ text }}</span>
        </div>
      </template>
      <template #moduleName="{text}">
        <div v-if="text" class="flex items-center">
          <Tag class="px-3 py-1 bg-gray-100 text-gray-700 border-gray-200 rounded-full flex items-center">
            <Icon icon="icon-mokuai" class="mr-2 text-gray-500" />
            <span class="text-sm">{{ text }}</span>
          </Tag>
        </div>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>
      <template #reviewStatus="{text}">
        <template v-if="text">
          <ReviewStatus :value="text" />
        </template>
        <template v-else>
          --
        </template>
      </template>
      <template #testResult="{text}">
        <div class="flex items-center">
          <TestResult :value="text" />
          <div
            v-if="caseInfo?.overdue"
            class="ml-3 px-2 py-1 bg-red-100 text-red-700 text-xs font-medium rounded-full border border-red-200">
            {{ t('caseReview.comp.caseBasicInfo.overdue') }}
          </div>
        </div>
      </template>
      <template #version="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>
      <template #softwareVersion="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>
    </Grid>
  </div>
</template>

<style scoped>
:deep(.case-basic-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>

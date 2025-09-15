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

const infoColumns = computed<GridColumns[][]>(() => [
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
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('caseReview.comp.caseBasicInfo.evalStoryPoint') : t('caseReview.comp.caseBasicInfo.evalWorkload'), dataIndex: 'evalWorkload', customRender: ({ text }) => text || '--' },
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('caseReview.comp.caseBasicInfo.actualStoryPoint') : t('caseReview.comp.caseBasicInfo.actualWorkload'), dataIndex: 'actualWorkload', customRender: ({ text }) => text || '--' },
    { label: t('caseReview.comp.caseBasicInfo.unplannedCase'), dataIndex: 'unplanned', customRender: ({ text }) => text ? t('status.yes') : t('status.no') }
  ]
]);

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      {{ t('caseReview.comp.caseBasicInfo.title') }}
    </div>
    <Grid
      :columns="infoColumns"
      :dataSource="caseInfo"
      :spacing="20"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="">
      <template #priority="{text}">
        <TaskPriority :value="text" />
      </template>
      <template #tags="{text}">
        <Tag
          v-for="(tag,index) in (text || [])"
          :key="tag.id"
          :class="{'min-w-17.5':!tag.name,'last-child':index===text.length-1}"
          color="rgba(252, 253, 255, 1)"
          class="text-3 px-2 font-normal text-theme-sub-content mr-2 h-6 py-1 border-border-divider">
          {{ tag.name }}
        </Tag>
        <template v-if="!text?.length">--</template>
      </template>
      <template #evalWorkload="{text}">
        {{ text || '--' }}
      </template>
      <template #actualWorkload="{text}">
        {{ text || '--' }}
      </template>
      <template #planName="{text}">
        <span>
          <Icon icon="icon-jihua" class="mr-1.25 flex-none -mt-0.25" />{{ text }}
        </span>
      </template>
      <template #moduleName="{text}">
        <template v-if="!text">
          --
        </template>
        <div v-else class="-mt-1 flex">
          <Tag
            class="px-0 py-1 font-normal text-theme-content rounded bg-white flex border-none">
            <Icon icon="icon-mokuai" class="mr-1.25 flex-none mt-0.5" />
            <div class="flex-1  whitespace-break-spaces break-all leading-4">{{ text }}</div>
          </Tag>
        </div>
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
            class="border border-status-error rounded px-0.5 ml-5"
            style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
            {{ t('caseReview.comp.caseBasicInfo.overdue') }}
          </div>
        </div>
      </template>
      <template #version="{text}">
        <span v-if="text">v{{ text }}</span>
        <template v-else>--</template>
      </template>
      <template #softwareVersion="{text}">
        <span v-if="text">{{ text }}</span>
        <template v-else>--</template>
      </template>
    </Grid>
  </div>
</template>

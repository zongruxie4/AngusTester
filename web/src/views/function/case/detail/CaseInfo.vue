<script lang="ts" setup>
import { computed } from 'vue';
import { Grid, ReviewStatus } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Tag } from 'ant-design-vue';
import TaskPriority from '@/components/TaskPriority/index.vue';

interface Props {
  caseInfo?: {[key: string]: any}
}
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const infoColumns = computed(() => [
  [
    { label: t('functionCase.detail.review.name'), dataIndex: 'name' },
    // { label: 'ID', dataIndex: 'id' },
    { label: t('functionCase.detail.review.code'), dataIndex: 'code' },
    {
      label: t('functionCase.detail.review.reviewStatus'),
      dataIndex: 'reviewStatus'
    },
    { label: t('functionCase.detail.review.priority'), dataIndex: 'priority' },
    { label: t('functionCase.detail.review.tags'), dataIndex: 'tags' },

    // { label: '所属计划', dataIndex: 'planName' },
    // { label: '所属模块', dataIndex: 'moduleName' },
    {
      label: t('functionCase.detail.review.testResult'),
      dataIndex: 'testResult'
    },
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.detail.review.evalWorkload') : t('functionCase.detail.review.evalWorkload'), dataIndex: 'evalWorkload', customRender: ({ text }) => text || '--' },
    { label: props.caseInfo?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.detail.review.actualWorkload') : t('functionCase.detail.review.actualWorkload'), dataIndex: 'actualWorkload', customRender: ({ text }) => text || '--' }
  ]
]);

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      {{ t('functionCase.detail.review.basicInfo') }}
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
            {{ t('functionCase.detail.review.overdue') }}
          </div>
        </div>
      </template>
    </Grid>
  </div>
</template>

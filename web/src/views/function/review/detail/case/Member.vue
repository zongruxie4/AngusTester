<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Grid } from '@xcan-angus/vue-ui';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any}
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const peopleInfoColumns = [
  [
    {
      label: t('caseReview.comp.member.tester'),
      dataIndex: 'testerName'
    },
    {
      label: t('caseReview.comp.member.developer'),
      dataIndex: 'developerName'
    },
    {
      label: t('caseReview.comp.member.reviewer'),
      dataIndex: 'reviewerName'
    },
    {
      label: t('caseReview.comp.member.creator'),
      dataIndex: 'createdByName'
    },
    {
      label: t('caseReview.comp.member.lastModifier'),
      dataIndex: 'lastModifiedByName'
    }]
];

</script>
<template>
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div class="flex items-center mb-4">
      <Icon icon="icon-chengyuan" class="text-pink-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.member.title') }}
      </h3>
    </div>

    <Grid
      :columns="peopleInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="24"
      :marginBottom="6"
      labelSpacing="12px"
      font-size="14px"
      class="member-info-grid">
    </Grid>
  </div>
</template>

<style scoped>
:deep(.member-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>

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
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      {{ t('caseReview.comp.member.title') }}
    </div>
    <Grid
      :columns="peopleInfoColumns"
      :dataSource="props.caseInfo"
      :spacing="20"
      :marginBottom="4"
      labelSpacing="10px"
      font-size="12px"
      class="">
    </Grid>
  </div>
</template>

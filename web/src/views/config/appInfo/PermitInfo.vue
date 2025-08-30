<script setup lang="ts">
import { Grid, Icon } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { useI18n } from 'vue-i18n';
import { usePermitInfo } from './composables/usePermitInfo';

const { t } = useI18n();

// Use composable for permit information management
const {
  // Reactive data
  dataSource,

  // Computed properties
  columns,

  // Methods
  getVersionTypeIcon,
  init
} = usePermitInfo();

// Initialize data on component mount
init();

</script>
<template>
  <Grid :columns="columns" :dataSource="dataSource">
    <template #editionType="{text}">
      <Icon :icon="getVersionTypeIcon(text?.value)" class="text-4 -mt-0.5" />
      {{ text?.message }}
    </template>
    <template #endDate="{text}">
      {{ text }} ( {{ t('app.config.permitInfo.remainingDays', { days: dayjs(text).diff(dayjs().format(),'day') > 0 ? dayjs(text).diff(dayjs().format(),'day') : 0 }) }} )
    </template>
  </Grid>
</template>

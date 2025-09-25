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
  <div class="license-info-container">
    <div class="page-header">
      <h2 class="page-title">{{ t('app.config.permitInfo.title') }}</h2>
      <p class="page-subtitle">{{ t('common.description') }}</p>
    </div>

    <div class="license-content">
      <Grid
        :columns="columns"
        :dataSource="dataSource"
        :spacing="16"
        :marginBottom="4"
        labelSpacing="8px"
        font-size="12px"
        class="license-grid">
        <template #editionType="{text}">
          <div class="edition-type-cell">
            <Icon :icon="getVersionTypeIcon(text?.value)" class="edition-icon" />
            <span class="edition-text">{{ text?.message }}</span>
          </div>
        </template>
        <template #expiredDate="{text}">
          <div class="expired-date-cell">
            <span>{{ text }}</span>
            <span class="remaining-days">({{ t('app.config.permitInfo.remainingDays', { days: dayjs(text).diff(dayjs().format(),'day') > 0 ? dayjs(text).diff(dayjs().format(),'day') : 0 }) }})</span>
          </div>
        </template>
      </Grid>
    </div>
  </div>
</template>

<style scoped>
.license-info-container {
  padding: 20px;
  background: var(--content-bg);
  border-radius: 4px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--content-text-title);
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--content-text-sub-content);
  line-height: 1.5;
}

.license-content {
  background: var(--content-bg-card);
  border: 1px solid var(--border-divider);
  border-radius: 4px;
  padding: 20px;
}

.license-grid {
  width: 100%;
}

.edition-type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edition-icon {
  font-size: 16px;
  color: var(--content-text-content);
}

.edition-text {
  font-size: 12px;
  color: var(--content-text-content);
}

.expired-date-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.expired-date-cell span:first-child {
  font-size: 12px;
  color: var(--content-text-content);
}

.remaining-days {
  font-size: 12px;
  color: var(--content-text-sub-content);
}

:deep(.grid-label) {
  font-size: 14px !important;
  font-weight: 500;
  color: var(--content-text-title);
  padding-bottom: 8px;
}

:deep(.grid-value) {
  font-size: 12px !important;
  color: var(--content-text-content);
  line-height: 1.5;
}
</style>

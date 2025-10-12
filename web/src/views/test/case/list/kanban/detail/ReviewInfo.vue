<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReviewStatus } from '@xcan-angus/infra';

import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

const oneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === 0 && props.dataSource?.reviewStatus?.value === ReviewStatus.PASSED
      ? t('status.yes')
      : t('status.no');
  }
  return '--';
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.reviewInfo') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Enable Review -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('status.enabled') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': props.dataSource?.review === undefined }">
              {{ props.dataSource?.review ? t('status.yes') : t('status.no') }}
            </span>
          </div>
        </div>

        <!-- Review Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.counts.reviewCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.reviewNum }">
              {{ props.dataSource?.reviewNum || '--' }}
            </span>
          </div>
        </div>

        <!-- Review Fail Count -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.counts.reviewFailCount') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.reviewFailNum }">
              {{ props.dataSource?.reviewFailNum || '--' }}
            </span>
          </div>
        </div>

        <!-- One Time Passed -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.counts.oneTimePassed') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': oneReviewPass === '--' }">
              {{ oneReviewPass }}
            </span>
          </div>
        </div>

        <!-- Review Opinion -->
        <div class="info-row">
          <div class="info-label">
            <span>{{ t('common.reviewOpinion') }}</span>
          </div>
          <div class="info-value">
            <span class="info-text" :class="{ 'dash-text': !props.dataSource?.reviewRemark }">
              {{ props.dataSource?.reviewRemark || '--' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Info row styles */
.info-row {
  display: flex;
  align-items: flex-start;
  min-height: auto;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

/* Label styles */
.info-label {
  flex-shrink: 0;
  width: 100px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #686868;
  font-weight: 500;
  line-height: 1.4;
}
.info-label span {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

/* Value area styles */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

/* Text styles */
.info-text {
  font-size: 12px;
  color: #262626;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
  min-width: 0;
}
.info-text.dash-text {
  color: #8c8c8c;
}
</style>

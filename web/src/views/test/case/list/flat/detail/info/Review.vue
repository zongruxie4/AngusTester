<script setup lang="ts">
import { computed } from 'vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';

import ReviewStatus from '@/components/test/ReviewStatus.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
  columns?: any[][];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([]),
  columns: () => []
});

const { t } = useI18n();

/**
 * <p>Calculates if the review passed on first attempt.</p>
 * <p>Returns 'Yes' if review passed with no failures, 'No' otherwise, or '--' if no reviews.</p>
 * @returns Review pass status string
 */
const getOneReviewPass = computed(() => {
  if (props.dataSource?.reviewNum && Number(props.dataSource.reviewNum) > 0) {
    return props.dataSource?.reviewFailNum === 0 &&
    props.dataSource?.reviewStatus?.value === ReviewStatus.PASSED
      ? t('status.yes')
      : t('status.no');
  }
  return '--';
});

/**
 * <p>Gets the review enabled status display text.</p>
 * <p>Returns 'Yes' if review is enabled, 'No' otherwise.</p>
 * @returns Review enabled status string
 */
const getReviewEnabled = computed(() => {
  return props.dataSource?.review ? t('status.yes') : t('status.no');
});
</script>
<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('common.review') }}</div>
    </template>

    <template #default>
      <div class="review-info-container">
        <!-- Review Enabled -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('status.enabled') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ getReviewEnabled }}</span>
            </div>
          </div>
        </div>

        <!-- Review Count -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.reviewCount') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !props.dataSource?.reviewNum }" class="info-text">
                {{ props.dataSource?.reviewNum || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Review Fail Count -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.reviewFailCount') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !props.dataSource?.reviewFailNum }" class="info-text">
                {{ props.dataSource?.reviewFailNum || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- One Review Pass -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.oneTimePassed') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ getOneReviewPass }}</span>
            </div>
          </div>
        </div>

        <!-- Review Opinion -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.reviewOpinion') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !props.dataSource?.reviewRemark }" class="info-text">
                {{ props.dataSource?.reviewRemark || '--' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>
<style scoped>
/* Main Container */
.review-info-container {
  padding: 1rem 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* Info Row Layout */
.info-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: stretch;
}

/* Individual Info Item */
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  min-height: 2rem;
}

/* Label Styling */
.info-label {
  flex-shrink: 0;
  width: 5rem;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-label span {
  font-size: 0.75rem;
  font-weight: 400;
  color: #7c8087;
  line-height: 1.2;
  word-break: break-word;
  white-space: normal;
}

/* Value Styling */
.info-value {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  min-height: 1.5rem;
}

.info-text {
  font-size: 0.75rem;
  font-weight: 400;
  color: #374151;
  line-height: 1.4;
  word-break: break-word;
  white-space: normal;
}

/* Placeholder text styling */
.placeholder-text {
  color: #7c8087 !important;
  font-weight: 400 !important;
}

/* Animation for smooth transitions */
.info-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

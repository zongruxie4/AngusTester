<script setup lang="ts">
import { computed } from 'vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseTestResult } from '@/enums/enums';
import { CaseActionAuth } from '@/views/test/case/types';

import TestResult from '@/components/test/TestResult.vue';

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
 * <p>Calculates if the test passed on first attempt.</p>
 * <p>Returns 'Yes' if test passed with no failures, 'No' otherwise, or '--' if no tests.</p>
 * @returns Test pass status string
 */
const getOneTestPass = computed(() => {
  if (props.dataSource?.testNum && Number(props.dataSource.testNum) > 0) {
    return props.dataSource?.testFailNum === 0 &&
    props.dataSource?.testResult?.value === CaseTestResult.PASSED
      ? t('status.yes')
      : t('status.no');
  }
  return '--';
});
</script>
<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('common.test') }}</div>
    </template>

    <template #default>
      <div class="test-info-container">
        <!-- Test Count -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.testCount') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !props.dataSource?.testNum }" class="info-text">
                {{ props.dataSource?.testNum || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Test Fail Count -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.testFailCount') }}</span>
            </div>
            <div class="info-value">
              <span :class="{ 'placeholder-text': !props.dataSource?.testFailNum }" class="info-text">
                {{ props.dataSource?.testFailNum || '--' }}
              </span>
            </div>
          </div>
        </div>

        <!-- One Test Pass -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.counts.oneTimePassed') }}</span>
            </div>
            <div class="info-value">
              <span class="info-text">{{ getOneTestPass }}</span>
            </div>
          </div>
        </div>

        <!-- Test Result -->
        <div class="info-row">
          <div class="info-item">
            <div class="info-label">
              <span>{{ t('common.testRemark') }}</span>
            </div>
            <div class="info-value">
              <div class="info-text">
                {{ props.dataSource?.testRemark }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Main Container */
.test-info-container {
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

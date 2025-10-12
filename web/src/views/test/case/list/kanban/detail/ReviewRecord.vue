<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Image, NoData } from '@xcan-angus/vue-ui';
import { ReviewStatus } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';

const BasicInfo = defineAsyncComponent(() => import('@/views/test/case/list/kanban/detail/ReviewRecordCase.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/test/review/detail/case/Precondition.vue'));
const Description = defineAsyncComponent(() => import('@/views/test/review/detail/case/Description.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));

interface Props {
  dataSource: CaseDetail;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({ id: -1 } as unknown as CaseDetail)
});

const reviewNum = computed(() => {
  const total = +(props.dataSource?.reviewNum || 0);
  const failNum = +(props.dataSource?.reviewFailNum || 0);
  const successNum = total - failNum;
  return {
    total,
    failNum,
    successNum
  };
});

const preconditionClass = ref('');
const descriptionClass = ref('');
const stepsClass = ref();

const selectRecordId = ref();
const selectRecordInfo = ref();
const changeCurrentRecord = async (record) => {
  if (selectRecordId.value === record.id) {
    return;
  }
  selectRecordId.value = record.id;
  selectRecordInfo.value = record.reviewedCase;

  if (!selectRecordInfo.value.precondition && props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.precondition && !props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-del';
  } else if (selectRecordInfo.value.precondition !== props.dataSource.precondition) {
    preconditionClass.value = 'bg-status-modify';
  } else {
    preconditionClass.value = '';
  }

  if (!selectRecordInfo.value.description && props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(82, 196, 26, 0.1);';
  } else if (selectRecordInfo.value.description && !props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(255, 82, 82, 0.1);';
  } else if (selectRecordInfo.value.description !== props.dataSource.description) {
    descriptionClass.value = 'background-color: rgba(255, 102, 0, 0.1);';
  } else {
    descriptionClass.value = '';
  }

  if (!selectRecordInfo.value.steps?.length && props.dataSource.steps?.length) {
    stepsClass.value = 'bg-status-add';
  } else if (selectRecordInfo.value.steps?.length && !props.dataSource.steps?.length) {
    stepsClass.value = 'bg-status-del';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length !== props.dataSource.steps?.length)) {
    stepsClass.value = 'bg-status-modify';
  } else if (!!selectRecordInfo.value.steps?.length && (selectRecordInfo.value.steps?.length === props.dataSource.steps?.length)) {
    const isModify = selectRecordInfo.value.steps.some((item, idx) => {
      return (item.expectedResult !== props.dataSource.steps[idx].expectedResult) || (item.step !== props.dataSource.steps[idx].step);
    });
    if (isModify) {
      stepsClass.value = 'bg-status-modify';
    }
  } else {
    stepsClass.value = '';
  }
};

const reviewRecords = ref([]);
const loadReviewRecord = async () => {
  const [error, { data }] = await testCase.getReviewRecord(props.dataSource?.id);
  if (error) {
    return;
  }
  reviewRecords.value = data || [];
};

onMounted(() => {
  watch(() => props.dataSource?.id, newValue => {
    if (newValue) {
      loadReviewRecord();
      selectRecordInfo.value = undefined;
    }
  }, {
    immediate: true
  });
});

defineExpose({
  refresh: loadReviewRecord
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.reviewRecord') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Review Statistics -->
        <div class="review-stats">
          <div class="stat-item">
            <div class="stat-label bg-blue-1 text-white">
              {{ t('chart.total') }}
            </div>
            <div class="stat-value bg-gray-light">
              {{ reviewNum.total }}
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-label bg-status-success text-white">
              {{ t('status.passed') }}
            </div>
            <div class="stat-value bg-gray-light">
              {{ reviewNum.successNum }}
            </div>
          </div>

          <div class="stat-item">
            <div class="stat-label bg-status-error text-white">
              {{ t('status.notPassed') }}
            </div>
            <div class="stat-value bg-gray-light">
              {{ reviewNum.failNum }}
            </div>
          </div>
        </div>

        <!-- Review Records List -->
        <div class="review-records-section">
          <div class="section-title">{{ t('common.reviewRecord') }}</div>

          <div class="records-list">
            <div
              v-for="record in reviewRecords"
              :key="record.id"
              class="record-item"
              :class="{'selected': record.id === selectRecordId}"
              @click="changeCurrentRecord(record)">
              <div class="record-header">
                <Image
                  type="avatar"
                  :src="record.avatar"
                  class="record-avatar" />

                <span class="reviewer-name">{{ record.reviewerName }}
                  {{ record.reviewStatus?.value === ReviewStatus.PASSED
                    ? t('testCase.messages.reviewPassedCase')
                    : t('testCase.messages.reviewFailedCase') }}</span>

                <div class="review-date">{{ record.reviewDate }}</div>
              </div>

              <div
                v-if="record.reviewRemark"
                class="review-remark"
                :title="record.reviewRemark">
                {{ record.reviewRemark }}
              </div>
              <div v-else class="no-remark">
                {{ t('common.noRemark') }}
              </div>
            </div>
          </div>

          <div v-if="!reviewRecords?.length" class="no-data-container">
            <NoData size="small" />
          </div>
        </div>

        <!-- Comparison View -->
        <div v-if="!!selectRecordInfo" class="comparison-view">
          <div class="comparison-column">
            <div class="column-title">{{ t('testCase.messages.reviewVersion') }}</div>

            <BasicInfo :caseInfo="selectRecordInfo" />

            <Precondition :caseInfo="selectRecordInfo" />

            <div class="section-subtitle">
              {{ t('common.testSteps') }}
            </div>

            <CaseStep :defaultValue="selectRecordInfo?.steps || {}" readonly />

            <Description :caseInfo="selectRecordInfo" />
          </div>

          <div class="comparison-column">
            <div class="column-title">{{ t('testCase.messages.latestVersion') }}</div>

            <BasicInfo :caseInfo="props.dataSource" />

            <Precondition :caseInfo="props.dataSource" :contentClass="preconditionClass" />

            <div class="section-subtitle">
              {{ t('common.testSteps') }}
            </div>

            <div :class="stepsClass">
              <CaseStep :defaultValue="props?.dataSource?.steps || []" readonly />
            </div>

            <Description :caseInfo="props?.dataSource" :contentBg="descriptionClass" />
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
  gap: 16px;
}

/* Review statistics styles */
.review-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
}

.stat-label {
  width: 80px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

.stat-value {
  width: 80px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

/* Review records section */
.review-records-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-item {
  padding: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.record-item:hover {
  background-color: #f5f5f5;
}

.record-item.selected {
  background-color: #e6f7ff;
  border-color: #1890ff;
}

.record-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-avatar {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.reviewer-name {
  font-weight: 600;
  font-size: 12px;
  flex: 1;
  min-width: 0;
}

.review-date {
  font-size: 12px;
  color: #8c8c8c;
  flex-shrink: 0;
}

.review-remark {
  margin-top: 12px;
  font-size: 12px;
  color: #262626;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-remark {
  margin-top: 12px;
  font-size: 12px;
  color: #8c8c8c;
}

.no-data-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Comparison view styles */
.comparison-view {
  display: flex;
  gap: 16px;
  max-width: 100%;
}

.comparison-column {
  flex: 1;
  padding: 8px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comparison-column:last-child {
  border-right: none;
}

.column-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  margin: 8px 0 4px 0;
}
</style>

<script lang="ts" setup>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Select } from '@xcan-angus/vue-ui';
import lodash from 'lodash-es';
import { test } from '@/api/tester';
import { BaselineCaseInfo, BaselineDetail } from '@/views/test/baseline/types';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

// Async component for case comparison modal
const CompareModal = defineAsyncComponent(() => import('./CompareModal.vue'));

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  baselineId: undefined,
  planId: undefined
});

// Reactive Data
const availableBaselines = ref<BaselineDetail[]>([]);
const allCaseIdentifiers = ref<string[]>([]);
const isCompareModalVisible = ref(false);
const selectedCompareBaselineId = ref<string>();
const baseCaseData = ref<BaselineCaseInfo>({} as BaselineCaseInfo);
const compareCaseData = ref({});

/**
 * Get the current baseline information
 */
const currentBaseline = computed(() => {
  return availableBaselines.value.find(baseline => baseline.id === props.baselineId);
});

/**
 * Load all available baselines for comparison
 */
const loadAvailableBaselines = async () => {
  const [error, { data }] = await test.getBaselineList({
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  availableBaselines.value = data?.list || [];
  selectedCompareBaselineId.value = availableBaselines.value[0].id;
};

/**
 * Load base case data from the current baseline
 */
const loadBaseCaseData = async () => {
  const [error, { data }] = await test.getBaselineCaseList(props.baselineId, {
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  (data?.list || []).forEach(caseItem => {
    allCaseIdentifiers.value.push(caseItem.id);
    baseCaseData.value[caseItem.id] = caseItem;
  });
};

/**
 * Load compare case data from the selected baseline
 */
const loadCompareCaseData = async () => {
  const [error, { data }] = await test.getBaselineCaseList(selectedCompareBaselineId.value, {
    pageSize: 2000,
    pageNo: 1,
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  allCaseIdentifiers.value = Object.keys(baseCaseData.value);
  (data?.list || []).forEach(caseItem => {
    if (!allCaseIdentifiers.value.includes(caseItem.id)) {
      allCaseIdentifiers.value.push(caseItem.id);
    }
    compareCaseData.value[caseItem.id] = caseItem;
  });
};

/**
 * Handle compare baseline selection change
 */
const handleCompareBaselineChange = () => {
  compareCaseData.value = {};
  loadCompareCaseData();
};

/**
 * Open comparison modal for specific case
 * @param caseId - ID of the case to compare
 */
const openCaseComparisonModal = (caseId) => {
  isCompareModalVisible.value = true;
  selectedCaseId.value = caseId;
};

// Component State
const selectedCaseId = ref();

/**
 * Initialize component data on mount
 */
onMounted(async () => {
  await loadAvailableBaselines();
  Promise.all([loadBaseCaseData(), loadCompareCaseData()])
    .then(() => {
      allCaseIdentifiers.value = lodash.uniq(allCaseIdentifiers.value);
    });
});

// Table Configuration
const leftColumnConfig = [
  {
    title: '',
    dataIndex: 'idx',
    class: 'w-8 border-r text-center'
  },
  {
    title: t('common.name'),
    dataIndex: 'name',
    class: 'flex-1 border-r'
  },
  {
    title: t('common.version'),
    dataIndex: 'version',
    class: 'w-20'
  }
];

const rightColumnConfig = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    class: 'flex-1 border-r'
  },
  {
    title: t('common.version'),
    dataIndex: 'version',
    class: 'w-20 border-r'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    class: 'w-20 text-center'
  }
];
</script>
<template>
  <div class="overflow-y-auto -mt-4">
    <div class="flex border-b border-l border-r">
      <div class="flex-1 leading-8  border-r">
        <div class="text-center">
          {{ currentBaseline?.name }}
        </div>
      </div>
      <div class="flex-1 leading-8">
        <div class="text-center px-1">
          <Select
            v-model:value="selectedCompareBaselineId"
            class="w-full"
            :options="availableBaselines"
            :fieldNames="{
              value: 'id',
              label: 'name'
            }"
            @change="handleCompareBaselineChange" />
        </div>
      </div>
    </div>

    <div class="flex border-b  border-l border-r">
      <div class="flex-1 leading-8 border-r">
        <div class="text-center flex">
          <div
            v-for="leftColumn in leftColumnConfig"
            :key="leftColumn.dataIndex"
            class="bg-gray-bg"
            :class="leftColumn.class">
            {{ leftColumn.title }}
          </div>
        </div>
      </div>
      <div class="flex-1 leading-8">
        <div class="text-center flex">
          <div
            v-for="rightColumn in rightColumnConfig"
            :key="rightColumn.dataIndex"
            class="bg-gray-bg"
            :class="rightColumn.class">
            {{ rightColumn.title }}
          </div>
        </div>
      </div>
    </div>

    <div
      v-for="(caseId, idx) in allCaseIdentifiers"
      :key="caseId"
      class="flex leading-8 border-b  border-l border-r">
      <div class="flex-1 leading-8 border-r flex">
        <div
          v-for="(leftColumn) in leftColumnConfig"
          :key="leftColumn.dataIndex"
          :class="leftColumn.class"
          class="px-1">
          <tmplate v-if="leftColumn.dataIndex === 'version'">
            <span v-if="baseCaseData[caseId]?.[leftColumn.dataIndex]">v{{ baseCaseData[caseId][leftColumn.dataIndex] }}</span>
          </tmplate>
          <template v-else-if="leftColumn.dataIndex === 'idx'">
            {{ idx + 1 }}
          </template>
          <template v-else>
            {{ baseCaseData[caseId]?.[leftColumn.dataIndex] }}
          </template>
        </div>
      </div>
      <div class="flex-1 leading-8  flex" :class="{'bg-status-add': !baseCaseData[caseId] && compareCaseData[caseId], 'bg-status-del': baseCaseData[caseId] && !compareCaseData[caseId]}">
        <div
          v-for="rightColumn in rightColumnConfig"
          :key="rightColumn.dataIndex"
          :class="rightColumn.class"
          class="px-1">
          <tmplate v-if="rightColumn.dataIndex === 'version'">
            <span v-if="compareCaseData[caseId]?.[rightColumn.dataIndex]">v{{ compareCaseData[caseId][rightColumn.dataIndex] }}</span>
          </tmplate>
          <tmplate v-else-if="rightColumn.dataIndex === 'action'">
            <Button
              v-if="compareCaseData[caseId] && baseCaseData[caseId]"
              type="link"
              size="small"
              @click="openCaseComparisonModal(caseId)">
              {{ t('testCaseBaseline.case.compare') }}
            </Button>
          </tmplate>
          <template v-else>
            {{ compareCaseData[caseId]?.[rightColumn.dataIndex] }}
          </template>
        </div>
      </div>
    </div>

    <AsyncComponent :visible="isCompareModalVisible">
      <CompareModal
        v-model:visible="isCompareModalVisible"
        :baselineId="props.baselineId"
        :compareBaselineId="selectedCompareBaselineId"
        :caseId="selectedCaseId"
        :projectId="props.projectId" />
    </AsyncComponent>
  </div>
</template>

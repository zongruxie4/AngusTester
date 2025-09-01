
<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { StatisticsProps } from './types';
import { useStatistics } from './composables/useStatistics';

const { t } = useI18n();

/**
 * <p>Component props definition</p>
 * <p>Defines the interface for component properties</p>
 */
const props = withDefaults(defineProps<StatisticsProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

/**
 * <p>Main composable for component logic</p>
 * <p>Integrates all business logic, chart management, and data operations</p>
 */
const {
  // Data state
  userStatistics,
  projectStatistics,

  // Chart configuration
  statisticsCardConfig,

  // Chart references
  variableRef,
  dataSetRef,
  fileRef,
  dataSourceRef
} = useStatistics(props.projectId, props.userInfo?.id);
</script>

<template>
  <div class="w-full">
    <!-- Section title -->
    <div class="text-3.5 font-semibold mb-2">
      {{ t('dataHome.statistics.resourceStatistics') }}
    </div>

    <!-- Combined Statistics Cards and Charts Section -->
    <div class="grid grid-cols-4 gap-5">
      <!-- Variable Statistics Group -->
      <div class="bg-white rounded border border-border-divider p-4">
        <!-- Statistics Card Section -->
        <div class="mb-4">
          <div class="flex items-center mb-2">
            <div class="text-sm text-text-sub-content">{{ statisticsCardConfig[0].name }}</div>
            <div class="text-2xl font-semibold text-text-title ml-2.5">
              {{ projectStatistics[statisticsCardConfig[0].total] }}
            </div>
          </div>
        </div>

        <!-- Chart Section -->
        <div ref="variableRef" class="w-full h-43"></div>
      </div>

      <!-- Dataset Statistics Group -->
      <div class="bg-white rounded border border-border-divider p-4">
        <!-- Statistics Card Section -->
        <div class="mb-4">
          <div class="flex items-center mb-2">
            <div class="text-sm text-text-sub-content">{{ statisticsCardConfig[1].name }}</div>
            <div class="text-2xl font-semibold text-text-title ml-2.5">
              {{ projectStatistics[statisticsCardConfig[1].total] }}
            </div>
          </div>
        </div>

        <!-- Chart Section -->
        <div ref="dataSetRef" class="w-full h-43"></div>
      </div>

      <!-- File Statistics Group -->
      <div class="bg-white rounded border border-border-divider p-4">
        <!-- Statistics Card Section -->
        <div class="mb-4">
          <div class="flex items-center mb-2">
            <div class="text-sm text-text-sub-content">{{ statisticsCardConfig[2].name }}</div>
            <div class="text-2xl font-semibold text-text-title ml-2.5">
              {{ projectStatistics[statisticsCardConfig[2].total] }}
            </div>
          </div>
        </div>

        <!-- Chart Section -->
        <div ref="fileRef" class="w-full h-43"></div>
      </div>

      <!-- Datasource Statistics Group -->
      <div class="bg-white rounded border border-border-divider p-4">
        <!-- Statistics Card Section -->
        <div class="mb-4">
          <div class="flex items-center mb-2">
            <div class="text-sm text-text-sub-content">{{ statisticsCardConfig[3].name }}</div>
            <div class="text-2xl font-semibold text-text-title ml-2.5">
              {{ projectStatistics[statisticsCardConfig[3].total] }}
            </div>
          </div>
        </div>

        <!-- Chart Section -->
        <div ref="dataSourceRef" class="w-full h-43"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>

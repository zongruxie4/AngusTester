
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
  loading,
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
    <!-- Statistics Cards Section -->
    <div class="grid grid-cols-4 gap-5 mb-7.5">
      <div
        v-for="config in statisticsCardConfig"
        :key="config.name"
        class="bg-white rounded border border-border-divider p-4">
        
        <!-- Top Section with Total Count -->
        <div class="flex items-center justify-between mb-2">
          <div class="text-2xl font-semibold text-text-title">
            {{ projectStatistics[config.total] }}
          </div>
          <div :class="config.topClass" class="w-8 h-8 rounded-full flex items-center justify-center">
            <span class="text-white text-sm">{{ projectStatistics[config.week] }}</span>
          </div>
        </div>
        
        <!-- Bottom Section with Monthly Count -->
        <div class="flex items-center justify-between">
          <div class="text-sm text-text-sub-content">{{ config.name }}</div>
          <div :class="config.bottomClass" class="px-2 py-1 rounded text-xs text-white">
            {{ projectStatistics[config.month] }}
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="bg-white rounded border border-border-divider p-5">
      <div class="text-lg font-semibold mb-5">{{ t('dataHome.statistics.charts.title') }}</div>
      
      <div class="grid grid-cols-2 gap-8">
        <!-- Variable Usage Chart -->
        <div class="text-center">
          <div class="text-sm font-medium mb-3">{{ t('dataHome.statistics.charts.variableUsage') }}</div>
          <div ref="variableRef" class="w-full h-48"></div>
        </div>
        
        <!-- Dataset Usage Chart -->
        <div class="text-center">
          <div class="text-sm font-medium mb-3">{{ t('dataHome.statistics.charts.datasetUsage') }}</div>
          <div ref="dataSetRef" class="w-full h-48"></div>
        </div>
        
        <!-- File Resource Type Chart -->
        <div class="text-center">
          <div class="text-sm font-medium mb-3">{{ t('dataHome.statistics.charts.fileResourceType') }}</div>
          <div ref="fileRef" class="w-full h-48"></div>
        </div>
        
        <!-- Datasource Database Type Chart -->
        <div class="text-center">
          <div class="text-sm font-medium mb-3">{{ t('dataHome.statistics.charts.datasourceDatabaseType') }}</div>
          <div ref="dataSourceRef" class="w-full h-48"></div>
        </div>
      </div>
    </div>

    <!-- User Statistics Section -->
    <div class="bg-white rounded border border-border-divider p-5 mt-5">
      <div class="text-lg font-semibold mb-5">{{ t('dataHome.statistics.userStatistics.title') }}</div>
      
      <div class="grid grid-cols-3 gap-8">
        <!-- Services Statistics -->
        <div class="text-center">
          <div class="text-2xl font-semibold text-text-title mb-2">
            {{ userStatistics.allService }}
          </div>
          <div class="text-sm text-text-sub-content mb-1">
            {{ t('dataHome.statistics.userStatistics.services') }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisWeek') }}: {{ userStatistics.serviceByLastWeek }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisMonth') }}: {{ userStatistics.serviceByLastMonth }}
          </div>
        </div>
        
        <!-- APIs Statistics -->
        <div class="text-center">
          <div class="text-2xl font-semibold text-text-title mb-2">
            {{ userStatistics.allApis }}
          </div>
          <div class="text-sm text-text-sub-content mb-1">
            {{ t('dataHome.statistics.userStatistics.apis') }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisWeek') }}: {{ userStatistics.apisByLastWeek }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisMonth') }}: {{ userStatistics.apisByLastMonth }}
          </div>
        </div>
        
        <!-- Unarchived APIs Statistics -->
        <div class="text-center">
          <div class="text-2xl font-semibold text-text-title mb-2">
            {{ userStatistics.allUnarchivedApis }}
          </div>
          <div class="text-sm text-text-sub-content mb-1">
            {{ t('dataHome.statistics.userStatistics.unarchivedApis') }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisWeek') }}: {{ userStatistics.unarchivedApisByLastWeek }}
          </div>
          <div class="text-xs text-text-sub-content">
            {{ t('dataHome.statistics.userStatistics.thisMonth') }}: {{ userStatistics.unarchivedApisByLastMonth }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.huang-top {
  background: linear-gradient(135deg, #ffd666 0%, #ffc53d 100%);
}

.huang-bottom {
  background: linear-gradient(135deg, #ffc53d 0%, #faad14 100%);
}

.hong-top {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
}

.hong-bottom {
  background: linear-gradient(135deg, #ff4d4f 0%, #f5222d 100%);
}

.lan-top {
  background: linear-gradient(135deg, #69c0ff 0%, #40a9ff 100%);
}

.lan-bottom {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
}

.qin-top {
  background: linear-gradient(135deg, #b37feb 0%, #9254de 100%);
}

.qin-bottom {
  background: linear-gradient(135deg, #9254de 0%, #722ed1 100%);
}
</style>

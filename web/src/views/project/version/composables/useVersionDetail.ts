import { inject, onMounted, ref, watch } from 'vue';
import { software } from '@/api/tester';
import { BasicProps } from '@/types/types';
import type { VersionInfo, ChartValue } from '../types';

export function useVersionDetail (props: BasicProps) {
  // Reactive state
  const dataSource = ref<VersionInfo>({} as VersionInfo);
  const loading = ref(false);
  const refreshNotify = ref('');

  // Injected dependencies
  const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

  /**
   * Load version detail data from API
   * Fetches version information and updates tab pane title
   * @param id - Version ID to load
   */
  const loadVersionData = async (id: string): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    const [error, res] = await software.getSoftwareVersionDetail(id);
    loading.value = false;

    if (error) {
      return;
    }

    const data = res?.data as VersionInfo;
    if (!data) {
      return;
    }

    // Update data source
    dataSource.value = { ...data };

    // Update tab pane title with version name
    const name = data.name;
    if (name && typeof updateTabPane === 'function') {
      updateTabPane({ name, _id: id + '-detail' });
    }
  };

  /**
   * Calculate chart values from version progress data
   * Converts progress metrics into chart-compatible format
   * @param progress - Version progress data
   * @returns Chart value configuration
   */
  const calculateChartValues = (progress: VersionInfo['progress']): { chart1Value: ChartValue; chart2Value: ChartValue } => {
    const { completedNum = 0, completedRate = 0, completedWorkload = 0, evalWorkload = 0, totalNum = 0 } = progress || {};

    return {
      chart1Value: {
        title: completedRate + '%',
        value: [
          { value: totalNum - completedNum },
          { value: completedNum }
        ]
      },
      chart2Value: {
        title: +evalWorkload > 0 ? ((completedWorkload / evalWorkload).toFixed(2) + '%') : '0%',
        value: [
          { value: evalWorkload - completedWorkload },
          { value: completedWorkload }
        ]
      }
    };
  };

  /**
   * Get computed chart values based on current data source
   * Reactive chart data derived from version progress
   */
  const chartValue = ref(calculateChartValues(dataSource.value.progress));

  /**
   * Watch for data source changes and update chart values
   * Ensures charts reflect current version progress
   */
  watch(() => dataSource.value.progress, (newProgress) => {
    chartValue.value = calculateChartValues(newProgress);
  }, { deep: true });

  /**
   * Watch for prop data changes and load new version
   * Handles navigation between different versions
   */
  onMounted(() => {
    watch(() => props.data, async (newValue, oldValue) => {
      const id = newValue?.id;
      if (!id) {
        return;
      }

      const oldId = oldValue?.id;
      if (id === oldId) {
        return;
      }

      await loadVersionData(id);
    }, { immediate: true });
  });

  return {
    dataSource,
    loading,
    refreshNotify,
    chartValue
  };
}

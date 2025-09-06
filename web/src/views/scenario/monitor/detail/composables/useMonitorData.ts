import { ref, watch } from 'vue';
import { scenario } from '@/api/tester';
import type { MonitorInfo } from '../types';

/**
 * Composable for managing monitor data and scenario information
 */
export function useMonitorData() {
  // Monitor data state
  const dataSource = ref<MonitorInfo>();
  const scenarioData = ref({});
  const scenarioPlugin = ref<string>();
  const loading = ref(false);

  /**
   * Load monitor detail data by ID
   * @param id - Monitor ID
   */
  const loadData = async (id: string): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    try {
      const [error, res] = await scenario.getMonitorDetail(id);
      
      if (error) {
        console.error('Failed to load monitor data:', error);
        return;
      }

      const data = res?.data as MonitorInfo;
      if (!data) {
        console.warn('No monitor data received');
        return;
      }

      dataSource.value = data;
      
      // Load scenario plugin if scenarioId exists
      if (data.scenarioId) {
        await loadScenarioPlugin(data.scenarioId);
      }
    } finally {
      loading.value = false;
    }
  };

  /**
   * Load scenario plugin information
   * @param scenarioId - Scenario ID
   */
  const loadScenarioPlugin = async (scenarioId: string): Promise<void> => {
    try {
      const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
      
      if (error) {
        console.error('Failed to load scenario data:', error);
        return;
      }

      scenarioData.value = data;
      scenarioPlugin.value = data.plugin;
    } catch (error) {
      console.error('Error loading scenario plugin:', error);
    }
  };

  /**
   * Watch for data changes and reload when needed
   * @param dataRef - Reactive data reference to watch
   */
  const watchDataChanges = (dataRef: () => any) => {
    watch(dataRef, async (newValue, oldValue) => {
      const id = newValue?.id;
      if (!id) {
        return;
      }

      const oldId = oldValue?.id;
      if (id === oldId) {
        return;
      }

      await loadData(id);
    }, { immediate: true });
  };

  return {
    // State
    dataSource,
    scenarioData,
    scenarioPlugin,
    loading,
    
    // Methods
    loadData,
    loadScenarioPlugin,
    watchDataChanges
  };
}

import { ref } from 'vue';
import { scenario } from '@/api/tester';
import type { ScenarioData } from '../types';

/**
 * Composable for managing scenario data and authentication
 */
export function useScenarioData (scenarioId: string) {
  const scenarioData = ref<ScenarioData>();
  const auth = ref(false);
  const authPermissions = ref<string[]>([]);
  const isHttpPlugin = ref(false);

  /**
   * Load scenario detail data
   */
  const loadScenarioDetail = async () => {
    const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
    if (error) {
      return;
    }
    scenarioData.value = data;
    isHttpPlugin.value = data?.plugin === 'Http';
    auth.value = data.auth;

    if (data.auth) {
      await loadPermissions();
    }
  };

  /**
   * Load user permissions for the scenario
   */
  const loadPermissions = async () => {
    const [_error, { data }] = await scenario.getCurrentScenarioAuth(scenarioId);
    authPermissions.value = (data?.permissions || []).map((i: { value: string }) => i.value);
  };

  /**
   * Handle authentication flag change
   */
  const handleAuthFlagChange = (data: { auth: boolean }) => {
    auth.value = data.auth;
    if (auth.value) {
      loadPermissions();
    }
  };

  return {
    scenarioData,
    auth,
    authPermissions,
    isHttpPlugin,
    loadScenarioDetail,
    loadPermissions,
    handleAuthFlagChange
  };
}

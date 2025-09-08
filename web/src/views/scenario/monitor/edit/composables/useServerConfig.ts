import { ref } from 'vue';
import { scenario } from '@/api/tester';
import type { ServerSetting } from '../../types';

/**
 * Composable for managing server configuration
 */
export function useServerConfig () {
  const serverSetting = ref<ServerSetting[]>([]);
  const isHttpPlugin = ref(false);

  /**
   * Load server settings for a scenario
   * @param scenarioId - Scenario ID
   */
  const loadServerSetting = async (scenarioId: string | undefined) => {
    if (!scenarioId) {
      return;
    }
    const [error, { data = [] }] = await scenario.getTestSchemaServer(scenarioId);
    if (error) {
      return;
    }
    serverSetting.value = data;
  };

  /**
   * Load scenario plugin information
   * @param scenarioId - Scenario ID
   */
  const loadScenarioPlugin = async (scenarioId: string) => {
    const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
    if (error) {
      return;
    }
    isHttpPlugin.value = data.plugin === 'Http';
  };

  /**
   * Check if server has variables
   * @param variables - Variables object
   * @returns boolean
   */
  const hasVariables = (variables = {}) => {
    if (!variables) {
      return false;
    }
    return !!Object.keys(variables || {}).length;
  };

  /**
   * Change variable default value
   * @param serverIndex - Server index
   * @param variableKey - Variable key
   * @param newValue - New default value
   */
  const changeVariableDefaultValue = (serverIndex: number, variableKey: string, newValue: string) => {
    if (serverSetting.value?.[serverIndex]?.variables?.[variableKey]) {
      serverSetting.value[serverIndex].variables![variableKey].default = newValue;
    }
  };

  /**
   * Handle scenario change event
   * @param _scenarioId - Selected scenario ID (unused)
   * @param option - Scenario option data
   */
  const handleScenarioChange = (_scenarioId: string, option: any) => {
    isHttpPlugin.value = option.plugin === 'Http';
  };

  return {
    serverSetting,
    isHttpPlugin,
    loadServerSetting,
    loadScenarioPlugin,
    hasVariables,
    changeVariableDefaultValue,
    handleScenarioChange
  };
}

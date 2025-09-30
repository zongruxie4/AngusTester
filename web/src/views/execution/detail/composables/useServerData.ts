import { ref } from 'vue';
import { exec } from '@/api/tester';
import type { ServerObject, ServerVariable } from '../types';

/**
 * Composable for managing server data in execution detail
 * Handles loading server configurations and checking for variables
 *
 * @param execId - The execution ID to load server data for
 * @returns Server data management functions and reactive state
 */
export const useServerData = (execId: string) => {
  // Reactive state for server list
  const serverList = ref<ServerObject[]>([]);

  /**
   * Load server configurations for the execution
   * Fetches server data from API and updates the serverList
   */
  const loadServers = async () => {
    // Fetch server data from API
    const [error, { data = [] }] = await exec.getTestServer(execId);
    if (error) {
      // Silently handle error - component will show empty state
      return;
    }
    // Update server list with fetched data
    serverList.value = data || [];
  };

  /**
   * Check if server has variables defined
   * @param variables - Server variables object
   * @returns boolean indicating if variables exist
   */
  const hasVariable = (variables: Record<string, ServerVariable> = {}) => {
    if (!variables) {
      return false;
    }
    // Check if variables object has any keys
    return !!Object.keys(variables || {}).length;
  };

  // Return reactive state and functions
  return {
    serverList,
    loadServers,
    hasVariable
  };
};

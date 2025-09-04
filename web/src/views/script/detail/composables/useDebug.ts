import { ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { exec } from 'src/api/ctrl';
import YAML from 'yaml';

/**
 * Debug functionality management composable
 * Handles script debugging and debug info loading
 */
export function useDebug (scriptId: any, scriptValue: any, toolbarRef: any) {
  const debugExecInfo = ref<{
    id: string;
    execNode: { id: string, name: string, ip: string, agentPort: string, publicIp: string };
    sampleContents: { [key: string]: any }[];
    task: {
      arguments: {
        ignoreAssertions: boolean;
      };
      pipelines: {
        name: string;
      }[];
    };
    schedulingResult: {
      console: string[];
      success: boolean;
      exitCode: string;
    };
  }>();

  const pluginType = ref<string>();

  /**
   * Start script debugging
   */
  const startDebug = async () => {
    try {
      const scriptConfig = YAML.parse(scriptValue.value);
      if (!scriptConfig) {
        return;
      }

      const params: {
        broadcast: true;
        scriptId: string;
        scriptType: string;
      } = {
        broadcast: true,
        scriptId: scriptId.value,
        scriptType: scriptConfig.type
      };

      const [error, { data }] = await exec.startDebug(params);
      if (error || !data) {
        return;
      }

      debugExecInfo.value = data;
      pluginType.value = data.plugin;
      if (typeof toolbarRef.value?.open === 'function') {
        toolbarRef.value.open('debugResult');
      }
    } catch (error) {
      notification.error(error.message);
    }
  };

  /**
   * Load debug information
   */
  const loadDebugInfo = async () => {
    const [error, { data }] = await exec.getDebugScriptInfo(scriptId.value);
    if (error || !data) {
      return;
    }

    debugExecInfo.value = data;
    pluginType.value = data.plugin;
  };

  return {
    debugExecInfo,
    pluginType,
    startDebug,
    loadDebugInfo
  };
}

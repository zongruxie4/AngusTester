import { ref, watch } from 'vue';
import { getDataByProxy } from '@/api/proxy';

/**
 * Execution log management composable
 * Handles loading and downloading of execution logs
 */
export function useExecLog(props: any) {
  const nodeId = ref<string>();
  const nodeIp = ref<string>();
  const nodePort = ref<string>('6807');

  const execLogContent = ref();
  const execLogPath = ref('');
  const execLogErr = ref(false);
  const errorText = ref();

  const loading = ref(!!props.execNode?.id);

  /**
   * Load execution log from proxy
   */
  const loadExecLog = async () => {
    loading.value = true;
    const [error, res] = await getDataByProxy(
      `http://${nodeIp.value}:${nodePort.value}/actuator/runner/log/${props.execId}`, 
      {}, 
      { timeout: 0 }
    );
    loading.value = false;
    
    if (error) {
      execLogErr.value = true;
      if (error.response?.data) {
        errorText.value = error.response.data;
      } else {
        errorText.value = undefined;
      }
      return;
    }

    execLogErr.value = false;
    execLogPath.value = res.headers?.['xc-agent-log-path'];
    execLogContent.value = res?.data || '';
  };

  /**
   * Download log as file
   */
  const downloadLog = () => {
    const content = execLogContent.value;
    if (!content) {
      return;
    }

    const blob = new Blob([content], {
      type: 'text/plain'
    });

    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = 'runner.log';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    window.URL.revokeObjectURL(url);
  };

  // Watch for execNode changes
  watch(() => props.execNode, (newValue) => {
    if (newValue && newValue?.id) {
      nodeId.value = newValue.id;
      nodeIp.value = newValue.publicIp || newValue.ip;
      nodePort.value = newValue.agentPort || '6807';
      loadExecLog();
    }
  }, {
    deep: true,
    immediate: true
  });

  return {
    // Log state
    execLogContent,
    execLogPath,
    execLogErr,
    errorText,
    loading,
    
    // Log methods
    loadExecLog,
    downloadLog
  };
}
import { ref } from 'vue';
import { ai } from 'src/api/gm';

/**
 * AI functionality management composable
 * Handles AI script generation features
 */
export function useAI (aiEnabled: any) {
  const antDrawerVisible = ref(false);
  const aiScriptValue = ref('');
  const aiKeywords = ref('');
  const generating = ref(false);

  /**
   * Open AI generation drawer
   */
  const openAIDrawer = () => {
    antDrawerVisible.value = true;
  };

  /**
   * Generate script using AI
   */
  const generateScript = async () => {
    if (!aiEnabled.value) {
      return;
    }

    generating.value = true;
    const [error, res] = await ai.getChartResult({ type: 'WRITE_ANGUS_SCRIPT', question: aiKeywords.value });
    generating.value = false;
    if (error) {
      return;
    }

    const data = (res?.data || { normal: '', content: '' }) as {
      normal:string;
      content:string;
    };
    aiScriptValue.value = data.content;
  };

  /**
   * Cancel AI generation
   */
  const cancelGenerate = () => {
    antDrawerVisible.value = false;
  };

  /**
   * Confirm AI generation
   */
  const confirmGenerate = (scriptValue: any) => {
    antDrawerVisible.value = false;
    scriptValue.value = aiScriptValue.value;
  };

  return {
    // AI state
    antDrawerVisible,
    aiScriptValue,
    aiKeywords,
    generating,

    // AI methods
    openAIDrawer,
    generateScript,
    cancelGenerate,
    confirmGenerate
  };
}

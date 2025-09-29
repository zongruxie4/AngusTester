import { useI18n } from 'vue-i18n';
import { onMounted, ref, watch } from 'vue';
import { variable } from '@/api/tester';
import { VariableDetail } from '../../types';

/**
 * Composable for managing variable preview data logic
 * Handles data fetching and refreshing for variable preview
 */
export function usePreviewData (props: {
  dataSource: VariableDetail;
}) {
  const { t } = useI18n();

  // Reactive state for component
  const loading = ref(false);
  const content = ref<string>();
  const errorMessage = ref<string>();

  /**
   * Refresh the preview data
   */
  const refresh = () => {
    if (loading.value) {
      return;
    }
    loadData();
  };

  /**
   * Load variable preview data from API
   */
  const loadData = async () => {
    if (!props.dataSource) {
      return;
    }

    loading.value = true;
    const [error, res] = await variable.previewVariableValue(props.dataSource, { silence: true });
    loading.value = false;
    if (error) {
      errorMessage.value = error.message;
      return;
    }

    content.value = res?.data;
    errorMessage.value = undefined;
  };

  /**
   * Reset the preview data
   */
  const reset = () => {
    loading.value = false;
    content.value = undefined;
    errorMessage.value = undefined;
  };

  // Load data when component is mounted
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      reset();

      if (!newValue) {
        return;
      }

      loadData();
    }, { immediate: true });
  });

  return {
    // State
    loading,
    content,
    errorMessage,

    // Methods
    refresh
  };
}

import { computed } from 'vue';

/**
 * Debug log management composable
 * Handles debug log data processing
 */
export function useDebugLog (props: any) {
  /**
   * Computed scheduling result
   */
  const schedulingResult = computed(() => {
    return props.value?.schedulingResult;
  });

  return {
    schedulingResult
  };
}

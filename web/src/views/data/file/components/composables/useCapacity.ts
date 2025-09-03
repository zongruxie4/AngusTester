import { ref, reactive, onMounted } from 'vue';
import { store } from '@/api/store';
import { space } from '@/api/storage';
import { appContext } from '@xcan-angus/infra';
import type { SourceType } from '../../components/type';

/**
 * Composable for managing capacity data and related operations
 * Handles loading capacity information, calculating usage percentages,
 * and managing upgrade URL for cloud disk storage
 *
 * @returns Object containing reactive state and methods for capacity management
 */
export function useCapacity () {
  // Reactive state for capacity data
  const state = reactive<{
    loading: boolean;
    source: SourceType;
  }>({
    loading: false,
    source: {}
  });

  // Computed percentage of space usage
  const percent = ref(0);

  // URL for upgrading cloud disk capacity
  const cloudDiskUrl = ref<string | undefined>();

  /**
   * Load cloud disk pricing URL for upgrade options
   * Only loads for non-private editions
   */
  const loadCloudDiskPayUrl = async (): Promise<void> => {
    const isPrivate = appContext.isPrivateEdition();
    if (isPrivate) {
      return;
    }

    const [error, res] = await store.getStoreList({ code: 'CloudDisk' });
    if (error) {
      return;
    }

    cloudDiskUrl.value = res.data.list?.[0]?.pricingUrl;
  };

  /**
   * Load capacity data for a specific space
   *
   * @param spaceId - ID of the space to load capacity for
   */
  const loadCapacity = async (spaceId: string): Promise<void> => {
    // Set loading state
    state.loading = true;

    try {
      const [error, res] = await space.getSpaceDetail(spaceId);
      if (error) {
        return;
      }

      // Update capacity data
      state.source = res.data.summary;

      // Calculate usage percentage (convert to percentage value)
      percent.value = Math.round(+(state.source.usage || 0) * 10000) / 100;
    } finally {
      // Reset loading state
      state.loading = false;
    }
  };

  /**
   * Initialize composable - load cloud disk URL on mount
   */
  const init = (): void => {
    onMounted(() => {
      loadCloudDiskPayUrl();
    });
  };

  return {
    // State
    state,
    percent,
    cloudDiskUrl,

    // Methods
    loadCapacity,
    init
  };
}

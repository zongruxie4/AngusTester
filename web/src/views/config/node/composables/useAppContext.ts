import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { appContext, utils } from '@xcan-angus/infra';

import { pubStore } from '@/api/store';

/**
 * Composable for managing application context
 *
 * @returns Object containing application context functions and state
 */
export function useAppContext () {
  const route = useRoute();

  // Application edition type
  const editionType = ref<string>();

  // Auto-refresh state for monitoring data
  const autoRefresh = ref(false);

  /**
   * Initializes application context information
   */
  const initializeAppContext = () => {
    try {
      editionType.value = appContext.getEditionType();
    } catch (error) {
      console.error('Failed to initialize app context:', error);
    }
  };

  /**
   * Checks if current user has admin privileges
   *
   * @returns True if user is admin, false otherwise
   */
  const isUserAdmin = (): boolean => {
    try {
      return appContext.isAdmin();
    } catch (error) {
      console.error('Failed to check admin privileges:', error);
      return false;
    }
  };

  /**
   * Handles purchase flow for cloud service nodes
   *
   * <p>Retrieves available goods versions and opens the pricing page
   * for the highest available version.</p>
   */
  const handlePurchase = async () => {
    try {
      const [error, res] = await pubStore.getGoodsVersion('CloudNode');

      if (error) {
        console.error('Failed to get goods version:', error);
        return;
      }

      const allVersions = (res.data || []).map((item: any) => item.version);
      const maxVersion = utils.maxVersion(allVersions);

      if (maxVersion) {
        const targetGoods = (res.data || []).find((goods: any) => goods.version === maxVersion);
        if (targetGoods?.pricingUrl) {
          window.open(targetGoods.pricingUrl);
        }
      }
    } catch (error) {
      console.error('Failed to handle purchase:', error);
    }
  };

  /**
   * Handles route query parameters for pre-filled search
   *
   * @param searchPanelRef - Reference to the search panel component
   */
  const handleRouteQuery = (searchPanelRef: any) => {
    if (route.query.id) {
      nextTick(() => {
        searchPanelRef.value?.setConfigs([{ valueKey: 'name', value: route.query.id }]);
      });
    }
  };

  /**
   * Toggles auto-refresh state for monitoring data
   */
  const toggleAutoRefresh = () => {
    autoRefresh.value = !autoRefresh.value;
  };

  // Initialize app context on mount
  onMounted(() => {
    initializeAppContext();
  });

  return {
    // State
    editionType,
    autoRefresh,

    // Methods
    initializeAppContext,
    isUserAdmin,
    handlePurchase,
    handleRouteQuery,
    toggleAutoRefresh
  };
}

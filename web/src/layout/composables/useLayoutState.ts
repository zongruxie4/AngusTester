import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { appContext } from '@xcan-angus/infra';

/**
 * Composable for managing layout state and context
 */
export function useLayoutState () {
  const route = useRoute();

  // Application context
  const appInfo = ref(appContext.getContext().accessApp);
  const defaultAppIcon = new URL('../../assets/images/AngusTester.png', import.meta.url).href;
  const defaultProjectImg = new URL('../../assets/images/default.png', import.meta.url).href;

  /**
   * Check if breadcrumb should be displayed
   */
  const hasBreadcrumb = computed((): boolean => {
    return !!(route.meta?.breadcrumb as Array<unknown>)?.length;
  });

  /**
   * Calculate main content style based on breadcrumb presence
   */
  const mainContentStyle = computed((): string => {
    return hasBreadcrumb.value ? 'height: calc(100% - 43px);' : 'height: 100%;';
  });

  return {
    appInfo,
    defaultAppIcon,
    defaultProjectImg,
    hasBreadcrumb,
    mainContentStyle
  };
}

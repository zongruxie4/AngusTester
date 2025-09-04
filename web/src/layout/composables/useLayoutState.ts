import { computed, provide, ref, ComputedRef, Ref } from 'vue';
import { useRoute } from 'vue-router';
import { appContext } from '@xcan-angus/infra';
import type { ProjectDisplayInfo, ProjectTypeVisibility } from '../types';

/**
 * Composable for managing layout state and context
 */
export function useLayoutState (
  currentProject: Ref<ProjectDisplayInfo | undefined>,
  projectTypeVisibility: ComputedRef<ProjectTypeVisibility>,
  changeProjectInfo: (projectId?: string, force?: boolean) => Promise<void>,
  loadProjectData: () => Promise<void>
) {
  const route = useRoute();

  // Application context
  const appInfo = ref(appContext.getContext().accessApp);
  const logoDefaultImg = new URL('../assets/AngusTester.png', import.meta.url).href;

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

  /**
   * Provide context to child components
   */
  const provideLayoutContext = (): void => {
    provide('proTypeShowMap', projectTypeVisibility.value);
    provide('projectInfo', currentProject);
    provide('changeProjectInfo', changeProjectInfo);
    provide('getNewCurrentProject', loadProjectData);
  };

  return {
    appInfo,
    logoDefaultImg,
    hasBreadcrumb,
    mainContentStyle,
    provideLayoutContext
  };
}

/**
 * Version management composable
 * Handles version routing, tab management, and navigation logic
 */

import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { software } from '@/api/tester';
import type { IPane } from '../types';

interface VersionManagementProps {
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
}

export function useVersionManagement(props: VersionManagementProps) {
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  // Component references
  const browserTabRef = ref();

  // Async components
  const List = defineAsyncComponent(() => import('../List.vue'));
  const Detail = defineAsyncComponent(() => import('../Detail.vue'));

  /**
   * Add new tab pane to browser tab component
   * @param data - Tab pane configuration
   */
  const addTabPane = (data: IPane): void => {
    browserTabRef.value.add(() => {
      return data;
    });
  };

  /**
   * Get tab pane data by key
   * @param key - Tab pane key
   * @returns Tab pane data or undefined
   */
  const getTabPane = (key: string): IPane[] | undefined => {
    return browserTabRef.value.getData(key);
  };

  /**
   * Delete tab panes by keys
   * @param keys - Array of tab pane keys to delete
   */
  const deleteTabPane = (keys: string[]): void => {
    browserTabRef.value.remove(keys);
  };

  /**
   * Update tab pane data
   * @param data - Updated tab pane data
   */
  const updateTabPane = (data: IPane): void => {
    browserTabRef.value.update(data);
  };

  /**
   * Replace tab pane with new data
   * @param key - Tab pane key to replace
   * @param data - New tab pane data
   */
  const replaceTabPane = (key: string, data: { key: string }): void => {
    browserTabRef.value.replace(key, data);
  };

  /**
   * Initialize version management system
   * Sets up default tab and processes current hash
   */
  const initialize = (): void => {
    if (typeof browserTabRef.value?.add === 'function') {
      browserTabRef.value.add((ids: string[]) => {
        if (!ids.includes('versionList')) {
          return {
            _id: 'versionList',
            value: 'versionList',
            name: t('version.title'),
            closable: false // Prevent closing of main version list tab
          };
        }
      });
    }

    hashChange(route.hash);
  };

  /**
   * Handle hash change events
   * Processes URL hash parameters and manages tab navigation
   * @param hash - URL hash string
   */
  const hashChange = async (hash: string): Promise<void> => {
    const queryString = hash.split('?')[1];
    if (!queryString) {
      return;
    }

    // Parse query parameters
    const queryParameters = queryString.split('&').reduce((prev, cur) => {
      const [key, value] = cur.split('=');
      prev[key] = value;
      return prev;
    }, {} as { [key: string]: string });

    const { id, type, name } = queryParameters;

    if (name || id) {
      if (type === 'edit') {
        // Add edit tab
        browserTabRef.value.add(() => {
          return {
            _id: id,
            value: 'versionEdit',
            noCache: true,
            data: { _id: id, id }
          };
        });
      } else {
        if (!id) {
          // Search by name and add detail tab
          const [_error, { data = {} }] = await software.getSoftwareVersionList({
            projectId: props.projectId,
            name
          });
          
          if (data?.list?.[0]) {
            browserTabRef.value.add(() => {
              return {
                _id: data?.list?.[0].id + '-detail',
                value: 'versionDetails',
                data: { _id: data?.list?.[0].id, id: data?.list?.[0].id }
              };
            });
            router.replace('/project#version');
          }
          return;
        }
        
        // Add detail tab for specific version
        browserTabRef.value.add(() => {
          return {
            _id: id + '-detail',
            value: 'versionDetails',
            data: { _id: id, id }
          };
        });
      }
    } else {
      if (type) {
        // Add new version creation tab
        browserTabRef.value.add(() => {
          const id = utils.uuid();
          return {
            _id: id,
            name: t('version.form.addVersion'),
            value: 'versionEdit',
            noCache: true,
            data: { _id: id }
          };
        });
      }
    }
    
    router.replace('/project#version');
  };

  /**
   * Handle storage key changes
   * Reinitializes system when storage changes
   */
  const storageKeyChange = (): void => {
    initialize();
  };

  /**
   * Computed storage key for tab persistence
   * Unique key per project for tab state management
   */
  const storageKey = computed(() => {
    if (!props.projectId) {
      return undefined;
    }
    return `version${props.projectId}`;
  });

  // Watch for hash changes and initialize on mount
  onMounted(() => {
    watch(() => route.hash, () => {
      if (!route.hash.startsWith('#version')) {
        return;
      }
      hashChange(route.hash);
    });
  });

  // Provide tab management functions to child components
  provide('addTabPane', addTabPane);
  provide('getTabPane', getTabPane);
  provide('deleteTabPane', deleteTabPane);
  provide('updateTabPane', updateTabPane);
  provide('replaceTabPane', replaceTabPane);

  return {
    browserTabRef,
    List,
    Detail,
    storageKey,
    storageKeyChange
  };
}

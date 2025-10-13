import { computed } from 'vue';
import { utils } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/types';

/**
 * Script info display management composable
 * Handles script information formatting and display
 */
export function useScriptInfo (props: any) {
  /**
   * Computed data source
   */
  const dataSource = computed(() => {
    return props.dataSource || {};
  });

  /**
   * Process tags for display
   */
  const tags = computed(() => {
    return dataSource.value.tags?.map((item: string) => {
      return {
        id: utils.uuid(),
        name: item
      };
    }) || [];
  });

  /**
   * Generate source name link URL
   */
  const sourceNameLinkUrl = computed(() => {
    const { source, name, plugin, sourceId } = dataSource.value;
    const sourceValue = source?.value;
    if (sourceValue === 'SERVICE_SMOKE') {
      return `/apis#${ApiMenuKey.SERVICES}?id=${sourceId}&name=${name}&value=group`;
    } else if (sourceValue === 'SERVICE_SECURITY') {
      return `/apis#${ApiMenuKey.SERVICES}?id=${sourceId}&name=${name}&value=group`;
    } else if (sourceValue === 'API') {
      return `/apis#${ApiMenuKey.SERVICES}?id=${sourceId}&name=${name}&value=API`;
    } else if (sourceValue === 'SCENARIO') {
      return `/scenario#scenario?id=${sourceId}&name=${name}&plugin=${plugin}`;
    }
    return '';
  });

  return {
    dataSource,
    tags,
    sourceNameLinkUrl
  };
}

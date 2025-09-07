import { computed, ref, watch } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration, GM } from '@xcan-angus/infra';
import type { GroupSetProps } from '../types';

/**
 * Interface for list item data
 */
interface ListInfo {
  [key: string]: string;
  avatar: string;
}

/**
 * Composable for managing group/user/dept data and selection
 */
export function useGroupData (props: GroupSetProps) {
  // Reactive state
  const dataSource = ref<ListInfo[]>([]);
  const inputValue = ref<string>();
  const activeId = ref<string>();

  // Configuration based on type
  const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
  const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
  const placeholder = ref<string>();
  const apiPath = ref<string>();

  // Computed search parameters
  const searchParams = computed(() => {
    const value = inputValue.value?.trim();
    if (value) {
      return {
        filters: [{
          key: props.type === 'user' ? 'fullName' : 'name',
          op: 'MATCH_END' as const,
          value
        }]
      };
    }
    return {};
  });

  /**
   * Handle scroll data change
   */
  const handleScrollChange = (data: ListInfo[]) => {
    dataSource.value = data;

    if (!activeId.value && data.length > 0) {
      const id = data[0]?.id;
      activeId.value = id;
      return id;
    }

    return activeId.value;
  };

  /**
   * Handle input change with debounce
   */
  const handleInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
    inputValue.value = event.target.value?.trim();
  });

  /**
   * Handle item selection
   */
  const handleItemSelect = (id: string) => {
    activeId.value = id;
    return id;
  };

  /**
   * Initialize configuration based on type and appId
   */
  const initializeConfig = () => {
    if (!props.appId) return;

    switch (props.type) {
      case 'dept':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = 'scenario.auth.groupSet.searchPlaceholders.dept';
        apiPath.value = `${GM}/app/${props.appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = 'scenario.auth.groupSet.searchPlaceholders.group';
        apiPath.value = `${GM}/app/${props.appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = 'scenario.auth.groupSet.searchPlaceholders.user';
        apiPath.value = `${GM}/app/${props.appId}/auth/user`;
        break;
    }
  };

  // Watch for type and appId changes
  watch([() => props.appId, () => props.type], () => {
    initializeConfig();
  }, { immediate: true });

  return {
    // State
    dataSource,
    inputValue,
    activeId,
    nameKey,
    idKey,
    placeholder,
    apiPath,
    searchParams,

    // Methods
    handleScrollChange,
    handleInputChange,
    handleItemSelect,
    initializeConfig
  };
}

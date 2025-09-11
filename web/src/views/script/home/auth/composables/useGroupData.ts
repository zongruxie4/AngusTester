import { computed, ref, watch } from 'vue';
import { debounce } from 'throttle-debounce';
import { GM, duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

/**
 * <p>
 * Interface for list item information containing avatar and other properties.
 * </p>
 */
interface ListInfo {
  [key: string]: string;
  avatar: string;
}

/**
 * <p>
 * Composable for managing group data including users, departments, and groups.
 * Handles data loading, searching, and selection logic for different entity types.
 * </p>
 */
export function useGroupData (type: 'user' | 'dept' | 'group', appId: string | undefined, visible: boolean) {
  const { t } = useI18n();

  // Data state
  const dataSource = ref<ListInfo[]>([]);
  const inputValue = ref<string>();
  const activeId = ref<string>();
  const notify = ref(0);

  // Configuration based on type
  const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
  const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
  const placeholder = ref<string>();
  const apiPath = ref<string>();

  /**
   * <p>
   * Computed parameters for API calls including search filters.
   * Returns filters object when search input has value.
   * </p>
   */
  const params = computed<{ filters?: [{ key: 'fullName' | 'name'; op: 'MATCH'; value: string }] }>(() => {
    const value = inputValue.value?.trim();
    if (value) {
      return {
        filters: [{ key: type === 'user' ? 'fullName' : 'name', op: 'MATCH', value }]
      };
    }

    return {};
  });

  /**
   * <p>
   * Handles scroll change events from the Scroll component.
   * Updates data source and sets initial active ID if none is selected.
   * </p>
   */
  const scrollChange = (data: ListInfo[]) => {
    dataSource.value = data;
    if (!activeId.value) {
      const id = data[0]?.id;
      activeId.value = id;
      return id;
    }
    return undefined;
  };

  /**
   * <p>
   * Debounced input change handler for search functionality.
   * Updates input value and triggers search.
   * </p>
   */
  const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
    inputValue.value = event.target.value?.trim();
  });

  /**
   * <p>
   * Handles item selection by updating active ID.
   * </p>
   */
  const checkedHandler = (id: string) => {
    activeId.value = id;
    return id;
  };

  /**
   * <p>
   * Initializes configuration based on entity type and app ID.
   * Sets up API paths, field names, and placeholders.
   * </p>
   */
  const initializeConfig = () => {
    if (!appId) {
      return;
    }

    switch (type) {
      case 'dept':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.deptPlaceholder');
        apiPath.value = `${GM}/app/${appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.groupPlaceholder');
        apiPath.value = `${GM}/app/${appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.userPlaceholder');
        apiPath.value = `${GM}/app/${appId}/auth/user`;
        break;
    }
  };

  /**
   * <p>
   * Resets component state when visibility changes.
   * Clears active ID, input value, and increments notify counter.
   * </p>
   */
  const resetState = () => {
    activeId.value = undefined;
    inputValue.value = undefined;
    notify.value++;
  };

  // Initialize configuration immediately
  initializeConfig();

  // Watchers
  watch([() => appId, () => type], ([_appId, _type]) => {
    if (!_appId) {
      return;
    }

    switch (_type) {
      case 'dept':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.deptPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.groupPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.userPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/user`;
        break;
    }
  }, { immediate: true });

  watch(() => visible, (newValue) => {
    if (!newValue) {
      return;
    }
    resetState();
  }, { immediate: true });

  return {
    // State
    dataSource,
    inputValue,
    activeId,
    notify,
    nameKey,
    idKey,
    placeholder,
    apiPath,
    params,

    // Methods
    scrollChange,
    inputChange,
    checkedHandler,
    initializeConfig,
    resetState
  };
}

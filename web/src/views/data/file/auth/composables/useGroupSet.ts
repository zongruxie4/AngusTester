import { computed, ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { GM, duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import type { GroupSetProps, GroupSetEmits, ListInfo } from '../types';

/**
 * <p>
 * Group set management composable for handling user, department, and group selection
 * </p>
 * <p>
 * This composable manages the selection state, API configuration, and data loading
 * for different types of entities (users, departments, groups)
 * </p>
 */
export function useGroupSet (
  props: GroupSetProps,
  emit: GroupSetEmits
) {
  const { t } = useI18n();

  // Reactive state
  const dataSource = ref<ListInfo[]>([]);
  const inputValue = ref<string>();
  const activeId = ref<string>();
  const notify = ref(0);

  // Configuration keys based on entity type
  const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
  const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
  const placeholder = ref<string>();
  const apiPath = ref<string>();

  /**
   * <p>
   * Computed parameters for API calls with search filters
   * </p>
   */
  const params = computed<{ filters?: [{ key: 'fullName' | 'name'; op: 'MATCH'; value: string }] }>(() => {
    const value = inputValue.value?.trim();
    if (value) {
      return {
        filters: [{ key: props.type === 'user' ? 'fullName' : 'name', op: 'MATCH', value }]
      };
    }
    return {};
  });

  /**
   * <p>
   * Handles scroll data changes and sets initial active item
   * </p>
   *
   * @param data - Array of list items from scroll component
   */
  const scrollChange = (data: ListInfo[]) => {
    dataSource.value = data;

    // Set first item as active if no active item
    if (!activeId.value) {
      const id = data[0]?.id;
      activeId.value = id;
      emit('update:checkedId', id);
    }

    // Mark as loaded if not already
    if (!props.loaded) {
      emit('update:loaded', true);
    }
  };

  /**
   * <p>
   * Handles input change with debouncing for search
   * </p>
   *
   * @param event - Input change event
   */
  const inputChange = debounce(duration.search, (event: Event) => {
    const target = event.target as HTMLInputElement;
    inputValue.value = target?.value?.trim();
  });

  /**
   * <p>
   * Handles item selection
   * </p>
   *
   * @param id - ID of the selected item
   */
  const checkedHandler = (id: string) => {
    activeId.value = id;
    emit('update:checkedId', id);
  };

  /**
   * <p>
   * Sets up configuration based on entity type and app ID
   * </p>
   */
  const setupConfiguration = () => {
    if (!props.appId) {
      return;
    }

    switch (props.type) {
      case 'dept':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('fileSpace.globalAuth.groupSet.placeholders.dept');
        apiPath.value = `${GM}/app/${props.appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('fileSpace.globalAuth.groupSet.placeholders.group');
        apiPath.value = `${GM}/app/${props.appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = t('fileSpace.globalAuth.groupSet.placeholders.user');
        apiPath.value = `${GM}/app/${props.appId}/auth/user`;
        break;
    }
  };

  /**
   * <p>
   * Resets component state when visibility changes
   * </p>
   */
  const resetState = () => {
    activeId.value = undefined;
    inputValue.value = undefined;
    notify.value++;
  };

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

    // Computed
    params,

    // Methods
    scrollChange,
    inputChange,
    checkedHandler,
    setupConfiguration,
    resetState
  };
}

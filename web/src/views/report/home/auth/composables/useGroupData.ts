import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { duration, GM } from '@xcan-angus/infra';

interface ListInfo {
  [key: string]: string;
  avatar: string;
}

/**
 * Composable for managing group data and related operations
 * @param appId - Application ID
 * @param type - Group type (user, dept, group)
 */
export function useGroupData (appId: string | undefined, type: 'user' | 'dept' | 'group') {
  const { t } = useI18n();

  // Reactive references
  const dataSource = ref<ListInfo[]>([]);
  const inputValue = ref<string>();
  const activeId = ref<string>();
  const notify = ref(0);

  // Keys for different group types
  const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
  const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
  const placeholder = ref<string>();
  const apiPath = ref<string>();

  /**
   * Computed parameters for API requests
   */
  const params = computed<{
    filters?: [{ key: 'fullName' | 'name'; op: 'MATCH_END'; value: string }]
  }>(() => {
    const value = inputValue.value?.trim();
    if (value) {
      return {
        filters: [{ key: type === 'user' ? 'fullName' : 'name', op: 'MATCH_END', value }]
      };
    }

    return {};
  });

  /**
   * Handle scroll change event
   * @param data - New data from scroll
   */
  const handleScrollChange = (data: ListInfo[]) => {
    dataSource.value = data;
    if (!activeId.value) {
      const id = data[0]?.id;
      activeId.value = id;
    }
  };

  /**
   * Handle item selection
   * @param id - Selected item ID
   */
  const handleChecked = (id: string) => {
    activeId.value = id;
  };

  /**
   * Handle input change with debounce
   */
  const handleInputChange = debounce(duration.search, (event: {target:{value:string}}) => {
    inputValue.value = event.target.value?.trim();
  });

  /**
   * Initialize component based on type and appId
   */
  const init = () => {
    watch([() => appId, () => type], ([_appId, _type]) => {
      if (!_appId) {
        return;
      }

      switch (_type) {
        case 'dept':
          nameKey.value = 'name';
          idKey.value = 'id';
          placeholder.value = t('reportHome.globalAuth.groupSet.searchDept');
          apiPath.value = `${GM}/app/${_appId}/auth/dept`;
          break;
        case 'group':
          nameKey.value = 'name';
          idKey.value = 'id';
          placeholder.value = t('reportHome.globalAuth.groupSet.searchGroup');
          apiPath.value = `${GM}/app/${_appId}/auth/group`;
          break;
        case 'user':
          nameKey.value = 'fullName';
          idKey.value = 'id';
          placeholder.value = t('reportHome.globalAuth.groupSet.searchUser');
          apiPath.value = `${GM}/app/${_appId}/auth/user`;
      }
    }, { immediate: true });
  };

  /**
   * Reset component state
   */
  const reset = () => {
    activeId.value = undefined;
    inputValue.value = undefined;
    notify.value++;
  };

  return {
    // Reactive data
    dataSource,
    inputValue,
    activeId,
    notify,
    nameKey,
    idKey,
    placeholder,
    apiPath,

    // Computed properties
    params,

    // Methods
    handleScrollChange,
    handleChecked,
    handleInputChange,
    init,
    reset
  };
}

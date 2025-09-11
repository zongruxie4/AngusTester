import { computed, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { appContext, AuthObjectType, duration, GM } from '@xcan-angus/infra';
import { auth } from 'src/api/gm';
import type { Params, Policy, TableData } from '../types';

/**
 * Composable for managing member data and related operations
 * @param activeKey - The currently active member type (USER, DEPT, GROUP)
 * @param appId - The application ID
 */
export function useMemberData (activeKey: AuthObjectType, appId: string) {
  const { t } = useI18n();
  const appInfo = ref(appContext.getAccessApp());

  // Member list data
  const tableList = ref<TableData[]>([]);
  const reloadNo = ref(0);
  const selectId = ref('');

  // Policy data
  const policyData = ref<Policy[]>([]);
  const policyKeyword = ref<string>();
  const policyLoading = ref(false);

  // Pagination for policies
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  });

  // Search parameters
  const params: Params = reactive({
    filters: []
  });

  /**
   * Computed property for the button name based on active member type
   */
  const btnName = computed(() => {
    switch (activeKey) {
      case AuthObjectType.USER:
        return t('app.config.memberList.buttons.addMember');
      case AuthObjectType.DEPT:
        return t('app.config.memberList.buttons.addDept');
      case AuthObjectType.GROUP:
        return t('app.config.memberList.buttons.addGroup');
      default:
        return '';
    }
  });

  /**
   * Computed property for search placeholder based on active member type
   */
  const placeholder = computed(() => {
    switch (activeKey) {
      case AuthObjectType.USER:
        return t('app.config.memberList.placeholders.searchMember');
      case AuthObjectType.DEPT:
        return t('app.config.memberList.placeholders.searchDept');
      case AuthObjectType.GROUP:
        return t('app.config.memberList.placeholders.searchGroup');
      default:
        return '';
    }
  });

  /**
   * Computed property for API endpoint URL based on active member type
   * Added null/undefined checks for appId
   */
  const urlPath = computed(() => {
    // Ensure we have valid appInfo and appId before constructing URLs
    const validAppId = appId || '';

    if (!appInfo.value || !validAppId) {
      return '';
    }
    switch (activeKey) {
      case AuthObjectType.USER:
        return `${GM}/app/${validAppId}/auth/user`;
      case AuthObjectType.DEPT:
        return `${GM}/app/${validAppId}/auth/dept`;
      case AuthObjectType.GROUP:
        return `${GM}/app/${validAppId}/auth/group`;
      default:
        return '';
    }
  });

  /**
   * Debounced search input handler
   */
  const handleInputChange = debounce(duration.search, (event: any) => {
    const value = event.target.value;
    if (value) {
      params.filters[0] = {
        key: activeKey === AuthObjectType.USER ? 'fullName' : 'name',
        op: 'MATCH',
        value: value.trim()
      };
    } else {
      params.filters = [];
    }
  });

  /**
   * Handler for when member list data changes
   * @param val - The new member list data
   */
  const handleChange = (val: TableData[]) => {
    tableList.value = val;
    if (!selectId.value && val.length) {
      selectId.value = tableList.value[0].id;
    }
  };

  /**
   * Select a member by ID
   * @param id - The ID of the member to select
   */
  const selectMember = (id: string) => {
    selectId.value = id;
  };

  /**
   * Get tenant type name for display
   * @param record - The policy record
   * @returns Formatted tenant type name
   */
  const getTenantTypeName = (record: any) => {
    const result: string[] = [];
    if (record?.currentDefault) {
      result.push(t('app.config.memberList.table.sources.appDefault'));
    }
    if (record?.openAuth) {
      result.push(t('app.config.memberList.table.sources.openAuth'));
    }
    return result.join(',');
  };

  // API configuration for policy deletion
  const deletePolicyApiConfig = {
    USER: auth.deleteUserPolicy,
    DEPT: auth.delPolicyByDept,
    GROUP: auth.deleteGroupPolicy
  };

  /**
   * Cancel authorization for a policy
   * @param policyIds - The IDs of policies to cancel authorization for
   */
  const handleCancel = async (policyIds: string) => {
    if (policyLoading.value) {
      return;
    }
    policyLoading.value = true;
    await deletePolicyApiConfig[activeKey](selectId.value, [policyIds]);
    policyLoading.value = false;

    // If this was the last policy, reload the member list
    if (policyData.value.length === 1) {
      selectId.value = '';
      reloadNo.value++;
      return;
    }

    // Otherwise, reload policies for the current member
    await loadPolicies();
  };

  // API configuration for policy loading
  const policyApiConfig = {
    USER: auth.getUserPolicyList,
    GROUP: auth.getGroupPolicy,
    DEPT: auth.getDeptPolicy
  };

  /**
   * Load policies for the currently selected member
   */
  const loadPolicies = async () => {
    if (policyLoading.value) {
      return;
    }
    policyLoading.value = true;

    const { current, pageSize } = pagination;
    const filters = policyKeyword.value
      ? [{ key: 'name', value: policyKeyword.value, op: 'MATCH' }]
      : [];

    const [error, res] = await policyApiConfig[activeKey](
      selectId.value,
      {
        appId: appInfo.value?.id || '',
        pageNo: current,
        pageSize,
        filters
      }
    );

    policyLoading.value = false;
    if (error) {
      return;
    }

    policyData.value = res.data.list || [];
    pagination.total = +res.data.total;
  };

  /**
   * Watch for changes to the selected member ID and load their policies
   */
  watch(() => selectId.value, newValue => {
    policyData.value = [];
    if (newValue) {
      loadPolicies();
    }
  });

  /**
   * Watch for changes to the policy search keyword and reload policies
   */
  watch(() => policyKeyword.value, debounce(duration.search, () => {
    pagination.current = 1;
    loadPolicies();
  }));

  return {
    // Reactive data
    tableList,
    reloadNo,
    selectId,
    policyData,
    policyKeyword,
    policyLoading,
    params,
    pagination,

    // Computed properties
    btnName,
    placeholder,
    urlPath,

    // Methods
    handleInputChange,
    handleChange,
    selectMember,
    getTenantTypeName,
    handleCancel,
    loadPolicies
  };
}

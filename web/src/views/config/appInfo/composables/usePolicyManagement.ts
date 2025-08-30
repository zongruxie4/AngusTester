import { computed, ref, watch, type Ref } from 'vue';
import { AuthObjectType, GM } from '@xcan-angus/infra';
import { auth } from 'src/api/gm';
import type { PolicyItem } from '../types';

/**
 * Composable for managing policy-related operations
 * @param activeKey - The currently active member type (USER, DEPT, GROUP)
 * @param appId - The application ID
 * @param selectId - The ID of the currently selected member (ref or string)
 * @param userId - The user ID (ref, optional)
 * @param deptId - The department ID (ref, optional)
 * @param groupId - The group ID (ref, optional)
 */
export function usePolicyManagement (
  activeKey: AuthObjectType,
  appId: string,
  selectId: Ref<string> | string,
  userId?: Ref<string> | string,
  deptId?: Ref<string> | string,
  groupId?: Ref<string> | string
) {
  // Policy modal data
  const params = ref<{
    filters: { key: 'name'; op: 'MATCH_END'; value: string | undefined }[];
    enabled: boolean;
    appId: string
  }>({
    filters: [],
    enabled: true,
    appId
  });

  const notify = ref(0);
  const dataList = ref<PolicyItem[]>([]);
  const checkedList = ref<string[]>([]);
  const indeterminate = ref(false);
  const loading = ref(false);

  // Create a reactive reference to the current selectId
  const currentSelectId = ref(typeof selectId === 'string' ? selectId : selectId.value);

  // Watch for changes in selectId and update currentSelectId
  if (typeof selectId !== 'string') {
    watch(selectId, (newSelectId) => {
      currentSelectId.value = newSelectId;
    }, { immediate: true });
  }

  /**
   * Computed property for policy modal type based on active member type
   */
  const policyModalType = computed(() => {
    return activeKey;
  });

  /**
   * Computed property for API endpoint URL based on active member type
   * Added null/undefined checks for member IDs
   */
  const action = computed(() => {
    // Ensure we have valid IDs before constructing URLs
    const validUserId = typeof userId === 'string' ? userId : userId?.value || currentSelectId.value || '';
    const validDeptId = typeof deptId === 'string' ? deptId : deptId?.value || currentSelectId.value || '';
    const validGroupId = typeof groupId === 'string' ? groupId : groupId?.value || currentSelectId.value || '';

    switch (activeKey) {
      case AuthObjectType.USER:
        return validUserId ? `${GM}/auth/user/${validUserId}/unauth/policy` : '';
      case AuthObjectType.DEPT:
        return validDeptId ? `${GM}/auth/dept/${validDeptId}/unauth/policy` : '';
      case AuthObjectType.GROUP:
        return validGroupId ? `${GM}/auth/group/${validGroupId}/unauth/policy` : '';
      default:
        return '';
    }
  });

  /**
   * Handle "select all" checkbox change
   * @param e - The checkbox change event
   */
  const onCheckAllChange = (e: any) => {
    if (e.target.checked) {
      checkedList.value = dataList.value.map(m => m.id);
      indeterminate.value = true;
    } else {
      indeterminate.value = false;
      checkedList.value = [];
    }
  };

  /**
   * Handle policy data change from Scroll component
   * @param value - The new policy data
   */
  const handleChange = (value: PolicyItem[]) => {
    dataList.value = value;
  };

  /**
   * Add policies to different member types
   * Added null/undefined checks for selectId
   * @param addIds - Array of policy IDs to add
   * @param onSuccess - Callback function to execute on successful policy addition
   */
  const policySave = async (addIds: string[], onSuccess?: () => void) => {
    // Check if we have valid IDs and addIds before proceeding
    if (!addIds.length || !currentSelectId.value) {
      console.warn('Missing required parameters for policySave:', { selectId: currentSelectId.value, addIds });
      return;
    }

    let success = false;
    switch (activeKey) {
      case AuthObjectType.USER:
        success = await addUserPolicy(addIds);
        break;
      case AuthObjectType.DEPT:
        success = await addDeptPolicy(addIds);
        break;
      default:
        success = await addGroupPolicy(addIds);
        break;
    }

    // If policy addition was successful, call the onSuccess callback
    if (success && onSuccess) {
      onSuccess();
    }
  };

  /**
   * Add policies to a user
   * Added null/undefined checks for selectId
   * @param addIds - Array of policy IDs to add
   * @returns boolean indicating success or failure
   */
  const addUserPolicy = async (addIds: string[]): Promise<boolean> => {
    // Validate inputs before proceeding
    if (loading.value || !currentSelectId.value || !addIds.length) {
      console.warn('Invalid parameters for addUserPolicy:', { loading: loading.value, selectId: currentSelectId.value, addIds });
      return false;
    }
    loading.value = true;
    try {
      await auth.createUserPolicy(currentSelectId.value, addIds);
      return true;
    } catch (error) {
      console.error('Error in addUserPolicy:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Add policies to a department
   * Added null/undefined checks for selectId
   * @param addIds - Array of policy IDs to add
   * @returns boolean indicating success or failure
   */
  const addDeptPolicy = async (addIds: string[]): Promise<boolean> => {
    // Validate inputs before proceeding
    if (loading.value || !currentSelectId.value || !addIds.length) {
      console.warn('Invalid parameters for addDeptPolicy:', { loading: loading.value, selectId: currentSelectId.value, addIds });
      return false;
    }
    loading.value = true;
    try {
      await auth.addPolicyByDept(currentSelectId.value, addIds);
      return true;
    } catch (error) {
      console.error('Error in addDeptPolicy:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  /**
   * Add policies to a group
   * Added null/undefined checks for selectId
   * @param addIds - Array of policy IDs to add
   * @returns boolean indicating success or failure
   */
  const addGroupPolicy = async (addIds: string[]): Promise<boolean> => {
    // Validate inputs before proceeding
    if (loading.value || !currentSelectId.value || !addIds.length) {
      console.warn('Invalid parameters for addGroupPolicy:', { loading: loading.value, selectId: currentSelectId.value, addIds });
      return false;
    }
    loading.value = true;
    try {
      await auth.createGroupPolicy(currentSelectId.value, addIds);
      return true;
    } catch (error) {
      console.error('Error in addGroupPolicy:', error);
      return false;
    } finally {
      loading.value = false;
    }
  };

  return {
    // Reactive data
    params,
    notify,
    dataList,
    checkedList,
    indeterminate,
    loading,

    // Computed properties
    policyModalType,
    action,

    // Methods
    onCheckAllChange,
    handleChange,
    policySave,
    addUserPolicy,
    addDeptPolicy,
    addGroupPolicy
  };
}

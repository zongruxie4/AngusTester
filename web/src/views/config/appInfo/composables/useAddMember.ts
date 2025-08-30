import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, AuthObjectType, GM } from '@xcan-angus/infra';
import { app } from 'src/api/gm';

/**
 * Composable for managing add member functionality
 * @param type - The member type to add (USER, DEPT, GROUP)
 * @param appId - The application ID
 */
export function useAddMember (type: AuthObjectType, appId: string) {
  const { t } = useI18n();
  const userInfo = ref(appContext.getUser());

  // Form data
  const selectedUserIds = ref<string[] | undefined>();
  const userError = ref(false);
  const selectedPolicyIds = ref<string[]>([]);
  const policyError = ref(false);

  /**
   * Validate user/department/group selection
   * @param value - The selected value
   */
  const userChange = (value: string[] | undefined) => {
    userError.value = !value || value.length === 0;
  };

  /**
   * Validate policy selection
   * @param value - The selected policy IDs
   */
  const policyChange = (value: string[]) => {
    policyError.value = !value.length;
  };

  /**
   * Computed property for modal title based on member type
   */
  const title = computed(() => {
    switch (type) {
      case AuthObjectType.USER:
        return t('app.config.addMembers.title.user');
      case AuthObjectType.DEPT:
        return t('app.config.addMembers.title.dept');
      case AuthObjectType.GROUP:
        return t('app.config.addMembers.title.group');
      default:
        return '';
    }
  });

  /**
   * Computed property for selection placeholder based on member type
   */
  const placeholder = computed(() => {
    switch (type) {
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
   * Computed property for API endpoint URL based on member type
   * Added null/undefined checks for appId
   */
  const selectAction = computed(() => {
    // Ensure we have a valid appId before constructing URLs
    const validAppId = appId || '';

    if (!validAppId) {
      console.warn('appId is empty or undefined in useAddMember composable');
      return '';
    }

    switch (type) {
      case AuthObjectType.USER:
        return `${GM}/app/${validAppId}/unauth/user`;
      case AuthObjectType.DEPT:
        return `${GM}/app/${validAppId}/unauth/dept`;
      case AuthObjectType.GROUP:
        return `${GM}/app/${validAppId}/unauth/group`;
      default:
        return `${GM}/app/${validAppId}/unauth/user`;
    }
  });

  /**
   * Computed property for policy API endpoint URL
   * Added null/undefined checks for appId and userInfo.id
   */
  const policyAction = computed(() => {
    const validAppId = appId || '';
    const validUserId = userInfo.value?.id || '';

    if (!validAppId || !validUserId) {
      return '';
    }
    return `${GM}/auth/user/${validUserId}/policy/associated?appId=${validAppId}&distinct=true`;
  });

  /**
   * Computed property for field names based on member type
   */
  const fieldNames = computed(() => {
    switch (type) {
      case AuthObjectType.USER:
        return { label: 'fullName', value: 'id' };
      case AuthObjectType.DEPT:
        return { label: 'name', value: 'id' };
      case AuthObjectType.GROUP:
        return { label: 'name', value: 'id' };
      default:
        return { label: 'fullName', value: 'id' };
    }
  });

  /**
   * Computed property for selection label based on member type
   */
  const selectLabel = computed(() => {
    switch (type) {
      case AuthObjectType.USER:
        return t('app.config.addMembers.labels.selectUser');
      case AuthObjectType.DEPT:
        return t('app.config.addMembers.labels.selectDept');
      case AuthObjectType.GROUP:
        return t('app.config.addMembers.labels.selectGroup');
      default:
        return '';
    }
  });

  /**
   * Computed property for selection label based on member type
   */
  const columnLabel = computed(() => {
    switch (type) {
      case AuthObjectType.USER:
        return t('app.config.addMembers.columns.user');
      case AuthObjectType.DEPT:
        return t('app.config.addMembers.columns.dept');
      case AuthObjectType.GROUP:
        return t('app.config.addMembers.columns.group');
      default:
        return '';
    }
  });

  /**
   * Add departments to the application
   * @param params - The parameters containing org IDs and policy IDs
   */
  const addDepts = async (params: { orgIds: string[]; policyIds: string[] }): Promise<void> => {
    const [err] = await app.addAuthDept(appId, params);
    return err ? Promise.reject(err) : Promise.resolve();
  };

  /**
   * Add users to the application
   * @param params - The parameters containing org IDs and policy IDs
   */
  const addUsers = async (params: { orgIds: string[]; policyIds: string[] }): Promise<void> => {
    const [err] = await app.addAuthUser(appId, params);
    return err ? Promise.reject(err) : Promise.resolve();
  };

  /**
   * Add groups to the application
   * @param params - The parameters containing org IDs and policy IDs
   */
  const addGroups = async (params: { orgIds: string[]; policyIds: string[] }): Promise<void> => {
    const [err] = await app.addAuthGroup(appId, params);
    return err ? Promise.reject(err) : Promise.resolve();
  };

  /**
   * Handle form submission
   * @param emit - The emit function to notify parent component
   */
  const handleSubmit = async (emit: (event: 'update', refresh: boolean) => void) => {
    if (!selectedUserIds.value || selectedUserIds.value.length === 0) {
      userError.value = true;
      return false;
    }
    if (!selectedPolicyIds.value.length) {
      policyError.value = true;
      return false;
    }
    const params = {
      orgIds: selectedUserIds.value,
      policyIds: selectedPolicyIds.value
    };

    try {
      switch (type) {
        case AuthObjectType.USER:
          await addUsers(params);
          break;
        case AuthObjectType.DEPT:
          await addDepts(params);
          break;
        default:
          await addGroups(params);
          break;
      }
      emit('update', true);
      return true;
    } catch (err) {
      emit('update', false);
      return false;
    }
  };

  return {
    // Reactive data
    selectedUserIds,
    userError,
    selectedPolicyIds,
    policyError,

    // Computed properties
    title,
    placeholder,
    selectAction,
    policyAction,
    fieldNames,
    selectLabel,
    columnLabel,

    // Methods
    userChange,
    policyChange,
    handleSubmit
  };
}

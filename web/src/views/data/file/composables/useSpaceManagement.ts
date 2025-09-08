import { computed, ref } from 'vue';
import { space } from '@/api/storage';
import { SpaceInfoType } from '../types';

/**
 * <p>Composable for managing space operations including creation, editing,</p>
 * <p>and permission management.</p>
 *
 * @returns Object containing state and methods for space management
 */
export function useSpaceManagement () {
  // Modal visibility states
  const editVisible = ref(false);
  const authModalVisible = ref(false);
  const globalAuthVisible = ref(false);

  // Selected space information
  const selectId = ref<string>();
  const auth = ref<string[]>([]);
  const selectName = ref<string>();

  /**
   * <p>Open edit modal for existing space.</p>
   *
   * @param id - Space ID to edit
   */
  const editSpace = (id: string) => {
    editVisible.value = true;
    selectId.value = id;
  };

  /**
   * <p>Open create modal for new space.</p>
   */
  const createSpace = () => {
    editVisible.value = true;
    selectId.value = undefined;
  };

  /**
   * <p>Open global authorization modal.</p>
   */
  const openAuthorizeModal = () => {
    globalAuthVisible.value = true;
  };

  /**
   * <p>Open permission edit modal for specific space.</p>
   *
   * @param record - Space record to edit permissions for
   */
  const editAuth = (record: SpaceInfoType) => {
    selectId.value = record.id;
    auth.value = record.auth || [];
    authModalVisible.value = true;
  };

  /**
   * <p>Handle authorization changes and update local state.</p>
   *
   * @param authData - New authorization data
   * @param dataList - Current space list
   */
  const authFlagChange = ({ auth: authData }: { auth: string[] }, dataList: SpaceInfoType[]) => {
    const targetId = selectId.value;
    for (let i = 0, len = dataList.length; i < len; i++) {
      if (dataList[i].id === targetId) {
        dataList[i].auth = authData;
        break;
      }
    }
  };

  /**
   * <p>Save space data (create or update).</p>
   *
   * @param form - Form data to save
   * @param projectId - Current project ID
   * @param onSuccess - Callback function to execute on successful save
   */
  const saveSpace = async (
    form: any,
    projectId: string,
    onSuccess: () => void
  ) => {
    const [error] = await (
      form.id
        ? space.patchSpace({ ...form })
        : space.addSpace({ ...form, projectId })
    );

    if (error) {
      return;
    }

    editVisible.value = false;
    onSuccess();
  };

  /**
   * <p>Computed value for default IDs in modals.</p>
   */
  const defaultIds = computed(() => {
    return selectId.value ? [selectId.value] : [];
  });

  /**
   * <p>Close edit modal and reset form state.</p>
   */
  const closeEditModal = () => {
    editVisible.value = false;
    selectId.value = undefined;
  };

  /**
   * <p>Close auth modal and reset state.</p>
   */
  const closeAuthModal = () => {
    authModalVisible.value = false;
    selectId.value = undefined;
    auth.value = [];
  };

  /**
   * <p>Close global auth modal.</p>
   */
  const closeGlobalAuthModal = () => {
    globalAuthVisible.value = false;
  };

  return {
    // State
    editVisible,
    authModalVisible,
    globalAuthVisible,
    selectId,
    auth,
    selectName,

    // Computed
    defaultIds,

    // Methods
    editSpace,
    createSpace,
    openAuthorizeModal,
    editAuth,
    authFlagChange,
    saveSpace,
    closeEditModal,
    closeAuthModal,
    closeGlobalAuthModal
  };
}

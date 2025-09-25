import { ref, reactive, watch, onMounted } from 'vue';
import { space } from '@/api/storage';
import { notification } from '@xcan-angus/vue-ui';
import { toClipboard } from '@xcan-angus/infra';
import type { ShareListItem, ShareListState } from '../types';
import { useI18n } from 'vue-i18n';

interface PaginationType {
  total: number;
  current: number;
  pageSize: number;
}

interface Props {
  id: string;
}

/**
 * Composable for managing share list functionality
 * Handles loading, displaying, and managing shared file records
 *
 * @returns Object containing reactive state and methods for share list management
 */
export function useShareList () {
  const { t } = useI18n();

  // Component props reference
  let props: Props;

  // Pagination state for share list
  const pagination = reactive<PaginationType>({
    total: 0,
    current: 1,
    pageSize: 10
  });

  // Component state
  const state = reactive<ShareListState>({
    visible: false,
    list: []
  });

  // UI state
  const showMore = ref(false);
  const remark = ref<string | undefined>(undefined);
  const shareId = ref<string | undefined>(undefined);

  /**
   * Load share list data with optional pagination
   *
   * @param loadMore - Whether to load more items (pagination)
   */
  const loadList = async (loadMore = false): Promise<void> => {
    if (loadMore) {
      pagination.current += 1;
    }

    const params = {
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
      spaceId: props.id,
      filters: remark.value ? [{ value: remark.value, key: 'remark', op: 'MATCH' }] : undefined
    };

    const [error, res = { data: { list: [] } }] = await space.getSharedList(params);
    if (error) {
      return;
    }

    if (loadMore) {
      state.list.push(...res.data.list);
    } else {
      state.list = res.data.list || [];
    }

    showMore.value = state.list.length < +res.data.total;
  };

  /**
   * Delete a share record
   *
   * @param id - ID of the share record to delete
   */
  const deleteShare = async (id: string): Promise<void> => {
    const [error] = await space.deleteShare([id]);
    if (error) {
      return;
    }

    notification.success(t('actions.tips.deleteSuccess'));
    pagination.current = 1;
    await loadList();
  };

  /**
   * Copy share link and password to clipboard
   *
   * @param item - Share item to copy
   */
  const copyToClipboard = (item: ShareListItem): void => {
    let message;
    if (!item.public0) {
      message = t('fileSpace.share.messages.linkAndPassword', { url: item.url, password: item.password || '' });
    } else {
      message = t('fileSpace.share.messages.link', { url: item.url });
    }

    toClipboard(message).then(() => {
      notification.success(t('actions.tips.copySuccess'));
    });
  };

  /**
   * Open edit password mode for a share item
   *
   * @param item - Share item to edit password for
   */
  const openEditPassword = (item: ShareListItem): void => {
    item.editPassd = true;
    item.tempPass = item.password;
  };

  /**
   * Update password for a share item
   *
   * @param item - Share item to update password for
   */
  const updatePassword = async (item: ShareListItem): Promise<void> => {
    if (!item.tempPass) {
      notification.error(t('fileSpace.share.shareList.notifications.passwordEmpty'));
      return;
    }

    const params = {
      id: item.id,
      expiredFlag: item.expiredFlag,
      objectIds: item.objectIds,
      public0: item.public0,
      remark: item.remark,
      expiredDuration: item.expiredDuration ? { ...item.expiredDuration, unit: item.expiredDuration.unit.value } : undefined,
      password: item.tempPass
    };

    const [error] = await space.patchShare(params);
    if (error) {
      return;
    }

    item.password = item.tempPass as string;
    item.editPassd = false;
    notification.success(t('fileSpace.share.shareList.notifications.modifyPasswordSuccess'));
  };

  /**
   * Cancel password editing for a share item
   *
   * @param item - Share item to cancel password editing for
   */
  const cancelPasswordEdit = (item: ShareListItem): void => {
    item.editPassd = false;
  };

  /**
   * Open share dialog for editing or creating shares
   *
   * @param item - Optional share item to edit
   */
  const openShareDialog = (item?: ShareListItem): void => {
    shareId.value = item ? item.id : undefined;
    state.visible = true;
  };

  /**
   * Handle share operation completion
   */
  const handleShareEnd = (): void => {
    pagination.current = 1;
    loadList();
  };

  /**
   * Initialize composable
   *
   * @param componentProps - Component props
   */
  const init = (componentProps: Props): void => {
    props = componentProps;

    // Watch for remark changes
    watch(() => remark.value, () => {
      pagination.current = 1;
      loadList();
    });

    // Watch for ID changes
    watch(() => props.id, newValue => {
      if (newValue) {
        pagination.current = 1;
        loadList();
      }
    });

    // Load initial data
    onMounted(() => {
      pagination.current = 1;
      loadList();
    });
  };

  return {
    // State
    pagination,
    state,
    showMore,
    remark,
    shareId,

    // Methods
    loadList,
    deleteShare,
    copyToClipboard,
    openEditPassword,
    updatePassword,
    cancelPasswordEdit,
    openShareDialog,
    handleShareEnd,
    init
  };
}

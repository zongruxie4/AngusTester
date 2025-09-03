import { ref, watch } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { SpacePermission } from '@/enums/enums';
import type { IndexProps, IndexEmits, PermissionItem } from '../types';

/**
 * <p>
 * Main index component management composable for handling authentication modal
 * </p>
 * <p>
 * This composable manages the tab state, selected IDs, and permission loading
 * for the main authentication interface
 * </p>
 */
export function useIndex (
  props: IndexProps,
  emit: IndexEmits
) {
  // Reactive state
  const activeKey = ref<'user' | 'dept' | 'group'>('user');
  const checkedUserId = ref<string>();
  const checkedGroupId = ref<string>();
  const checkedDeptId = ref<string>();
  const permissions = ref<PermissionItem[]>([]);
  const loaded = ref(false);

  /**
   * <p>
   * Loads permission enums and converts them to the required format
   * </p>
   */
  const loadEnums = () => {
    const res = enumUtils.enumToMessages(SpacePermission);
    permissions.value = res.map(item => ({
      label: item.message,
      value: item.value
    }));
  };

  /**
   * <p>
   * Cancels the modal and emits visibility update
   * </p>
   */
  const cancel = () => {
    emit('update:visible', false);
  };

  /**
   * <p>
   * Resets all component state when visibility changes
   * </p>
   */
  const resetState = () => {
    activeKey.value = 'user';
    checkedUserId.value = undefined;
    checkedGroupId.value = undefined;
    checkedDeptId.value = undefined;
    loaded.value = false;
  };

  /**
   * <p>
   * Sets up watchers for component lifecycle
   * </p>
   */
  const setupWatchers = () => {
    watch(() => props.visible, (newValue) => {
      if (!newValue) {
        return;
      }

      resetState();
      loadEnums();
    }, { immediate: true });
  };

  return {
    // State
    activeKey,
    checkedUserId,
    checkedGroupId,
    checkedDeptId,
    permissions,
    loaded,

    // Methods
    loadEnums,
    cancel,
    resetState,
    setupWatchers
  };
}

import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { NoticeSetting, OrgItem } from '../../types';

/**
 * Composable for managing notification configuration
 */
export function useNotificationConfig () {
  const { t } = useI18n();

  // Organization list for notice recipients
  const notificationOrgs = ref<{ name: string; id: string }[]>([]);

  /**
   * Initialize organization list with user info
   * @param userInfo - User information
   */
  const initializeOrgs = (userInfo: { id: string; fullName: string }) => {
    notificationOrgs.value = [{ id: userInfo.id, name: userInfo.fullName }];
  };

  /**
   * Validate organization selection
   * @returns Promise<void>
   */
  const validateOrgs = (): Promise<void> => {
    if (!notificationOrgs.value.length) {
      return Promise.reject(t('scenarioMonitor.edit.selectOrgsRule'));
    }
    return Promise.resolve();
  };

  /**
   * Handle organization type change
   * @param noticeSetting - Notice setting object
   */
  const handleOrgTypeChange = (noticeSetting: NoticeSetting) => {
    noticeSetting.orgs = [];
    notificationOrgs.value = [];
  };

  /**
   * Handle organization selection change
   * @param _selectedValues - Selected organization values (unused)
   * @param valueObjects - Organization objects with full details
   */
  const handleOrgSelectionChange = (
    _selectedValues: string[],
    valueObjects: OrgItem[]
  ) => {
    notificationOrgs.value = (valueObjects || []).map(item => ({
      name: item.fullName || item.name,
      id: item.id
    }));
  };

  /**
   * Get notification setting with current organization selection
   * @param noticeSetting - Base notice setting
   * @returns NoticeSetting with current orgs
   */
  const getNotificationSettingWithOrgs = (noticeSetting: NoticeSetting): NoticeSetting => {
    return {
      ...noticeSetting,
      orgs: noticeSetting.enabled ? notificationOrgs.value : []
    };
  };

  return {
    notificationOrgs,
    initializeOrgs,
    validateOrgs,
    handleOrgTypeChange,
    handleOrgSelectionChange,
    getNotificationSettingWithOrgs
  };
}

import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils } from '@xcan-angus/infra';
import { ReportPermission } from '@/enums/enums';

/**
 * Composable for managing authorization permissions
 */
export function useAuthPermissions () {
  const { t } = useI18n();

  // Reactive references
  const permissions = ref<{value:string, label:string}[]>([]);
  const loaded = ref(false);

  /**
   * Load permission enums
   */
  const loadEnums = () => {
    const res = enumUtils.enumToMessages(ReportPermission);
    permissions.value = res.map(item => ({ label: item.message, value: item.value }));
  };

  /**
   * Initialize permissions
   */
  const init = (visible: boolean) => {
    watch(() => visible, (newValue) => {
      if (!newValue) {
        return;
      }

      loaded.value = false;
      loadEnums();
    }, { immediate: true });
  };

  return {
    // Reactive data
    permissions,
    loaded,

    // Methods
    init
  };
}

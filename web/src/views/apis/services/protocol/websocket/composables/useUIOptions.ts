import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * UI options composable for WebSocket view
 * <p>
 * Provides computed menu items for the right-side drawer.
 * </p>
 */
export function useUIOptions (props: any) {
  const { t } = useI18n();

  /**
   * Navigation menu items for the drawer
   * <p>
   * Generated based on current unarchived state and i18n labels.
   * </p>
   */
  const navigationMenuItems = computed(() => [
    props.valueObj.unarchived && {
      icon: 'icon-baocundaoweiguidang',
      name: t('service.apiWebSocket.navigation.saveToUnarchived'),
      key: 'saveUnarchived'
    },
    {
      icon: 'icon-baocun',
      name: props.valueObj?.unarchived ? t('actions.archiveToService') : t('actions.save'),
      key: 'save'
    },
    !props.valueObj.unarchived && {
      icon: 'icon-bianliang',
      name: t('common.variables'),
      key: 'variable'
    },
    {
      icon: 'icon-jiekoudaili',
      name: t('service.apiWebSocket.navigation.agent'),
      key: 'agent'
    }
  ].filter(Boolean));

  return { navigationMenuItems };
}

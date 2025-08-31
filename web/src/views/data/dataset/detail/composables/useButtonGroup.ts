import { useI18n } from 'vue-i18n';

/**
 * Composable for managing button group logic in dataset detail components
 * Provides button configurations and action handlers for dataset operations
 */
export function useButtonGroup () {
  const { t } = useI18n();

  /**
   * Configuration for primary action buttons
   * These buttons are always visible regardless of edit state
   */
  const primaryButtons = [
    {
      key: 'ok',
      icon: 'icon-dangqianxuanzhong',
      text: t('dataset.detail.buttonGroup.save'),
      type: 'primary' as const,
      size: 'small' as const
    }
  ];

  /**
   * Configuration for secondary action buttons
   * These buttons are only visible when in edit mode
   */
  const secondaryButtons = [
    {
      key: 'delete',
      icon: 'icon-qingchu',
      text: t('dataset.detail.buttonGroup.delete'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'export',
      icon: 'icon-fuzhizujian2',
      text: t('dataset.detail.buttonGroup.export'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'clone',
      icon: 'icon-fuzhizujian2',
      text: t('dataset.detail.buttonGroup.clone'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'copyLink',
      icon: 'icon-fuzhi',
      text: t('dataset.detail.buttonGroup.copyLink'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'refresh',
      icon: 'icon-shuaxin',
      text: t('dataset.detail.buttonGroup.refresh'),
      type: 'default' as const,
      size: 'small' as const
    }
  ];

  return {
    primaryButtons,
    secondaryButtons
  };
}

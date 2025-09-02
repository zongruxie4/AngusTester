import { useI18n } from 'vue-i18n';
import { ButtonGroupAction } from '../types';

/**
 * Composable for managing button group logic in variable detail components
 * Provides button configurations and action handlers for variable operations
 */
export function useButtonGroup () {
  const { t } = useI18n();

  /**
   * Configuration for primary action buttons
   * These buttons are always visible regardless of edit state
   */
  const primaryButtons = [
    {
      key: 'ok' as ButtonGroupAction,
      icon: 'icon-dangqianxuanzhong',
      text: t('dataVariable.detail.buttonGroup.save'),
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
      key: 'delete' as ButtonGroupAction,
      icon: 'icon-qingchu',
      text: t('dataVariable.detail.buttonGroup.delete'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'export' as ButtonGroupAction,
      icon: 'icon-fuzhizujian2',
      text: t('dataVariable.detail.buttonGroup.export'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'clone' as ButtonGroupAction,
      icon: 'icon-fuzhizujian2',
      text: t('dataVariable.detail.buttonGroup.clone'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'copyLink' as ButtonGroupAction,
      icon: 'icon-fuzhi',
      text: t('dataVariable.detail.buttonGroup.copyLink'),
      type: 'default' as const,
      size: 'small' as const
    },
    {
      key: 'refresh' as ButtonGroupAction,
      icon: 'icon-shuaxin',
      text: t('dataVariable.detail.buttonGroup.refresh'),
      type: 'default' as const,
      size: 'small' as const
    }
  ];

  return {
    primaryButtons,
    secondaryButtons
  };
}

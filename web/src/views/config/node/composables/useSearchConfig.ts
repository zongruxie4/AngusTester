import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

import { SearchOption, RoleOption } from '../types';
import { enumUtils, NodeRole } from '@xcan-angus/infra';
import { NodeSource } from '@/enums/enums';

/**
 * Composable for managing search configuration
 *
 * @returns Object containing search configuration functions and state
 */
export function useSearchConfig () {
  const { t } = useI18n();

  // Role options for node assignment
  const roleOptions = ref<RoleOption[]>([]);

  // Disabled roles that cannot be assigned to nodes
  const disabledRoles = [NodeRole.MANAGEMENT, NodeRole.CONTROLLER];

  /**
   * Search options configuration for the search panel
   */
  const searchOptions: SearchOption[] = [
    {
      type: 'input',
      allowClear: true,
      valueKey: 'name',
      label: t('node.searchOptions.node.label'),
      placeholder: t('node.searchOptions.node.placeholder'),
      maxlength: 100
    },
    {
      type: 'input',
      allowClear: true,
      valueKey: 'ip',
      label: t('node.searchOptions.ip.label'),
      placeholder: t('node.searchOptions.ip.placeholder'),
      op: 'EQUAL',
      maxlength: 100
    },
    {
      type: 'select-enum',
      allowClear: true,
      valueKey: 'role',
      enumKey: NodeRole,
      label: t('node.searchOptions.role.label'),
      op: 'IN',
      placeholder: t('node.searchOptions.role.placeholder')
    },
    {
      type: 'select-user',
      allowClear: true,
      valueKey: 'createdBy',
      label: t('node.searchOptions.creator.label'),
      placeholder: t('node.searchOptions.creator.placeholder'),
      maxlength: 100
    },
    {
      type: 'select-enum',
      allowClear: true,
      valueKey: 'source',
      enumKey: NodeSource,
      label: t('node.searchOptions.source.label'),
      placeholder: t('node.searchOptions.source.placeholder')
    }
  ];

  /**
   * Loads role enumeration data and transforms it for display
   *
   * <p>Converts enum values to role options with proper labels and disabled states.
   * Management and controller roles are disabled by default.</p>
   */
  const loadRoleEnums = () => {
    try {
      const data = enumUtils.enumToMessages(NodeRole);
      roleOptions.value = (data || []).map(item => ({
        ...item,
        name: item.message,
        label: item.message,
        disabled: disabledRoles.includes(item.value as NodeRole)
      }));
    } catch (error) {
      console.error('Failed to load role enums:', error);
      roleOptions.value = [];
    }
  };

  /**
   * Initializes search configuration on component mount
   */
  const initializeSearchConfig = () => {
    loadRoleEnums();
  };

  // Initialize search configuration when composable is created
  onMounted(() => {
    initializeSearchConfig();
  });

  return {
    // State
    roleOptions,
    disabledRoles,

    // Configuration
    searchOptions,

    // Methods
    loadRoleEnums,
    initializeSearchConfig
  };
}

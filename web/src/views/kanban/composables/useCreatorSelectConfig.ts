import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { GM } from '@xcan-angus/infra';
import type { CreatorObjectType, SelectFieldNames } from '../types';

/**
 * <p>Provides computed configuration for the Select component based on the current type.</p>
 * <p>Centralizes endpoint, placeholder, and field mapping logic.</p>
 */
export function useCreatorSelectConfig (currentType: { value: CreatorObjectType }) {
  const { t } = useI18n();

  const action = computed<string>(() => {
    if (currentType.value === 'USER') return `${GM}/user?fullTextSearch=true`;
    if (currentType.value === 'DEPT') return `${GM}/dept?fullTextSearch=true`;
    return `${GM}/group?fullTextSearch=true`;
  });

  const placeholder = computed<string>(() => {
    if (currentType.value === 'USER') return t('kanban.selectUser');
    if (currentType.value === 'DEPT') return t('kanban.selectDepartment');
    return t('kanban.selectGroup');
  });

  const fieldNames = computed<SelectFieldNames>(() => {
    if (currentType.value === 'USER') return { label: 'fullName', value: 'id' };
    return { label: 'name', value: 'id' };
  });

  return { action, placeholder, fieldNames };
}

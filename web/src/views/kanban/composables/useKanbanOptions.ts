import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * Provide UI options (e.g., view type options) that depend on i18n.
 */
export function useKanbanOptions () {
  const { t } = useI18n();
  const viewTypeOptions = computed(() => [
    { value: 'evaluation', label: t('kanban.viewType.evaluation') },
    { value: 'cto', label: t('kanban.viewType.cto') },
    { value: 'effectiveness', label: t('kanban.viewType.effectiveness') },
    { value: 'data', label: t('kanban.viewType.dataAssets') }
  ]);

  return { viewTypeOptions } as const;
}

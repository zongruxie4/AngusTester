import { onMounted, ref } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ReportCategory } from '@/enums/enums';
import type { CategorySelectEmits, CategorySelectProps, TreeDataNode, UseCategorySelectReturn } from '../types';

/**
 * Composable for managing category selection functionality
 * Handles category tree data loading and selection events
 */
export function useCategorySelect (
  props: CategorySelectProps,
  emit: CategorySelectEmits
): UseCategorySelectReturn {
  const { t } = useI18n();

  // Reactive state
  const moduleTreeData = ref<TreeDataNode[]>([
    { name: t('reportHome.chart.categories.allReports'), value: '', key: '' }
  ]);

  // Category icon mapping
  const categoryIcon: Record<ReportCategory, string> = {
    PROJECT: 'icon-xiangmu',
    TASK: 'icon-renwuceshibaogao',
    FUNCTIONAL: 'icon-gongnengceshibaogao',
    APIS: 'icon-jiekou',
    SCENARIO: 'icon-changjingguanli',
    EXECUTION: 'icon-zhihang',
    '': 'icon-liebiaoshitu'
  };

  /**
   * Load category options from enum
   */
  const loadOpt = () => {
    const data = enumUtils.enumToMessages(ReportCategory);
    const categoryData = (data || []).map(item => ({
      ...item,
      name: item.message,
      key: item.value || ''
    }));
    moduleTreeData.value.push(...categoryData);
  };

  /**
   * Handle tree selection change
   */
  const handleSelectKeysChange = (value: string[]) => {
    emit('update:category', value[0]);
  };

  // Lifecycle hooks
  onMounted(() => {
    loadOpt();
  });

  return {
    moduleTreeData,
    categoryIcon,
    handleSelectKeysChange,
    loadOpt
  };
}

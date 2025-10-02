import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { EfficiencyIndicatorLevel } from '../types';

/**
 * Composable for managing efficiency indicator data
 * @returns Object containing reactive data for efficiency indicators
 */
export function useEfficiencyData () {
  const { t } = useI18n();

  // Edit mode state
  const editable = ref(false);

  // Columns configuration for efficiency indicator table
  const columns = [
    {
      dataIndex: 'level',
      title: t('indicator.efficiency.table.columns.level')
    },
    {
      dataIndex: 'workload',
      title: t('common.workload')
    },
    {
      dataIndex: 'completedRate',
      title: t('common.counts.completedRate')
    },
    {
      dataIndex: 'overdueRate',
      title: t('common.counts.overdueRate')
    },
    {
      dataIndex: 'oneTimePassedRate',
      title: t('common.counts.oneTimePassedRate')
    },
    {
      dataIndex: 'savingWorkloadRate',
      title: t('common.counts.savingWorkloadRate')
    }
  ];

  // Static data for efficiency indicator levels
  const data = ref<EfficiencyIndicatorLevel[]>([
    {
      level: t('indicator.efficiency.table.data.levels.veryPoor'),
      workload: '100',
      completedRate: '35%',
      overdueRate: '35%',
      oneTimePassedRate: '30%',
      savingWorkloadRate: '0%'
    },
    {
      level: t('indicator.efficiency.table.data.levels.poor'),
      workload: '100',
      completedRate: '50%',
      overdueRate: '25%',
      oneTimePassedRate: '45%',
      savingWorkloadRate: '0%'
    },
    {
      level: t('indicator.efficiency.table.data.levels.medium'),
      workload: '100',
      completedRate: '65%',
      overdueRate: '15%',
      oneTimePassedRate: '60%',
      savingWorkloadRate: '10%'
    },
    {
      level: t('indicator.efficiency.table.data.levels.good'),
      workload: '100',
      completedRate: '90%',
      overdueRate: '5%',
      oneTimePassedRate: '85%',
      savingWorkloadRate: '20%'
    },
    {
      level: t('indicator.efficiency.table.data.levels.excellent'),
      workload: '100',
      completedRate: '100%',
      overdueRate: '2%',
      oneTimePassedRate: '90%',
      savingWorkloadRate: '30%'
    }
  ]);

  /**
   * Toggle edit mode for efficiency indicators
   */
  const toggleEditMode = () => {
    editable.value = !editable.value;
  };

  return {
    // Reactive data
    editable,
    columns,
    data,

    // Methods
    toggleEditMode
  };
}

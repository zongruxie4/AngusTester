import { useI18n } from 'vue-i18n';
import dayjs, { Dayjs } from 'dayjs';
import type { MenuItem, UseSearchPanelFiltersReturn } from '../types';
import { ScenarioMonitorStatus } from '@/enums/enums';

export function useSearchPanelFilters (): UseSearchPanelFiltersReturn {
  const { t } = useI18n();

  // Status options for filtering
  const statusOpt: MenuItem[] = [
    { name: t('scenarioMonitor.searchPanel.statusOptions.pending'), key: ScenarioMonitorStatus.PENDING },
    { name: t('scenarioMonitor.searchPanel.statusOptions.success'), key: ScenarioMonitorStatus.SUCCESS },
    { name: t('scenarioMonitor.searchPanel.statusOptions.failure'), key: ScenarioMonitorStatus.FAILURE }
  ];

  const statusKeys = statusOpt.map(i => i.key);

  // Association keys for filtering
  const assocKeys = ['createdDate', 'createdBy'];
  const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

  /**
   * Format date string based on time key
   * @param key - Time key (lastDay, lastThreeDays, lastWeek)
   * @returns Array of [startDate, endDate] formatted strings
   */
  const formatDateString = (key: string): [string, string] => {
    let startDate: Dayjs | undefined;
    let endDate: Dayjs | undefined;

    if (key === 'lastDay') {
      startDate = dayjs().startOf('date');
      endDate = dayjs();
    }

    if (key === 'lastThreeDays') {
      startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
      endDate = dayjs();
    }

    if (key === 'lastWeek') {
      startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
      endDate = dayjs();
    }

    return [
      startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '',
      endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''
    ];
  };

  return {
    statusOpt,
    statusKeys,
    assocKeys,
    timeKeys,
    formatDateString
  };
}

import { ref } from 'vue';
import { PageQuery, SearchCriteria, appContext } from '@xcan-angus/infra';
import { ScenarioMonitorStatus } from '@/enums/enums';
import dayjs from 'dayjs';

import type {
  MenuItem,
  OrderByKey
} from '../types';

type FilterItem = SearchCriteria;
type SearchPanelParams = PageQuery;

export function useSearchPanelAction (
  searchPanelRef: any,
  selectedMenuMap: any,
  searchFilters: any,
  assocFilters: any,
  quickSearchFilters: any,
  orderBy: any,
  orderSort: any,
  getParams: () => SearchPanelParams,
  emit: (value: SearchPanelParams) => void,
  emitRefresh: () => void
) {
  const userInfo = ref(appContext.getUser());

  // Filter configuration
  const assocKeys = ['createdDate', 'createdBy'];
  const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

  /**
   * Format date string based on time key
   * @param key - Time key (lastDay, lastThreeDays, lastWeek)
   * @returns Array of [startDate, endDate] formatted strings
   */
  const formatDateString = (key: string): [string, string] => {
    let startDate: any;
    let endDate: any;

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

  /**
   * Handle search panel change event
   * @param data - Filter items from search panel
   */
  const searchChange = (data: FilterItem[]): void => {
    searchFilters.value = data.filter(item => item.key && !assocKeys.includes(item.key));
    assocFilters.value = data.filter(item => item.key && assocKeys.includes(item.key));

    if (!assocFilters.value.length) {
      assocKeys.forEach(i => {
        if (i === 'createdDate') {
          timeKeys.forEach(t => delete selectedMenuMap.value[t]);
        } else {
          delete selectedMenuMap.value[i];
        }
      });
    } else {
      assocKeys.forEach(key => {
        if (['createdBy'].includes(key)) {
          const filterItem = assocFilters.value.find(i => i.key === key);
          if (!filterItem || filterItem.value !== String(userInfo.value?.id)) {
            delete selectedMenuMap.value[key];
          }
        } else if (key === 'createdDate') {
          const filterItem = assocFilters.value.filter(i => i.key === key);
          const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
          if (timeKey) {
            const timeValue = formatDateString(timeKey);
            if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
              delete selectedMenuMap.value[timeKey];
            }
          }
        }
      });
    }

    emit(getParams());
  };

  /**
   * Handle sort change event
   * @param sortData - Sort data with orderBy and orderSort
   */
  const toSort = (sortData: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }): void => {
    orderBy.value = sortData.orderBy;
    orderSort.value = sortData.orderSort;
    emit(getParams());
  };

  /**
   * Handle menu item click for quick search
   * @param data - Menu item data
   */
  const menuItemClick = (data: MenuItem): void => {
    const key = data.key;
    let searchChangeFlag = false;

    if (selectedMenuMap.value[key]) {
      delete selectedMenuMap.value[key];
      if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
        searchPanelRef.value.setConfigs([
          { valueKey: 'createdDate', value: undefined }
        ]);
        searchChangeFlag = true;
      } else if (assocKeys.includes(key)) {
        searchPanelRef.value.setConfigs([
          { valueKey: key, value: undefined }
        ]);
        searchChangeFlag = true;
      }
    } else {
      if (key === '') {
        selectedMenuMap.value = { '': true };
        quickSearchFilters.value = [];
        // Clear search panel
        if (typeof searchPanelRef.value?.clear === 'function') {
          searchPanelRef.value.clear();
          searchChangeFlag = true;
        }
      } else {
        delete selectedMenuMap.value[''];
      }

      if (timeKeys.includes(key)) {
        timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
        selectedMenuMap.value[key] = true;
      } else {
        selectedMenuMap.value[key] = true;
      }

      if (Object.values(ScenarioMonitorStatus).includes(key as ScenarioMonitorStatus)) {
        Object.values(ScenarioMonitorStatus).forEach(timeKey => delete selectedMenuMap.value[timeKey]);
        selectedMenuMap.value[key] = true;
      } else {
        selectedMenuMap.value[key] = true;
      }
    }

    const userId = userInfo.value?.id;
    const assocFiltersInQuick: {valueKey: string, value: string|string[]}[] = [];

    quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
      if (key === '') {
        return undefined;
      } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
        assocFiltersInQuick.push({
          valueKey: 'createdDate',
          value: formatDateString(key)
        });
        return undefined;
      } else if (assocKeys.includes(key)) {
        if (['createdBy'].includes(key)) {
          assocFiltersInQuick.push({ valueKey: key, value: String(userId) });
        }
        return undefined;
      } else if (Object.values(ScenarioMonitorStatus).includes(key as ScenarioMonitorStatus)) {
        return {
          key: 'status',
          op: SearchCriteria.OpEnum.Equal,
          value: key
        };
      } else {
        return {
          key,
          op: SearchCriteria.OpEnum.Equal,
          value: String(userId)
        };
      }
    }).filter(Boolean) as FilterItem[];

    if (assocFiltersInQuick.length) {
      searchPanelRef.value.setConfigs([
        ...assocFiltersInQuick
      ]);
      searchChangeFlag = true;
    }

    if (!searchChangeFlag) {
      emit(getParams());
    }
  };

  /**
   * Refresh search panel
   */
  const refresh = (): void => {
    emitRefresh();
  };

  return {
    searchChange,
    toSort,
    menuItemClick,
    refresh
  };
}

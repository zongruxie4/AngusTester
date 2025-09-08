import { computed, ref } from 'vue';
import { GroupedKey, ScenarioInfo } from '../types';

/**
 * Composable for managing scenario grouping functionality
 */
export function useScenarioGrouping () {
  const openKeys = ref<string[]>([]);

  /**
   * Handle collapse panel changes
   */
  const handleCollapseChange = (value: string[]) => {
    openKeys.value = value;
  };

  /**
   * Handle arrow toggle for collapse panels
   */
  const handleArrowChange = (open: boolean, key: string) => {
    if (open) {
      openKeys.value.push(key);
      return;
    }

    openKeys.value = openKeys.value.filter(item => item !== key);
  };

  /**
   * Group scenarios by the specified key
   */
  const groupScenarios = (dataSource: ScenarioInfo[], groupedKey: GroupedKey) => {
    if (!dataSource?.length) {
      return [];
    }

    const temp = dataSource.reduce((prev, cur) => {
      let _key = cur[groupedKey];
      if (Object.prototype.toString.call(_key) === '[object Object]') {
        _key = _key.value;
      }

      if (prev[_key]) {
        prev[_key].push(cur);
      } else {
        prev[_key] = [cur];
      }

      return prev;
    }, {} as { [key: string]: ScenarioInfo[] });

    return Object.entries(temp);
  };

  /**
   * Computed grouped list of scenarios
   */
  const getGroupedList = (dataSource: ScenarioInfo[], groupedKey: GroupedKey) => {
    return computed(() => groupScenarios(dataSource, groupedKey));
  };

  return {
    // State
    openKeys,

    // Methods
    handleCollapseChange,
    handleArrowChange,
    groupScenarios,
    getGroupedList
  };
}

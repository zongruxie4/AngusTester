import { computed, ref } from 'vue';
import { kanban } from '@/api/tester';
import {
  AssigneeInfo,
  BurnDownDataByType,
  EffectivenessProps,
  OverviewData,
  RankingData,
  TotalTypeCount
} from '../types';

/**
 * <p>
 * Effectiveness data management composable
 * </p>
 *
 * @param props - Component props containing filter parameters
 * @returns Object containing reactive data and methods
 */
export function useEffectivenessData (props: EffectivenessProps) {
  /** Overview data for display */
  const overviewData = ref<OverviewData>({} as OverviewData);

  /** Burn down chart data */
  const burnDownData = ref<BurnDownDataByType | null>(null);

  /** Total type count data */
  const totalTypeData = ref<TotalTypeCount>({} as TotalTypeCount);

  /** Assignee ranking data */
  const assigneeRanking = ref<RankingData | null>(null);

  /** Tester ranking data */
  const testerRanking = ref<RankingData | null>(null);

  /** Assignee information */
  const assignees = ref<Record<string, AssigneeInfo>>({});

  /** Tester information */
  const testers = ref<Record<string, AssigneeInfo>>({});

  /**
   * <p>
   * Computed parameters for API calls
   * </p>
   * <p>
   * Adjusts planId based on countType - uses sprintId for tasks and planId for use cases
   * </p>
   */
  const apiParams = computed(() => {
    const { countType, planId, sprintId, onShow, ...other } = props;
    if (countType === 'task') {
      return {
        ...other,
        planId: sprintId
      };
    }
    return {
      ...other,
      planId
    };
  });

  /**
   * <p>
   * Loads effectiveness dashboard data from API
   * </p>
   * <p>
   * Fetches data based on countType and updates all reactive data properties
   * </p>
   */
  const loadEffectivenessData = async () => {
    const [error, { data = {} }] = await (
      props.countType === 'task'
        ? kanban.getTaskOverView({ ...apiParams.value })
        : kanban.getTestingOverView({ ...apiParams.value })
    );

    if (error) {
      return;
    }

    // Update overview data
    const totalOverview = data.totalOverview || {};
    if (totalOverview) {
      overviewData.value = totalOverview;
    }

    // Update burn down chart data
    const burnDownCharts = data.burnDownCharts;
    if (burnDownCharts) {
      burnDownData.value = burnDownCharts;
    }

    // Update total type count
    const totalTypeCount = data.totalTypeCount;
    if (totalTypeCount) {
      totalTypeData.value = totalTypeCount;
    }

    // Update ranking data based on count type
    if (props.countType === 'task') {
      testerRanking.value = null;
      testers.value = {};

      const assigneeRankingData = data.assigneeRanking;
      if (assigneeRankingData) {
        assigneeRanking.value = assigneeRankingData;
      }

      const assigneesData = data.assignees;
      if (assigneesData) {
        assignees.value = assigneesData;
      }
    } else {
      assigneeRanking.value = null;
      assignees.value = {};

      const testerRankingData = data.testerRanking;
      if (testerRankingData) {
        testerRanking.value = testerRankingData;
      }

      const testersData = data.testers;
      if (testersData) {
        testers.value = testersData;
      }
    }
  };

  /**
   * <p>
   * Resets all data to initial state
   * </p>
   */
  const resetData = () => {
    overviewData.value = {} as OverviewData;
    burnDownData.value = null;
    totalTypeData.value = {} as TotalTypeCount;
    assigneeRanking.value = null;
    testerRanking.value = null;
    assignees.value = {};
    testers.value = {};
  };

  return {
    // Reactive data
    overviewData,
    burnDownData,
    totalTypeData,
    assigneeRanking,
    testerRanking,
    assignees,
    testers,

    // Computed properties
    apiParams,

    // Methods
    loadEffectivenessData,
    resetData
  };
}

import { ref, computed, reactive } from 'vue';
import { kanban } from '@/api/tester';
import { 
  DataAssetsProps, 
  RankingItem, 
  GrowthTrendData, 
  CaseData, 
  ApiData, 
  TaskData, 
  ScenarioData, 
  ScriptData, 
  MockData, 
  DataAssetsData 
} from '../types';

/**
 * <p>
 * Data assets data management composable
 * </p>
 * <p>
 * Manages all data fetching and state for the data assets dashboard
 * </p>
 * 
 * @param props - Component props containing filter parameters
 * @returns Object containing reactive data and data loading methods
 */
export function useDataAssetsData(props: DataAssetsProps) {
  /** Ranking data for contribution ranking */
  const rankingData = ref<RankingItem[]>([]);
  
  /** Growth trend data for charts */
  const growthTrendData = ref<GrowthTrendData>({});
  
  /** Case-related data */
  const caseData = ref<CaseData>({});
  
  /** API-related data */
  const apiData = ref<ApiData>({});
  
  /** Task and sprint data */
  const taskData = ref<TaskData>({});
  
  /** Scenario data */
  const scenarioData = ref<ScenarioData>({});
  
  /** Script data */
  const scriptData = ref<ScriptData>({});
  
  /** Mock data */
  const mockData = reactive<MockData>({
    allApi: 0,
    allService: 0,
    allResponse: 0,
    allPushback: 0
  });
  
  /** Data assets data */
  const dataAssetsData = reactive<DataAssetsData>({
    allDataset: 0,
    allDatasource: 0,
    allFile: 0,
    allVariable: 0
  });

  /** Computed API parameters */
  const apiParams = computed(() => {
    const { onShow, ...other } = props;
    return { ...other };
  });

  /**
   * <p>
   * Loads ranking data from API
   * </p>
   * <p>
   * Fetches user contribution ranking data and merges with user information
   * </p>
   */
  const loadRankData = async () => {
    const [error, { data = {} }] = await kanban.getRanking({ ...apiParams.value });
    if (error) {
      return;
    }

    rankingData.value = data.rankings.TOTAL.map((item: any) => {
      return {
        ...item,
        ...(data?.userInfos?.[item.userId] || {})
      };
    });
  };

  /**
   * <p>
   * Loads growth trend data from API
   * </p>
   * <p>
   * Fetches growth trend data for the specified target type
   * </p>
   * 
   * @param targetType - Target type for growth trend (e.g., TASK, FUNC)
   */
  const loadGrowthTrendData = async (targetType: string) => {
    const [error, { data = {} }] = await kanban.getGrowthTrend({ 
      ...apiParams.value, 
      category: targetType 
    });
    
    if (error) {
      return;
    }

    growthTrendData.value = data;
  };

  /**
   * <p>
   * Loads case-related data from API
   * </p>
   * <p>
   * Fetches case statistics, test results, review status, and plan data
   * </p>
   */
  const loadCaseData = async () => {
    const [error, { data = {} }] = await kanban.getTesting({ ...apiParams.value });
    if (error) {
      return;
    }

    caseData.value = data;
  };

  /**
   * <p>
   * Loads API-related data from API
   * </p>
   * <p>
   * Fetches API statistics by HTTP method and status
   * </p>
   */
  const loadApiData = async () => {
    const [error, { data = {} }] = await kanban.getApis({ ...apiParams.value });
    if (error) {
      return;
    }

    apiData.value = data;
  };

  /**
   * <p>
   * Loads task and sprint data from API
   * </p>
   * <p>
   * Fetches task statistics by type and status, sprint statistics by status
   * </p>
   */
  const loadTaskData = async () => {
    const [error, { data = {} }] = await kanban.getTask({ ...apiParams.value });
    if (error) {
      return;
    }

    taskData.value = data;
  };

  /**
   * <p>
   * Loads scenario data from API
   * </p>
   * <p>
   * Fetches scenario statistics by script type
   * </p>
   */
  const loadScenarioData = async () => {
    const [error, { data = {} }] = await kanban.getScenario({ ...apiParams.value });
    if (error) {
      return;
    }

    scenarioData.value = data;
  };

  /**
   * <p>
   * Loads script data from API
   * </p>
   * <p>
   * Fetches script statistics by plugin name
   * </p>
   */
  const loadScriptData = async () => {
    const [error, { data = {} }] = await kanban.getScript({ ...apiParams.value });
    if (error) {
      return;
    }

    scriptData.value = data;
  };

  /**
   * <p>
   * Loads mock data from API
   * </p>
   * <p>
   * Fetches mock API, service, response, and pushback counts
   * </p>
   */
  const loadMockData = async () => {
    const [error, { data = {} }] = await kanban.getMock({ ...apiParams.value });
    if (error) {
      return;
    }

    const { allApi, allService, allResponse, allPushback } = data;
    mockData.allApi = allApi || 0;
    mockData.allService = allService || 0;
    mockData.allResponse = allResponse || 0;
    mockData.allPushback = allPushback || 0;
  };

  /**
   * <p>
   * Loads data assets data from API
   * </p>
   * <p>
   * Fetches dataset, datasource, file, and variable counts
   * </p>
   */
  const loadDataAssetsData = async () => {
    const [error, { data = {} }] = await kanban.getData({ ...apiParams.value });
    if (error) {
      return;
    }

    const { allDataset, allDatasource, allFile, allVariable } = data;
    dataAssetsData.allDataset = allDataset || 0;
    dataAssetsData.allDatasource = allDatasource || 0;
    dataAssetsData.allFile = allFile || 0;
    dataAssetsData.allVariable = allVariable || 0;
  };

  /**
   * <p>
   * Loads all data assets data
   * </p>
   * <p>
   * Calls all data loading methods to refresh the dashboard
   * </p>
   */
  const loadAllData = async () => {
    await Promise.all([
      loadGrowthTrendData('TASK'),
      loadRankData(),
      loadCaseData(),
      loadApiData(),
      loadTaskData(),
      loadScenarioData(),
      loadScriptData(),
      loadMockData(),
      loadDataAssetsData()
    ]);
  };

  /**
   * <p>
   * Refreshes all data
   * </p>
   * <p>
   * Only loads data if projectId is available
   * </p>
   */
  const refresh = async () => {
    if (!props.projectId) {
      return;
    }
    await loadAllData();
  };

  return {
    // Reactive data
    rankingData,
    growthTrendData,
    caseData,
    apiData,
    taskData,
    scenarioData,
    scriptData,
    mockData,
    dataAssetsData,
    
    // Computed properties
    apiParams,
    
    // Methods
    loadRankData,
    loadGrowthTrendData,
    loadCaseData,
    loadApiData,
    loadTaskData,
    loadScenarioData,
    loadScriptData,
    loadMockData,
    loadDataAssetsData,
    loadAllData,
    refresh
  };
}

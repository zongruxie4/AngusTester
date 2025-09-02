import { reactive, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import { analysis } from '@/api/tester';
import { UserCreationStatistics, ProjectCreationStatistics } from '../types';

/**
 * <p>Composable for managing homepage data state and operations</p>
 * <p>Handles all data fetching, state management, and data transformations</p>
 */
export function useStatisticsData (_projectId: string, userId: string) {
  // Loading state
  const loading = ref(false);
  const projectId = ref<string>(_projectId);

  // User-specific statistics state
  const userStatistics = reactive<UserCreationStatistics>({
    allService: '0',
    serviceByLastWeek: '0',
    serviceByLastMonth: '0',
    allApis: '0',
    apisByLastWeek: '0',
    apisByLastMonth: '0',
    allUnarchivedApis: '0',
    unarchivedApisByLastWeek: '0',
    unarchivedApisByLastMonth: '0'
  });

  // Project-wide statistics state
  const projectStatistics = reactive<ProjectCreationStatistics>({
    allVariable: 0,
    allDataset: 0,
    allDatasource: 0,
    allFile: 0,
    variableByUse: {
      IN_USE: 0,
      NOT_IN_USE: 0
    },
    fileByResourceType: {
      SPACE: 0,
      DIRECTORY: 0,
      FILE: 0
    },
    datasourceByDb: {
      H2: 0,
      HSQLDB: 0,
      SQLITE: 0,
      POSTGRES: 0,
      MARIADB: 0,
      MYSQL: 0,
      ORACLE: 0,
      SQLSERVER: 0
    },
    datasetByUse: {
      IN_USE: 0,
      NOT_IN_USE: 0
    }
  });

  /**
   * <p>Load user-specific statistics from API</p>
   * <p>Fetches statistics for services and APIs created by the current user</p>
   */
  const loadUserStatistics = async (): Promise<void> => {
    loading.value = true;

    try {
      const params = {
        creatorObjectType: 'USER',
        creatorObjectId: userId,
        projectId: projectId.value
      };

      const [error, res] = await analysis.getDataStatistics(params);
      if (error || utils._typeof(res?.data) !== 'object') {
        return;
      }

      // Update user statistics with API response
      Object.assign(userStatistics, res.data);
    } finally {
      loading.value = false;
    }
  };

  /**
   * <p>Load project-wide statistics from API</p>
   * <p>Fetches overall project statistics including variables, datasets, datasources, and files</p>
   */
  const loadProjectStatistics = async (): Promise<void> => {
    loading.value = true;

    try {
      const params = { projectId: projectId.value };
      const [error, res] = await analysis.getDataStatistics(params);

      if (error || utils._typeof(res?.data) !== 'object') {
        return;
      }

      const { data } = res;

      // Update project statistics with API response
      projectStatistics.allVariable = data.allVariable;
      projectStatistics.allDataset = data.allDataset;
      projectStatistics.allDatasource = data.allDatasource;
      projectStatistics.allFile = data.allFile;

      // Update variable usage statistics
      const { IN_USE = 0, NOT_IN_USE = 0 } = data.variableByUse;
      projectStatistics.variableByUse.IN_USE = IN_USE;
      projectStatistics.variableByUse.NOT_IN_USE = NOT_IN_USE;

      // Update file resource type statistics
      const { SPACE = 0, DIRECTORY = 0, FILE = 0 } = data.fileByResourceType;
      projectStatistics.fileByResourceType.SPACE = SPACE;
      projectStatistics.fileByResourceType.DIRECTORY = DIRECTORY;
      projectStatistics.fileByResourceType.FILE = FILE;

      // Update datasource database type statistics
      const {
        H2 = 0, HSQLDB = 0, SQLITE = 0, POSTGRES = 0,
        MARIADB = 0, MYSQL = 0, ORACLE = 0, SQLSERVER = 0
      } = data.datasourceByDb;

      projectStatistics.datasourceByDb.H2 = H2;
      projectStatistics.datasourceByDb.HSQLDB = HSQLDB;
      projectStatistics.datasourceByDb.SQLITE = SQLITE;
      projectStatistics.datasourceByDb.POSTGRES = POSTGRES;
      projectStatistics.datasourceByDb.MARIADB = MARIADB;
      projectStatistics.datasourceByDb.MYSQL = MYSQL;
      projectStatistics.datasourceByDb.ORACLE = ORACLE;
      projectStatistics.datasourceByDb.SQLSERVER = SQLSERVER;

      // Update dataset usage statistics
      if (data.datasetByUse) {
        const { IN_USE = 0, NOT_IN_USE = 0 } = data.datasetByUse;
        if (projectStatistics.datasetByUse) {
          projectStatistics.datasetByUse.IN_USE = IN_USE;
          projectStatistics.datasetByUse.NOT_IN_USE = NOT_IN_USE;
        }
      }
    } finally {
      loading.value = false;
    }
  };

  /**
   * <p>Load all statistics data</p>
   * <p>Fetches both user-specific and project-wide statistics</p>
   */
  const loadAllStatistics = async (): Promise<void> => {
    await Promise.all([
      loadUserStatistics(),
      loadProjectStatistics()
    ]);
  };

  // Watch for projectId changes and reload data
  watch(projectId, () => {
    loadAllStatistics();
  });

  return {
    // State
    projectId,
    loading,
    userStatistics,
    projectStatistics,

    // Methods
    loadUserStatistics,
    loadProjectStatistics,
    loadAllStatistics
  };
}

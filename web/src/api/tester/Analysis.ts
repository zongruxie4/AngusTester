import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/analysis';
  }

  addAnalysis <T> (params: T) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateAnalysis <T> (params: T) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteAnalysis (ids: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  refreshAnalysis (analysisId: string) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${analysisId}/refresh`);
  }

  getAnalysisDetail (analysisId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${analysisId}`);
  }

  getAnalysisList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getApisStatistics (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/resources`, params);
  }

  getScriptCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/script/count`, params);
  }

  getCustomizationSummary (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/customization/summary`, params);
  }

  getFuncCaseCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/case/count`, params);
  }

  getFuncPlanBurndown (planId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/plan/${planId}/burndown`);
  }

  getFuncTesterWorkSummary (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/work/summary`, params);
  }

  getFuncTesterBurndown (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/burndown`, params);
  }

  getFuncTesterProgress (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/progress`, params);
  }

  getFuncResourceCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/resources/count`, params);
  }

  getScenarioStatistics (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/association/creation`, params);
  }

  getScenarioResourceCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/resources/count`, params);
  }

  getTaskCountSummary (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/summary`, params);
  }

  getTaskCount (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/count`, params);
  }

  getAnalysisMockService (mockServiceId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/mock/service/count?mockServiceId=${mockServiceId}`);
  }

  getFuncWork (projectId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/project/${projectId}/work`);
  }

  getDataStatistics (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/data/resources`, params);
  }

  getAssigneeProgress (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/assignee/progress`, params);
  }

  getSprintBurndown (sprintId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/sprint/${sprintId}/burndown`);
  }

  getTaskWorkSummary <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/assignee/work/summary`, params);
  }

  getTaskResourceCount <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/resources/count`, params);
  }

  getTaskAssigneeBurndown <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/assignee/burndown`, params);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/analysis';
  }

  loadProStatistics (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/resources`, params);
  }

  loadScriptCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/script/count`, params);
  }

  loadCustomizationSummary (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/customization/summary`, params);
  }

  loadFuncCaseCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/case/count`, params);
  }

  loadFuncPlanBurndown (planId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/plan/${planId}/burndown`);
  }

  loadFuncTesterWorkSummary (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/work/summary`, params);
  }

  loadFuncTesterBurndown (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/burndown`, params);
  }

  loadFuncTesterProgress (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/tester/progress`, params);
  }

  loadFuncResourceCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/resources/count`, params);
  }

  loadScenarioStatistics (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/association/creation`, params);
  }

  loadScenarioResourceCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/resources/count`, params);
  }

  loadTaskCount (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/summary`, params);
  }

  getTaskCount (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/count`, params);
  }

  getAnalysisMockService (mockServiceId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/mock/service/count?mockServiceId=${mockServiceId}`);
  }

  loadFuncWork (projectId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/project/${projectId}/work`);
  }

  loadDataStatistics (params) : Promise<[Error | null, any]> {
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

  searchList <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  deleteAnalysis (ids: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  refreshContent (analysisId: string) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${analysisId}/refresh`);
  }

  getAnalysisInfo (analysisId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${analysisId}`);
  }

  addAnalysis <T> (params: T) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateAnalysis <T> (params: T) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }
}

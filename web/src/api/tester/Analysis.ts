import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/analysis';
  }

  // 查询项目下接口数据统计
  loadProStatistics (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/resources`, params);
  }

  // // 查询项目统计
  // loadStatistics (params): Promise<[error | null, any]> {
  //   return http.get(`${baseUrl}/apis/association/creation`, params);
  // }

  loadScriptCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/script/count`, params);
  }

  loadCustomizationSummary (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/customization/summary`, params);
  }

  loadFuncCaseCount (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/func/case/count`, params)
  }

  // 查询主页统计
  loadScenarioStatistics (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/association/creation`, params);
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

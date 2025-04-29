import { http } from '@xcan-angus/tools';

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
}

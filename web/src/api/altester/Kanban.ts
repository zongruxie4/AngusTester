import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/kanban';
  }

  // 增长趋势
  loadGrowthTrend (params: {category:string, projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/growthtrend`, params);
  }

  // 排名
  loadRankData (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/ranking`, params);
  }

  // 功能用例
  loadCase (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/func`, params);
  }

  // 接口
  loadApi (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/apis`, params);
  }

  // 数据
  loadData (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/data`, params);
  }

  // mock
  loadMock (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/mock`, params);
  }

  // mock
  loadScenario (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/scenarios`, params);
  }

  // 脚本
  loadScript (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/script`, params);
  }

  // 任务
  loadTask (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/task`, params);
  }

  // 报告
  loadReport (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/report`, params);
  }

  // 效能视图 =>任务总览
  loadTaskOverView (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; planId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/task/overview`, params);
  }

  // 效能视图 =>用例总览
  loadCaseOverView (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/case/overview`, params);
  }

  // cto 视图
  loadTaskCto (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/task/overview`, params);
  }

  // cto 视图
  loadCaseCto (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/case/overview`, params);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/kanban';
  }

  loadGrowthTrend (params: {category:string, projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/growthtrend`, params);
  }

  loadRankData (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/ranking`, params);
  }

  loadCase (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/func`, params);
  }

  loadApi (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/apis`, params);
  }

  loadData (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/data`, params);
  }

  loadMock (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/mock`, params);
  }

  loadScenario (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/scenarios`, params);
  }

  loadScript (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/script`, params);
  }

  loadTask (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/task`, params);
  }

  loadReport (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/report`, params);
  }

  loadTaskOverView (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; planId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/task/overview`, params);
  }

  loadCaseOverView (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/case/overview`, params);
  }

  loadTaskCto (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/task/overview`, params);
  }

  loadCaseCto (params: {projectId: string, creatorObjectType?: string; creatorObjectId?:string;createdDateStart?: string;createdDateEnd?: string; sprintId?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/case/overview`, params);
  }
}

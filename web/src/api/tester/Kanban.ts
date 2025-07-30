import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/kanban';
  }

  getGrowthTrend (params: {
    category: string,
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/growthtrend`, params);
  }

  getRanking (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/ranking`, params);
  }

  getTesting (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/func`, params);
  }

  getApis (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/apis`, params);
  }

  getData (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/data`, params);
  }

  getMock (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/mock`, params);
  }

  getScenario (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/scenarios`, params);
  }

  getScript (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/script`, params);
  }

  getTask (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/task`, params);
  }

  getReport (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dataassets/report`, params);
  }

  getTaskOverView (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    planId?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/task/overview`, params);
  }

  getTestingOverView (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    sprintId?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/efficiency/case/overview`, params);
  }

  getTaskCto (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    sprintId?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/task/overview`, params);
  }

  getTestingCto (params: {
    projectId: string,
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    sprintId?: string
  }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cto/case/overview`, params);
  }
}

import {http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func';
  }

  getPlanInfo (planId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}`);
  }

  getCurrentAuth (params: {ids: string[], adminFlag: boolean}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current`, params);
  }

  getCurrentAuthByPlanId (planId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}/user/auth/current`);
  }

  backAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  deleteAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backTrash (trashId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${trashId}/back`);
  }

  deleteTrash (trashId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${trashId}`);
  }

  searchTrash (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/search`, params);
  }
}

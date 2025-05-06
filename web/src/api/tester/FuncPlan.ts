import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/plan';
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
}

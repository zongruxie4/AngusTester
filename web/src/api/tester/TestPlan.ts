import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/plan';
  }

  addPlan (params) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  putPlan (params) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  getPlanList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getPlanDetail (planId: number): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}`);
  }

  startPlan (planId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${planId}/start`);
  }

  endPlan (planId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${planId}/end`);
  }

  blockPlan (planId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${planId}/block`);
  }

  deletePlan (planId: number) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${planId}`);
  }

  clonePlan (planId: number) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${planId}/clone`);
  }

  resetCaseResult (params) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case/result/reset`, params, { paramsType: true });
  }

  resetCaseReview (params) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case/review/reset`, params, { paramsType: true });
  }

  getCurrentAuth (params: {ids: number[], admin: boolean}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current`, params);
  }

  getCurrentAuthByPlanId (planId: number, params: any = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}/user/auth/current`, params);
  }

  getNotReviewedCase (planId: number, params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}/case/notReviewed`, params);
  }

  getCaseNotEstablishedBaseline (planId: number, params = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}/case/notEstablishedBaseline`, params);
  }
}

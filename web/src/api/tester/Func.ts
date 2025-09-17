import { http } from '@xcan-angus/infra';
import { slice } from 'lodash-es';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func';
  }

  addReview (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/review`, params);
  }

  putReview (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review`, { ...params, fullTextSearch: true });
  }

  cloneReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/clone`);
  }

  startReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/start`);
  }

  endReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/end`);
  }

  deleteReview (reviewId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/review/${reviewId}`);
  }

  getReviewDetail (reviewId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/${reviewId}`);
  }

  getReviewList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review`, { ...params, fullTextSearch: true });
  }

  getReviewAuthByPlanId (planId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/${planId}/user/auth/current`, params);
  }

  addReviewCase (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/review/case`, params);
  }

  deleteReviewCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/review/case`, caseIds, {
      dataType: true
    });
  }

  reviewCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review/case/review`, params);
  }

  getReviewCaseDetail (caseId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/case/${caseId}`);
  }

  getReviewCaseList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/case`, { ...params, fullTextSearch: true });
  }

  restartReviewCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/case/restart`, { ids: caseIds }, { paramsType: true });
  }

  resetReviewCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/case/reset`, { ids: caseIds }, { paramsType: true });
  }

  addBaseline (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline`, params);
  }

  updateBaseline (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/baseline`, params);
  }

  establishBaseline (baselineId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline/${baselineId}/establish`);
  }

  deleteBaseline (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline`, {
      ids
    });
  }

  getBaselineDetail (baselineId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}`);
  }

  getBaselineList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline`, { ...params, fullTextSearch: true });
  }

  addBaselineCase (baselineId: string, caseIds) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline/${baselineId}/case`, caseIds);
  }

  deleteBaselineCase (baselineId: string, caseIds: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline/${baselineId}/case`, caseIds, {
      dataType: true
    });
  }

  deleteBaselineCaseById (baselineCaseId: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline/case`, baselineCaseId, {
      dataType: true
    });
  }

  getBaselineCaseList (baselineId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}/case`, { ...params, fullTextSearch: true });
  }

  getBaselineCaseDetail (baselineId: string, caseId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}/case/${caseId}`, null, {
      silence: false
    });
  }

  backTrash (trashId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${trashId}/back`);
  }

  backAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  deleteTrash (trashId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${trashId}`);
  }

  deleteAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  getTrashList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }
}

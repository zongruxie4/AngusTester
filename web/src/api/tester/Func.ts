import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func';
  }

  getCurrentReviewAuthByPlanId (reviewId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/${reviewId}/user/auth/current`, params);
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
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }

  searchReview (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review`, { ...params, fullTextSearch: true });
  }

  putReview (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review`, { ...params, fullTextSearch: true });
  }

  addReview (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/review`, params);
  }

  deleteReview (reviewId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/review/${reviewId}`);
  }

  getReview (reviewId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/${reviewId}`);
  }

  endReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/end`);
  }

  cloneReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/clone`);
  }

  searchReviewCase (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/case`, { ...params, fullTextSearch: true });
  }

  addReviewCase (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/review/case`, params);
  }

  deleteReviewCase (reviewIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/review/case`, reviewIds, {
      dataType: true
    });
  }

  reviewCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review/case/review`, params);
  }

  getReviewCase (reviewId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/review/case/${reviewId}`);
  }

  startReview (reviewId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/${reviewId}/start`);
  }

  restartReviewCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/case/restart`, { ids: caseIds }, { paramsType: true });
  }

  resetReviewCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/case/reset`, { ids: caseIds }, { paramsType: true });
  }

  searchBaseline (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline`, { ...params, fullTextSearch: true });
  }

  updateBaseline (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/baseline`, params);
  }

  addBaseline (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline`, params);
  }

  deleteBaseline (baselineId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline/${baselineId}`);
  }

  batchDelBaseline (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline`, {
      ids
    });
  }

  getBaselineInfo (baselineId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}`);
  }

  searchCaseInBaseline (baselineId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}/case`, params);
  }

  addBaselineCase (baselineId: string, caseIds) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline/${baselineId}/case`, caseIds);
  }

  establishBaseline (baselineId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/baseline/${baselineId}/establish`);
  }

  searchBaselineCase (baselineId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}/case`, { ...params, fullTextSearch: true });
  }

  deleteBaselineCase (baselineId: string, caseIds: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline/${baselineId}/case`, caseIds, {
      dataType: true
    });
  }

  deleteBaselineCaseByCaseIdInBaseline (baselineCaseId: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/baseline/case`, baselineCaseId, {
      dataType: true
    });
  }

  getCaseInfoInBaseline (baselineId: string, caseId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/baseline/${baselineId}/case/${caseId}`);
  }
}

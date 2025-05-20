import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/case';
  }

  loadFuncCase (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  loadFavourite (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/favourite/search`, params);
  }

  loadFollow (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/follow/search`, params);
  }

  updateCase (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteCase (caseIds: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, caseIds, { dataType: true });
  }

  putCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  addCase (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  cancelFavouriteCase (caseId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${caseId}/favourite`);
  }

  favouriteCase (caseId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${caseId}/favourite`);
  }

  followCase (caseId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${caseId}/follow`);
  }

  cancelFollowCase (caseId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${caseId}/follow`);
  }

  cloneCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, caseIds);
  }

  getCaseInfo (CaseId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${CaseId}`);
  }

  putResult (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/result`, params);
  }

  updateResult (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result`, params);
  }

  resetResult (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/reset`, caseIds, { dataType: true });
  }

  reviewCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review`, params);
  }

  resetReview (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/reset`, caseIds, { dataType: true });
  }

  getReviewRecord (CaseId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${CaseId}/review`);
  }

  retestResult (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/retest`, caseIds, { dataType: true });
  }

  putDeadline (caseId: string, deadline: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/deadline/${deadline}`);
  }

  moveCase (targetPlanId: string, caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/move?targetPlanId=${targetPlanId}`, caseIds, { paramsType: false });
  }

  putAttachment (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/attachment`, params);
  }

  putName (caseId: string, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/name?name=${name}`);
  }

  putPriority (caseId: string, value): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/priority/${value}`);
  }

  putEvalWorkload (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/evalWorkload`, params);
  }

  putActualWorkload (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/actualWorkload`, params);
  }

  putTag (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/tag`, params);
  }

  importCase (formData: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData);
  }

  putAssociationCase (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/case`, params);
  }

  cancelAssociationCase (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/case/cancel`, params);
  }

  putAssociationTask (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/task`, params);
  }

  cancelAssociationTask (caseId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/task/cancel`, params);
  }
}

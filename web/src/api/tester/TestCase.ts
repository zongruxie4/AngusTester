import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/case';
  }

  addCase (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  putCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  updateCase (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteCase (caseIds: number[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, caseIds, { dataType: true });
  }

  cloneCase (caseIds: number[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, caseIds);
  }

  getCaseDetail (CaseId: number): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${CaseId}`);
  }

  getCaseList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  putCaseResult (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/result`, params);
  }

  updateCaseResult (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result`, params);
  }

  resetCaseResult (caseIds: number[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/reset`, caseIds, { dataType: true });
  }

  reviewCase (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/review`, params);
  }

  resetReviewCase (caseIds: number[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/reset`, caseIds, { dataType: true });
  }

  getReviewRecord (CaseId: number): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${CaseId}/review`);
  }

  retestResult (caseIds: number[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/retest`, caseIds, { dataType: true });
  }

  putDeadline (caseId: number, deadline: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/deadline/${deadline}`);
  }

  moveCase (targetPlanId: number | undefined, caseIds: number[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/move?targetPlanId=${targetPlanId}`, caseIds, { paramsType: false });
  }

  putAttachment (caseId: number, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/attachment`, params);
  }

  putName (caseId: number, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/name?name=${name}`);
  }

  putPriority (caseId: number, value): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/priority/${value}`);
  }

  putEvalWorkload (caseId: number, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/evalWorkload`, params);
  }

  putActualWorkload (caseId: number, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/actualWorkload`, params);
  }

  putTag (caseId: number, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/tag`, params);
  }

  importCase (formData: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData);
  }

  putAssociationCase (caseId: number, assocCaseIds: number[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/case`, { assocCaseIds }, { paramsType: true });
  }

  cancelAssociationCase (caseId: number, assocCaseIds: number[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/case/cancel`, { assocCaseIds }, { paramsType: true });
  }

  putAssociationTask (caseId: number, assocTaskIds: number[]) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/task`, { assocTaskIds }, { paramsType: true });
  }

  cancelAssociationTask (caseId: number, assocTaskIds: number[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${caseId}/association/task/cancel`, { assocTaskIds }, { paramsType: true });
  }

  AddFavouriteCase (caseId: number): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${caseId}/favourite`);
  }

  addFollowCase (caseId: number): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${caseId}/follow`);
  }

  cancelFavouriteCase (caseId: number): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${caseId}/favourite`);
  }

  cancelFollowCase (caseId: number): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${caseId}/follow`);
  }

  getFavouriteList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/favourite`, { ...params, fullTextSearch: true });
  }

  getFollowList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/follow`, { ...params, fullTextSearch: true });
  }
}

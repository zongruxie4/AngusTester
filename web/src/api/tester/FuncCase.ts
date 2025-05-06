import {http, TESTER} from '@xcan-angus/tools';

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

  deleteCase (caseIds: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, caseIds, { dataType: true });
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

  updateResult (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result`, params);
  }

  resetResult (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/reset`, caseIds, { dataType: true });
  }

  resetReview (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/review/reset`, caseIds, { dataType: true });
  }

  retestResult (caseIds: string[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/result/retest`, caseIds, { dataType: true });
  }
}

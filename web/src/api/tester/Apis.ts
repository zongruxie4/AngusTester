import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis';
  }

  patchClone (params:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/clone`, { id: params });
  }

  patchMove (params: {apiIds: string[], targetProjectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/move`, params);
  }

  del (params: { ids: string[] }): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, params);
  }

  loadInfo (id:string, resolveRef = false, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, { resolveRef }, axiosConf);
  }

  putApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  addApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/archive`, params);
  }

  updateApi (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  addFavourite (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/favourite`, {});
  }

  cancelFavourite (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, {});
  }

  updatePerf (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/perf?id=${params.id}`, params);
  }

  delNotArchiveList (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/archive`);
  }

  searchList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  cancelPerf (id:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/perf/cancel`);
  }

  loadPerf (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/perf`);
  }

  addNewApi (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  loadApiAuthority (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  updateAuthFlag (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  addAuth (id: string, params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/auth`, params);
  }

  addWatch (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  cancelWatch (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, {});
  }

  updateAuth (authId: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  delAuth (authId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  retest (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  reOpen (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  loadActionAuth ({ id = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/${userId}/auth`);
  }

  loadUserAuth (id: string, admin = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin });
  }

  checkAuth ({ id = '', authPermission = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/${authPermission}/check`, {
      userId
    });
  }

  addCase (params: {[key: string]: any}): Promise<[Error | null, any]> {
    const { apisId, ...param } = params;
    return http.post(`${baseUrl}/${apisId}/case`, param);
  }

  addApisCase (params: any[] = []): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/case`, params);
  }

  loadCaseInfo (caseId: string, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/case/${caseId}`, null, axiosConf);
  }

  patchCase (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case`, params);
  }

  replaceCase (params:any[] = []): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case`, params);
  }

  loadApiCases (params): Promise<[Error | null, any]> {
    const { ...param } = params;
    return http.get(`${baseUrl}/case/search`, param);
  }

  delCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/case`, caseIds, {
      dataType: true
    });
  }

  cloneCase (ids: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/case/clone`, ids);
  }

  execCase (caseIds: string[], apisId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${apisId}/case/exec`, { caseIds }, {
      paramsType: true
    });
  }

  enabledCase (enabled: boolean, caseIds: string[]) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case/enabled`, {
      enabled,
      ids: caseIds
    }, {
      paramsType: true
    });
  }

  updateCaseName (params: {id: string, name: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case/${params.id}/name?name=${params.name}`);
  }

  updateCasePriority (params: {id: string, priority: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case/${params.id}/priority?priority=${params.priority}`);
  }

  cancelAllFavourite (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/favourite`, {});
  }

  loadShareList (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/search`, params);
  }

  loadShareInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }

  addShare (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  delShare (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids: [id] });
  }

  patchShared (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  loadWatchList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/follow/search`, params);
  }

  cancelWatchAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/follow`, {});
  }

  loadFavouriteList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/favourite/search`, params);
  }

  loadApiInfoList (ids:string[]): Promise<[Error | null, any]> {
    return http.get(baseUrl + '/list/detail', { ids });
  }

  patchStatus ({ id = '', status = '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/status?status=${status}`);
  }

  addMockApiByApiId (apisId: string, params:{mockServiceId:string, summary?:string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${apisId}/association/mock/apis`, params);
  }

  getMockApiByApiId (apisId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${apisId}/association/mock/apis`);
  }

  putApiScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/generate`, params);
  }

  delApiScript (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/script`, { testTypes });
  }

  updateApiScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/update`, params);
  }

  getTrashData (params: {targetType: 'API'|'SERVICE', [key: string]: any}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/search`, { ...params });
  }

  delTrash (id: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${id}`);
  }

  delAllTrash <T> (params: T) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backAllTrash <T> (params: T, axioConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, axioConf);
  }

  backTrash (id: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${id}/back`);
  }

  delUnarchived (id: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/unarchived/${id}`);
  }

  delAllUnarchived () : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/unarchived`);
  }

  getDesignList <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/design/search`, params, axioConf);
  }

  addDesign <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/design`, params, axioConf);
  }

  updateDesign <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design`, params, axioConf);
  }

  getDesignInfo (designId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/design/${designId}`);
  }

  deleteDesign (designIds: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/design`, {ids: designIds});
  }

  exportDesign (params: {id: string, format: 'json'|'yaml'}, axioConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/design/export`, params, axioConf);
  }

  cloneDesign (designId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design/${designId}/clone`);
  }

  releaseDesign (designId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design/${designId}/release`);
  }

  generateServiceFromDesign (designId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design/${designId}/services/generate`);
  }

  importDesign (params:FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/design/import`, params);
  }

  putDesignContent (params: {id: string, openapi: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design/content`, params);
  }

  addDesignByService (serviceId) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design/services/${serviceId}/associate`);
  }
}

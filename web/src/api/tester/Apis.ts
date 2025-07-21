import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis';
  }

  addApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/archive`, params);
  }

  putApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  updateApi (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  cloneApi (params:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/clone`, { id: params });
  }

  moveApi (params: {apiIds: string[], targetProjectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/move`, params);
  }

  patchApiStatus ({ id = '', status = '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/status?status=${status}`);
  }

  deleteApi (params: { ids: string[] }): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, params);
  }

  getApiDetail (id:string, resolveRef = false, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, { resolveRef }, axiosConf);
  }

  getApiList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  addFavourite (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/favourite`, {});
  }

  cancelFavourite (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, {});
  }

  cancelAllFavourite (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/favourite`, {});
  }

  addFollow (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  cancelFollow (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, {});
  }

  cancelWatchAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/follow`, {});
  }

  updatePerf (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/perf?id=${params.id}`, params);
  }

  cancelPerf (id:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/perf/cancel`);
  }

  loadPerf (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/perf`);
  }

  addAuth (id: string, params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/auth`, params);
  }

  enabledAuth (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  updateAuth (authId: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  deleteAuth (authId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  getAuthList (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  getUserAuth ({ id = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/${userId}/auth`);
  }

  getCurrentAuth (id: string, admin = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin });
  }

  checkAuthPermission ({ id = '', authPermission = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/${authPermission}/check`, {
      userId
    });
  }

  restartTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  reopenTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  deleteTestTask (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  addCase (params: {[key: string]: any}): Promise<[Error | null, any]> {
    const { apisId, ...param } = params;
    return http.post(`${baseUrl}/${apisId}/case`, param);
  }

  addCases (params: any[] = []): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/case`, params);
  }

  getCaseDetail (caseId: string, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/case/${caseId}`, null, axiosConf);
  }

  patchCase (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case`, params);
  }

  putCase (params:any[] = []): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case`, params);
  }

  loadApiCases (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/case`, { ...params, fullTextSearch: true });
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

  addShare (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  deleteShare (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids: [id] });
  }

  patchShared (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  getShareList (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share`, { ...params, fullTextSearch: true });
  }

  getShareDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }

  addMockApiByApiId (apisId: string, params:{mockServiceId:string, summary?:string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${apisId}/association/mock/apis`, params);
  }

  getMockApiByApiId (apisId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${apisId}/association/mock/apis`);
  }

  generateTestScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/generate`, params);
  }

  deleteTestScript (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/script`, { testTypes });
  }

  updateTestScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/update`, params);
  }

  getTrashList (params: {targetType: 'API'|'SERVICE', [key: string]: any}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }

  deleteTrash (id: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${id}`);
  }

  deleteAllTrash <T> (params: T) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backAllTrash <T> (params: T, axioConf = {}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, axioConf);
  }

  backTrash (id: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${id}/back`);
  }

  addUnarchivedApi (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/unarchived`, params.dto);
  }

  updateUnarchivedApi (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/unarchived/${params.dto?.[0].id}`, params.dto);
  }

  deleteUnarchivedApi (id: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/unarchived/${id}`);
  }

  deleteAllUnarchivedApi () : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/unarchived`);
  }

  getUnarchivedApiDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/unarchived/${id}`);
  }

  getUnarchivedApiList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/unarchived`, { ...params, fullTextSearch: true });
  }

  getUnarchivedApiCount (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/unarchived/count`, params);
  }

  getDesignList <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/design`, { ...params, fullTextSearch: true }, axioConf);
  }

  addDesign <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/design`, params, axioConf);
  }

  updateDesign <T> (params: T, axioConf = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/design`, params, axioConf);
  }

  getDesignDetail (designId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/design/${designId}`);
  }

  deleteDesign (designIds: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/design`, { ids: designIds });
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

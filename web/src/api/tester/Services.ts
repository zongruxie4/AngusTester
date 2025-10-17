import { http } from '@xcan-angus/infra';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/services';
  }

  getList (params = {}, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  loadInfo (params: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${params}`, {});
  }

  addServices (params: any): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  updateServices (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${params.id}/name?name=${params.name}`);
  }

  cloneServices (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params.id}/clone`);
  }

  exportServices (params: any, conf = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/export`, params, conf);
  }

  loadServicesMockService (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/association/mock/service`);
  }

  getServicesSyncConfig (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/synchronization`);
  }

  putServicesSyncConfig<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/synchronization`, params);
  }

  delSyncInfo (id: string, names: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/synchronization`, { names: names });
  }

  loadSynchronizationTest<T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/synchronization/test`, params);
  }

  postSynchronizationExec (id: string, name?: string): Promise<[Error | null, any]> {
    const url = name ? `${baseUrl}/${id}/synchronization/exec?name=${name}` : `${baseUrl}/${id}/synchronization/exec`;
    return http.post(url);
  }

  getProjectServers <T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/schema/server`, params);
  }

  putServicesServerUrl<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/server`, params);
  }

  delServicesServerUrl (id: string, ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/schema/server`, { ids });
  }

  getServicesServerUrlInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/server`);
  }

  getServiceServerByServerId (serviceId: string, serverId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/schema/server/${serverId}`);
  }

  loadAuthority (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  loadApis (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    const { id, ...otherParams } = params;
    return http.get(`${baseUrl}/${id}/apis`, { ...otherParams, fullTextSearch: true }, axiosConfig);
  }

  resetTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  reopenTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  addAuth (id: string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/auth`, params);
  }

  updateAuth (authId: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  delAuth (authId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  updateAuthEnabled (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  localImport (params: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, params);
  }

  loadActivity ({ id, ...params }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/activity`, params);
  }

  loadActionAuth ({ id = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/${userId}/auth`);
  }

  loadApisByServicesId (id: string, params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/apis`, params);
  }

  patchStatus ({ id = '', status = '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/status?status=${status}`);
  }

  loadSchema (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema`);
  }

  putSchemaInfo (id: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/info`, params);
  }

  getCompData (id: string, types: string[], keys?: string[], config?:any, ignoreModel = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/type`, {
      types,
      keys,
      ignoreModel
    }, config);
  }

  getTags (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/tag`);
  }

  updateSchemaSecurity (id: string, security: Record<string, string[]>[]) {
    return http.put(`${baseUrl}/${id}/schema/securityRequirement/all`, security);
  }

  loadSecurity (id: string) {
    return http.get(`${baseUrl}/${id}/schema/securityRequirement`);
  }

  addServicesSchemaTag<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/tag`, params);
  }

  delServicesSchemaTag (id: string, names: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/schema/tag`, { names });
  }

  getServicesSchemaTag (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/tag`);
  }

  getServicesCompList (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/all?ignoreModel=true`);
  }

  addComponent (id:string, type:string, key:string, component:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/comp/${type}/${key}`, component, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  delComponent (id:string, refs:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/comp`, { refs });
  }

  getComponentRef (id:string, ref:string, config?:Record<string, any>): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/ref`, { ref }, config);
  }

  updateServiceApisServer (id:string, serverId:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/server/${serverId}/apis/sync`);
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

  getCurrentAuth (id: string, admin = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin });
  }

  importServicesSamples ():Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/example/import`);
  }

  batchAddParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchUpdateParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchDeleteParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchToggleEnabledParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/parameter/enabled?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  batchUpdateAuthentication (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/authentication?${queryStr}`, parameters);
  }

  batchUpdateServer (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/server?${queryStr}`, parameters);
  }

  batchUpdateReferenceVariable (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/variable/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  batchDeleteReferenceVariable (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/variable/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  batchAddReferenceDataset (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${serviceId}/apis/variable/dataset/reference?${queryStr}`, parameters);
  }

  batchDeleteReferenceDataset (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/variable/dataset/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  getOpenapi<T> (serviceId: string, params:T):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/openapi`, params);
  }

  getTestResult (serviceId: string):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/test/result`);
  }

  putOpenapi<T> (serviceId: string, params:T):Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${serviceId}/openapi?forced=true&gzipCompression=false`, params);
  }

  toggleTestEnabled (serviceId: string, enabled: boolean, params, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${serviceId}/test/enabled?enabled=${enabled}`, params, axiosConf);
  }

  translate (serviceId: string, queryParams: {sourceLanguage: string, targetLanguage: string}) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/translate`, queryParams, {
      paramsType: true
    });
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/script';
  }

  addScript (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateScript (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  deleteScript (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  cloneScript (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/clone`);
  }

  getScriptDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  getScriptList (params = {}, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  importScript (params: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, params);
  }

  importScriptDemo (): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/example/import`);
  }

  enableScriptAuth (scriptId: string, enabled) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${scriptId}/auth/enabled?enabled=${enabled}`);
  }

  deleteScriptAuth (authId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  putScriptAuth (authId: string, params: { permissions: string[] }) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  addScriptAuth (authId: string, params: { permissions: string[], authObjectType: string }) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${authId}/auth`, params);
  }

  getScriptAuthList (params = {}, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConf);
  }

  getScriptCurrentAuth (scriptIds:string[]): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current?admin=true`, { scriptIds });
  }
}

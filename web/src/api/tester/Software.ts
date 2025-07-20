import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/software';
  }

  searchList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/version`, { ...params, fullTextSearch: true });
  }

  delete (params = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/version`, params);
  }

  addVersion (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/version`, params);
  }

  updateVersion (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/version`, params);
  }

  getVersionInfo (versionId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/version/${versionId}`);
  }

  updateStatus (versionId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/version/${versionId}/status`, params, {
      paramsType: true
    });
  }

  merge (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/version/merge`, params, {
      paramsType: true
    });
  }
}

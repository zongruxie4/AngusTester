import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/software';
  }

  addSoftwareVersion (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/version`, params);
  }

  updateSoftwareVersion (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/version`, params);
  }

  updateSoftwareVersionStatus (versionId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/version/${versionId}/status`, params, {
      paramsType: true
    });
  }

  deleteSoftwareVersion (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/version`, { ids });
  }

  mergeSoftwareVersion (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/version/merge`, params, {
      paramsType: true
    });
  }

  getSoftwareVersionDetail (versionId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/version/${versionId}`);
  }

  getSoftwareVersionList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/version`, { ...params, fullTextSearch: true });
  }
}

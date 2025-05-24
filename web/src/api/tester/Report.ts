import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/report';
  }

  searchList (params, axiosConfig = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params, axiosConfig);
  }

  delete (ids: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  getReportInfo (reportId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}`);
  }

  getWideContent <T> (reportId: string, params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}/wide/content`, params);
  }

  getShareToken (reportId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}/shareToken`);
  }

  generateReport (reportId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${reportId}/generate/now`);
  }

  updateReport <T> (params: T): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  addReport <T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  toggleAuthEnabled (reportId: string, enabled): Promise<void> {
    return http.patch(`${baseUrl}/${reportId}/auth/enabled?enabled=${enabled}`);
  }

  deleteAuth (authId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  putAuth (authId: string, params: { permissions: string[]}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  addAuth (reportId: string, params: {permissions: string[], authObjectType: string}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${reportId}/auth`, params);
  }

  getAuth (params = {}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConf);
  }

  loadReportRecord (params = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/record`, params);
  }

  deleteReocrd (recordsIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/record`, recordsIds);
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/report';
  }

  addReport <T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateReport <T> (params: T): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  generateReport (reportId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${reportId}/generate/now`);
  }

  deleteReport (ids: string[]) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  getReportDetail (reportId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}`);
  }

  getReportWideContent <T> (reportId: string, params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}/wide/content`, params);
  }

  getReportList (params, axiosConfig = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  getReportShareToken (reportId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}/shareToken`);
  }

  enableReport (reportId: string, enabled): Promise<void> {
    return http.patch(`${baseUrl}/${reportId}/auth/enabled?enabled=${enabled}`);
  }

  getReportRecord (params = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/record`, params);
  }

  deleteReportRecord (recordsIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/record`, recordsIds);
  }

  addReportAuth (reportId: string, params: {permissions: string[], authObjectType: string}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${reportId}/auth`, params);
  }

  putReportAuth (authId: string, params: { permissions: string[]}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  deleteReportAuth (authId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  getReportAuth (params = {}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConf);
  }
}

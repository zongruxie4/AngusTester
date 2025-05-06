import { http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/report';
  }

  searchList (params) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
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
}

import { http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/report';
  }

  getReportInfo (reportId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}`);
  }

  getWideContent <T> (reportId: string, params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${reportId}/wide/content`, params);
  }
}

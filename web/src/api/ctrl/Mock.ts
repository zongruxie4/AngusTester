import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/mock';
  }

  getMockServiceMetrics (id:string, params?:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${id}/metrics`, params);
  }
}

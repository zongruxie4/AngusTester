import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis';
  }

  getApiShareInfo<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/view`, params);
  }

  getApisList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/apis`, { ...params, fullTextSearch: true });
  }

  getApiInfo (params: {id: string, password?: string, sid: string, spt: string}): Promise<[Error | null, any]> {
    const { id, ...param } = params;
    return http.get(`${baseUrl}/share/apis/${id}`, param);
  }
}

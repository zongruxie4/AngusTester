import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis/unarchived';
  }

  loadCount (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/count`, params);
  }

  loadUnarchivedList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  deleteAll (): Promise<[Error | null, any]> {
    return http.del(baseUrl);
  }

  del (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  loadInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  addApi (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params.dto);
  }

  updateApi (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.dto?.[0].id}`, params.dto);
  }
}

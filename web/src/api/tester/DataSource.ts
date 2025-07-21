import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/data';
  }

  add (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/datasource`, params);
  }

  put (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/datasource`, params);
  }

  delete (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/datasource/${id}`);
  }

  getList (params:{pageNo:number, pageSize:number, [key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource`, { ...params, fullTextSearch: true });
  }

  getScriptList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/script`, params);
  }

  getTable (id:string | undefined): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/${id}/table`);
  }

  testById (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/${id}/test`);
  }

  testByConfig (params:{username :string, password:string, jdbcurl:string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/test`, params);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/data';
  }

  getSourceScriptList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/script`, params);
  }

  getSourceList (params:{pageNo:number, pageSize:number, [key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/search`, params);
  }

  getSourceTable (id:string | undefined): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/${id}/table`);
  }

  addSource (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/datasource`, params);
  }

  testSourceById (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/${id}/test`);
  }

  testSource (params:{username :string, password:string, jdbcurl:string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/test`, params);
  }

  editSource (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/datasource`, params);
  }

  deleteSource (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/datasource/${id}`);
  }

  resolveSurface (params:{id:string, tableNames :string[]}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/datasource/parse`, params);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  loadNodes (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  loadList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  add (params): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  update (params): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  delete (params: any): Promise<[Error | null, any]> {
    return http.del(baseUrl, params);
  }

  installAgent (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params.id}/agent/install`, params);
  }

  enable (params: Array<any>): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  test (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/test`, params);
  }

  detail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, {});
  }

  restartProxy (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/agent/restart`);
  }

  loadRunnerProcess (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process`, params, { paramsType: true });
  }

  killRunnerProcess (params) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process/kill`, params);
  }
}

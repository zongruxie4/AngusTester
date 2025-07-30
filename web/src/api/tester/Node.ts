import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  addNode (params): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  updateNode (params): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  deleteNode (params: any): Promise<[Error | null, any]> {
    return http.del(baseUrl, params);
  }

  enableNode (params: Array<any>): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  testNodeConnection (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/test`, params);
  }

  getNodeDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, {});
  }

  getNodeList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  installNodeAgent (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params.id}/agent/install`, params);
  }

  restartNodeAgent (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/agent/restart`);
  }

  getRunnerProcess (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process`, params, { paramsType: true });
  }

  killRunnerProcess (params) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process/kill`, params);
  }
}

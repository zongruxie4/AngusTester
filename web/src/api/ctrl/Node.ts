import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  getNodeMetrics (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/latest`);
  }

  getNodeNetwork (id: string): Promise<[Error | null, any]> {
    // return http.get(`${baseUrl}/${id}/metrics/network`, param);
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  getCpuData (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/cpu`, params);
  }

  getDiskName (id): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk/info`);
  }

  getDiskData (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/disk`, params);
  }

  getMemoryData (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/memory`, params);
  }

  getNetworkName (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  getNetworkData (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/network`, params);
  }

  getNodeStatus (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/agent/status`, { ids: [id] });
  }

  getNodeExec (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/info/${id}/exec`);
  }
}

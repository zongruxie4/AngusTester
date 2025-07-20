import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  getLatestMetrics (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/latest`);
  }

  getCpuMetrics (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/cpu`, params);
  }

  getDiskInfoMetrics (id): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk/info`);
  }

  getDiskMetrics (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/disk`, params);
  }

  getMemoryMetrics (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/memory`, params);
  }

  getNetworkInfoMetrics (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  getNetworkMetrics (nodeId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${nodeId}/metrics/network`, params);
  }

  getAgentStatus (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/agent/status`, { ids: [id] });
  }

  getExecInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/info/${id}/exec`);
  }
}

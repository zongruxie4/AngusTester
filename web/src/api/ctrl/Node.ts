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

  getCpuData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/cpu`, param);
  }

  getDiskName (id): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk/info`);
  }

  getDiskData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk`, param);
  }

  getMemoryData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/memory`, param);
  }

  getNetworkName (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  getNetworkData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network`, param);
  }

  getNodeStatus (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/agent/status`, { ids: [id] });
  }

  getNodeExec (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/info/${id}/exec`);
  }
}

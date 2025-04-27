import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  // 获取节点 list的监控数据
  getNodeMetrics (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/latest`);
  }

  // 获取节点网络使用率
  getNodeNetwork (id: string): Promise<[Error | null, any]> {
    // return http.get(`${baseUrl}/${id}/metrics/network`, param);
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  // 获取节点 CPU 使用数据
  getCpuData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/cpu`, param);
  }

  // 获取节点 所有磁盘 名称
  getDiskName (id): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk/info`);
  }

  // 获取节点 磁盘 使用数据
  getDiskData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/disk`, param);
  }

  // 获取节点 内存数据
  getMemoryData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/memory`, param);
  }

  // 获取节点 所有网络名称
  getNetworkName (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network/info`);
  }

  // 获取节点 网络数据
  getNetworkData ({ id = '', ...param }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/network`, param);
  }

  // 获取节点状态
  getNodeStatus (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/agent/status`, { ids: [id] });
  }

  // 获取节点的执行任务
  getNodeExec (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/info/${id}/exec`);
  }
}

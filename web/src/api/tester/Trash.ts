import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/trash';
  }

  // 回收站总数
  loadCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/count`);
  }

  // 获取回收站列表
  loadAllocateList (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 删除
  del (params: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${params}`, {});
  }

  // 删除全部
  delAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`);
  }

  // 恢复
  back (params: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params}/back`);
  }

  // 恢复所有
  backAll (): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/back`);
  }

  // 回收站总数量
  loadScenarioCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/count`);
  }

  // 目录 list
  loadScenarioDirList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/search`, { ...params, targetType: 'DIR' });
  }

  // 场景 list
  loadScenarioSceneList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/search`, { ...params, targetType: 'SCENARIO' });
  }

  // 删除单个场景
  deleteScenario (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/scenario/${id}`);
  }

  // 删除全部场景
  delScenarioAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/scenario`);
  }

  // 恢复单个场景
  recoveryScenario (id): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/scenario/${id}/back`);
  }

  // 恢复全部场景
  recoveryScenarioAll (): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/scenario/back`);
  }

  loadTaskCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/count`);
  }
}

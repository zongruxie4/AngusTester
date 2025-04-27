import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/script';
  }

  // 获取脚本列表
  loadScriptList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 获取脚本列表操作权限
  loadScriptListAuth (scriptIds:string[]): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current?adminFlag=true`, { scriptIds });
  }

  // 添加
  add (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 更新
  update (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  // 删除
  delete (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  // 克隆
  clone (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/clone`);
  }

  // 获取脚本详情
  loadDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  // 获取脚本详情
  import (params: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, params);
  }

  // 获取脚本详情
  importDemo (): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/example/import`);
  }
}

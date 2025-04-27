import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/scenario';
  }

  loadScenario (params: {[key: string]: any}):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  patchScripts (params: {dirId: string, id: string, name: string, scriptYaml?: string}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  saveScriptPerformance (id:string, params:{ concurrency: string, errorRate: string, apt: string, ops: string, percentile:string }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}`, params);
  }

  delScript (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  addScriptVariable (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/variables`, params);
  }

  // 查询
  checkAuth ({ id = '', authPermission = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/${authPermission}/check`, {
      userId
    });
  }

  // 添加权限
  updateAuth (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${params.id}`, params);
  }

  // 权限标志
  updateAuthFlag (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  // 获取权限数据
  loadAuthority (params): Promise<[Error | null, any]> {
    params.scenarioId = params.id;
    return http.get(`${baseUrl}/auth`, params);
  }

  // 添加权限数
  addAuth (params): Promise<[Error | null, any]> {
    const { id, ...other } = params;
    return http.post(`${baseUrl}/${id}/auth`, other);
  }

  // 删除权限
  delAuth (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${id}`);
  }

  // 获取场景详情
  loadInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  // 获取变量列表
  loadVariableList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/variables`, params);
  }

  delVariable (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/variables/${id}`);
  }

  loadScriptComment (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/comment`, params);
  }

  addFavoriteScript (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/favourite`);
  }

  delFavoriteScript (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`);
  }

  delAllFavoriteScript (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/favourite/all`);
  }

  addFollowScript (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  delFollowScript (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`);
  }

  delAllFollowScript (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/follow/all`);
  }

  // 克隆场景
  cloneScenario (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/clone`);
  }

  // 设置为测试任务
  setTestTask (id:string, params:any[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test`, params);
  }

  // 重新打开测试任务
  reOpen (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  // 重新测试
  reStart (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  // 删除测试任务
  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  // 获取场景权限
  loadScenePermissions (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { adminFlag: true });
  }

  // 添加场景
  addScene (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 移动场景
  moveScenario (id:string, pid:string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/${pid}/move`);
  }
}

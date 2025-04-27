import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis';
  }

  // 克隆
  patchClone (params:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/clone`, { id: params });
  }

  // 移动
  patchMove (params: {apiIds: string[], targetProjectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/move`, params);
  }

  // 删除
  del (params: { ids: string[] }): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, params);
  }

  // 查看详情
  loadInfo (id:string, resolveRefFlag = false): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, { resolveRefFlag });
  }

  // 添加 socketapi
  putApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  // 添加保存
  addApi (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/archive`, params);
  }

  // 修改保存
  updateApi (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  //  添加收藏
  addFavourite (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params}/favourite`, {});
  }

  // 取消收藏
  cancelFavourite (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, {});
  }

  // 保存接口性能指标
  updatePerf (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/perf?id=${params.id}`, params);
  }

  // 清除所有为归档
  delNotArchiveList (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/archive`);
  }

  // 查询
  searchCollectLsit (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 取消接口性能指标
  cancelPerf (id:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/perf/cancel`);
  }

  // 查询接口性能指标
  loadPerf (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/perf`);
  }

  addNewApi (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 获取接口权限
  loadApiAuthority (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params);
  }

  // 更改是否有权限控制
  updateAuthFlag (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  // 权限弹窗 添加权限
  addAuth (params: any): Promise<[Error | null, any]> {
    const { id, ...other } = params;
    return http.post(`${baseUrl}/${id}/auth`, other);
  }

  // 增加关注
  addWatch (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  // 取消关注
  cancelWatch (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, {});
  }

  // 修改权限
  updateAuth (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${params.id}`, params);
  }

  // 删除授权
  delAuth (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${id}`);
  }

  // 执行测试信息
  loadTest (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/tester`);
  }

  // 查询分配测试人
  loadDistrbutionTest (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${params.id}/test`);
  }

  // 修改分配测试人
  putDistrubutionTest (params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${params.id}/test`, params.dto);
  }

  // 重新测试
  retest (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  // 重新打开测试任务
  reOpen (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  // 查询操作权限
  loadActionAuth ({ id = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/${userId}/auth`);
  }

  // 查看个人权限
  loadUserAuth (id: string, adminFlag = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { adminFlag });
  }

  // 查询
  checkAuth ({ id = '', authPermission = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/${authPermission}/check`, {
      userId
    });
  }

  // 添加用例
  addCase (params: {[key: string]: any}): Promise<[Error | null, any]> {
    const { apisId, ...param } = params;
    return http.post(`${baseUrl}/${apisId}/case`, param);
  }

  // 详情
  loadCaseInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/case/${id}`);
  }

  // 修改
  patchCase (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case`, params);
  }

  // case list API
  loadApiCases (params): Promise<[Error | null, any]> {
    const { ...param } = params;
    return http.get(`${baseUrl}/case/search`, param);
  }

  // 用例
  // 删除
  delCase (caseIds: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/case`, caseIds, {
      dataType: true
    });
  }

  // 克隆
  cloneCase (ids: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/case/clone`, ids);
  }

  // 执行用例
  execCase (caseIds: string[], apisId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${apisId}/case/exec`, { caseIds }, {
      paramsType: true
    });
  }

  // 启用禁用用例
  enabledCase (enabled: boolean, caseIds: string[]) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/case/enabled`, {
      enabled,
      ids: caseIds
    }, {
      paramsType: true
    });
  }

  // 保存 name
  updateCaseName (params: {id: string, name: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case/${params.id}/name?name=${params.name}`);
  }

  // 保存 等级
  updateCasePriority (params: {id: string, priority: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/case/${params.id}/priority?priority=${params.priority}`);
  }

  // 收藏
  // 取消全部收藏 /api/v1/apis/favourite/cancelAll
  cancelAllFavourite (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/favourite`, {});
  }

  // 获取分享历史列表
  loadShareList (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/search`, params);
  }

  // 分享详情
  loadShareInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }

  // 添加分享
  addShare (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  // 删除分享
  delShare (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids: [id] });
  }

  // 修改
  patchShared (params:any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  // 获取关注列表
  loadWatchList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/follow/search`, params);
  }

  // 取消全部关注
  cancelWatchAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/follow`, {});
  }

  // 获取关注列表
  loadFavouriteList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/favourite/search`, params);
  }

  loadApiInfoList (ids:string[]): Promise<[Error | null, any]> {
    return http.get(baseUrl + '/list/detail', { ids });
  }

  // 修改接口状态
  patchStatus ({ id = '', status = '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/status?status=${status}`);
  }

  addMockApiByApiId (apisId: string, params:{mockServiceId:string, summary?:string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${apisId}/association/mock/apis`, params);
  }

  getMockApiByApiId (apisId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${apisId}/association/mock/apis`);
  }

  // 生成测试脚本
  putApiScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/generate`, params);
  }

  // 删除测试脚本
  delApiScript (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/script`, { testTypes });
  }

  // 更新测试脚本
  updateApiScript (id: string, params: {duration: string; priority: string; testType: string; threads: string}[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test/script/update`, params);
  }

  // 获取回收站数据
  getTrashData (params: {targetType: 'API'|'SERVICE', [key: string]: any}) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/search`, { ...params });
  }

  // 删除回收站数据
  delTrash (id: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${id}`);
  }

  // 删除回收站所有数据
  delAllTrash () : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`);
  }

  // 还原回收站所有数据
  backAllTrash () : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`);
  }

  // 还原数据
  backTrash (id: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${id}/back`);
  }
}

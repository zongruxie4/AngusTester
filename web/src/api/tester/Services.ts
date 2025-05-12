import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/services';
  }

  // 获取服务列表
  loadList (params = {}, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params, axiosConfig);
  }

  // 获取服务详情信息
  loadInfo (params: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${params}`, {});
  }

  // 添加服务
  addServices (params: any): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  // 更新项目
  updateServices (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${params.id}/name?name=${params.name}`);
  }

  // TODO Q2 肯定有用到为什么没找到？ 删除服务
  delProject (params: any): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${params.id}`, {});
  }

  // 克隆服务
  cloneServices (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params.id}/clone`);
  }

  // 导出服务
  exportServices (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/export`, params);
  }

  loadServicesMockService (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/association/mock/service`);
  }

  getServicesSyncConfig (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/synchronization`);
  }

  putServicesSyncConfig<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/synchronization`, params);
  }

  delSyncInfo (id: string, names: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/synchronization`, { names: names });
  }

  loadSynchronizationTest<T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/synchronization/test`, params);
  }

  postSynchronizationExec (id: string, name?: string): Promise<[Error | null, any]> {
    const url = name ? `${baseUrl}/${id}/synchronization/exec?name=${name}` : `${baseUrl}/${id}/synchronization/exec`;
    return http.post(url);
  }

  getProjectServers <T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/schema/server`, params);
  }

  putServicesServerUrl<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/server`, params);
  }

  delServicesServerUrl (id: string, ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/schema/server`, { ids });
  }

  getServicesServerUrlInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/server`);
  }

  getServiceServerByServerId (serviceId: string, serverId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/schema/server/${serverId}`);
  }

  loadAuthority (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  // 查询apis列表
  loadApis (params: any, axiosConfig = {}): Promise<[Error | null, any]> {
    const { id, ...param } = params;
    return http.get(`${baseUrl}/${id}/apis/search`, param, axiosConfig);
  }

  // 重新测试
  resetTest (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  // 重新打开测试任务
  reOpen (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  // 删除测试任务
  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  // 权限弹窗 添加权限
  addAuth (id: string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/auth`, params);
  }

  // 修改权限
  updateAuth (authId: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  // 删除授权
  delAuth (authId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${authId}`);
  }

  // 更改是否有权限控制
  updateAuthEnabled (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  // 本地导入
  localImport (params: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, params);
  }

  // 服务活动记录
  loadActivity ({ id, ...params }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/activity`, params);
  }

  // 查询操作权限
  loadActionAuth ({ id = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/${userId}/auth`);
  }

  loadApisByServicesId (id: string, params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/apis`, params);
  }

  // 修改接口状态
  patchStatus ({ id = '', status = '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/status?status=${status}`);
  }

  loadSchema (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema`);
  }

  putSchemaInfo (id: string, params: {
    contact?: {
      email: string;
      extensions?: Record<string, string>,
      name: string;
      url: string;
    },
    description?: string;
    extensions?: Record<string, string>;
    license?: {
      extensions?: Record<string, string>;
      identifier?: string;
      name: string;
      url: string;
    },
    summary?: string;
    termsOfService?: string;
    title?: string;
    version?: string;
  }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/info`, params);
  }

  // 服务常用资源
  getCompData (id: string, types: string[], keys?: string[], config?:any, ignoreModel = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/type`, {
      types,
      keys,
      ignoreModel
    }, config);
  }

  // 服务配置的 tagSelector
  getTags (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/tag`);
  }

  // 修改服务 schema 安全需求
  updateSchemaSecurity (id: string, security: Record<string, string[]>[]) {
    return http.put(`${baseUrl}/${id}/schema/securityRequirement/all`, security);
  }

  // 获取服务 安全需求
  loadSecurity (id: string) {
    return http.get(`${baseUrl}/${id}/schema/securityRequirement`);
  }

  addServicesSchemaTag<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/tag`, params);
  }

  delServicesSchemaTag (id: string, names: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/schema/tag`, { names });
  }

  getServicesSchemaTag (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/schema/tag`);
  }

  getServicesCompList (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/all?ignoreModel=true`);
  }

  addComponent (id:string, type:string, key:string, component:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/comp/${type}/${key}`, component, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  delComponent (id:string, refs:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/comp`, { refs });
  }

  // 查询ref详情
  getRefInfo (id:string, ref:string, config?:Record<string, any>): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/comp/ref`, { ref }, config);
  }


  updateServiceApisServer (id:string, serverId:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/schema/server/${serverId}/apis/sync`);
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

  // 获取服务权限信息
  getCurrentAuth (id: string, admin = true): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin });
  }

  // 导入服务样例 TODO 功能已删除
  importServicesSamples ():Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/example/import`);
  }

  batchAddParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchUpdateParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchDeleteParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/parameter?${queryStr}`, parameters);
  }

  batchToggleEnabledParams (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/parameter/enabled?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  batchUpdateAuthentication (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/authentication?${queryStr}`, parameters);
  }

  batchUpdateServer (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/server?${queryStr}`, parameters);
  }

  batchUpdateReferenceVariable (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${serviceId}/apis/variable/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }



  batchDeleteReferenceVariable (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/variable/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  batchDeleteReferenceDataset (serviceId: string, queryStr: string, parameters):Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${serviceId}/apis/variable/dataset/reference?${queryStr}`, parameters, {
      paramsType: true
    });
  }

  getOpenapi<T> (serviceId: string, params:T):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/openapi`, params);
  }

  getTestResult (serviceId: string):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${serviceId}/test/result`);
  }

  putOpenapi<T> (serviceId: string, params:T):Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${serviceId}/openapi?forced=true&gzipCompression=false`, params);
  }

  toggleTestEnabled (serviceId: string, enabled: boolean, params, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${serviceId}/test/enabled?enabled=${enabled}`, params, axiosConf);
  }
}

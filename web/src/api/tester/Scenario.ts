import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/scenario';
  }

  loadScenario (params: {[key: string]: any}, axiosConfig = {}):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  patchScripts (params: {dirId: string, id: string, name: string, scriptYaml?: string}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  saveScriptPerformance (id:string, params:{ concurrency: string, errorRate: string, apt: string, ops: string, percentile:string }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}`, params);
  }

  deleteScenario (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  addScriptVariable (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/variables`, params);
  }

  checkAuth ({ id = '', authPermission = '', userId = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/${authPermission}/check`, {
      userId
    });
  }

  putAuth (authId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  updateAuthFlag (scenarioId: string, enabled: boolean): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${scenarioId}/auth/enabled?enabled=${enabled}`);
  }

  loadAuthority (params, axiosConfig): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  addAuth (scenarioId: string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${scenarioId}/auth`, params);
  }

  delAuth (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${id}`);
  }

  loadInfo (id: string, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, axiosConf);
  }

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

  delFollowScenario (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`);
  }

  delAllFollowScript (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/follow/all`);
  }

  cloneScenario (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/clone`);
  }

  setTestTask (id:string, params:any[]): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/test`, params);
  }

  reOpen (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  reStart (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  deleteTest (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  loadScenePermissions (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin: true });
  }

  addScenario (params, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params, axiosConfig);
  }

  putScenario (params, axiosConfig = {}) {
    return http.put(`${baseUrl}`, params, axiosConfig);
  }

  moveScenario (id:string, pid:string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/${pid}/move`);
  }

  addMonitor (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/monitor`, params);
  }

  updateMonitor (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/monitor`, params);
  }

  deleteMonitor (params): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/monitor`, params);
  }

  runMonitor (monitorId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/monitor/${monitorId}/run`);
  }

  searchMonitor (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor`, { ...params, fullTextSearch: true });
  }

  getMonitorInfo (monitorId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/${monitorId}`);
  }

  getMonitorHistory (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/history`, params);
  }

  getExecInfoByMonitorHistoryId (historyId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/history/${historyId}`);
  }

  getTestSchemaServer (scenarioId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${scenarioId}/test/schema/server`);
  }

  backAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  deleteAllTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  backTrash (trashId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${trashId}/back`);
  }

  deleteTrash (trashId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${trashId}`);
  }

  searchTrash (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }
}

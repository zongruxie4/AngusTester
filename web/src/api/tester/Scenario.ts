import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/scenario';
  }

  addScenario (params, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params, axiosConfig);
  }

  putScenario (params, axiosConfig = {}) {
    return http.put(`${baseUrl}`, params, axiosConfig);
  }

  cloneScenario (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/clone`);
  }

  deleteScenario (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  getScenarioDetail (id: string, axiosConf = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, axiosConf);
  }

  getTestSchemaServer (scenarioId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${scenarioId}/test/schema/server`);
  }

  getScenarioList (params: {[key: string]: any}, axiosConfig = {}):Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  reopenTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/reopen`);
  }

  restartTestTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/test/task/restart`);
  }

  deleteTestTask (id: string, testTypes: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/test/task`, { testTypes });
  }

  addScenarioFavorite (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/favourite`);
  }

  deleteScenarioFavorite (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`);
  }

  addScenarioFollow (id:string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`);
  }

  deleteScenarioFollow (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`);
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

  getMonitorDetail (monitorId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/${monitorId}`);
  }

  getMonitorList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor`, { ...params, fullTextSearch: true });
  }

  getMonitorHistoryDetail (historyId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/history/${historyId}`);
  }

  getMonitorHistoryList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/monitor/history`, params);
  }

  backScenarioTrash (trashId: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${trashId}/back`);
  }

  backAllScenarioTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  deleteScenarioTrash (trashId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${trashId}`);
  }

  deleteAllScenarioTrash (params: {projectId: string}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  getScenarioTrashList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, fullTextSearch: true });
  }

  addScenarioAuth (scenarioId: string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${scenarioId}/auth`, params);
  }

  putScenarioAuth (authId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  deleteScenarioAuth (id:string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${id}`);
  }

  enableScenarioAuth (scenarioId: string, enabled: boolean): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${scenarioId}/auth/enabled?enabled=${enabled}`);
  }

  getCurrentScenarioAuth (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin: true });
  }

  getScenarioAuthList (params, axiosConfig): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  searchList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  putConfig (execId:string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/config`, params);
  }

  deleteExec (execIds: string[], axiosConf = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, execIds, axiosConf);
  }

  startExec (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/start`, params);
  }

  stopExec (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stop`, params);
  }

  loadExecTestResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/result`);
  }

  getExecResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/result`);
  }

  getExecInfo (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}`);
  }

  getSampleExtensionContent <T> (execId: string, params: T) : Promise<string[]> {
    return http.get(`${baseUrl}/${execId}/sample/extension/content`, params);
  }

  execByScript (params: {scriptId: string}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params, axiosConf);
  }

  getScenarioResult (scenarioId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/${scenarioId}/result`);
  }

  getSampleSummaryList (execId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/sample/summary/list`, params);
  }

  getSampleErrorCounterLatest (execId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/sample/errors/counter/latest`);
  }

  getSampleErrContent (execId: string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/sample/error/content`, params);
  }

  getSampleExtensionCountMapLatest (execId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/sample/extension/counter/map1/latest`);
  }

  getTestServer (execId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/server`);
  }

  startDebug (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/debug/scenario/start`, params);
  }

  loadDebugScenarioInfo (scenarioId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/debug/scenario/${scenarioId}`);
  }

  putScriptConfig (execId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/script/config`, params);
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  addExecByScript (params: {scriptId: string}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params, axiosConf);
  }

  putExecConfig (execId:string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/config`, params);
  }

  putExecScriptConfig (execId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/script/config`, params);
  }

  startExec (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/start`, params);
  }

  stopExec (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stop`, params);
  }

  deleteExec (execIds: string[], axiosConf = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, execIds, axiosConf);
  }

  getExecDetail (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}`);
  }

  getExecList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  startScenarioDebug (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/debug/scenario/start`, params);
  }

  getScenarioDebugDetail (scenarioId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/debug/scenario/${scenarioId}`);
  }

  getTestServer (execId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/server`);
  }

  getTestResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/result`);
  }

  getResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/result`);
  }

  getSampleExtensionContent <T> (execId: string, params: T) : Promise<string[]> {
    return http.get(`${baseUrl}/${execId}/sample/extension/content`, params);
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
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  addByScript (params: {scriptId: string}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params, axiosConf);
  }

  putConfig (execId:string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/config`, params);
  }

  putScriptConfig (execId: string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${execId}/script/config`, params);
  }

  start (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/start`, params);
  }

  stop (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stop`, params);
  }

  delete (execIds: string[], axiosConf = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, execIds, axiosConf);
  }

  getDetail (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}`);
  }

  getList (params): Promise<[Error | null, any]> {
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

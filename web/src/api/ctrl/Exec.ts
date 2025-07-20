import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  addByScript (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params);
  }

  putScriptConfig (id:string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/script/config`, params);
  }

  getList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, params);
  }

  putConfig (id:string, params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/config`, params);
  }

  restart (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/start`, params);
  }

  stop (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stop`, params);
  }

  delete (ids:any): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, ids, { dataType: true });
  }

  getDetail (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, undefined, { silence: true });
  }

  getInfo (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/info`);
  }

  getScriptByExecId (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/script`);
  }

  getSummaryList (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/summary/list`, params);
  }

  getThroughput (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/throughput`, params);
  }

  getThread (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/thread`, params);
  }

  getResponseTime (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/score`, params);
  }

  getError (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/error`, params);
  }

  getLatestErrorCounter (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/errors/counter/latest`);
  }

  getErrorContent (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/error/content`, params);
  }

  getLatestStatusCodeCount (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/counter/latest`, params);
  }

  getStatusCodeCountByGroup (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/counter/map1/latest`);
  }

  getSampleExtensionContent (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/content`, params);
  }

  getSummaryMetricsByTask (id:string, taskName:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/summary/${taskName}`);
  }

  getScoreMetrics (id:string, params:{pageNo:number, pageSize:number}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/score`, params, { silence: true });
  }

  getSummariesMetrics (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/summaries`);
  }

  getExecResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/result`);
  }

  getApiTestResultByType (apisId: string, scriptType: 'TEST_PERFORMANCE'|'TEST_STABILITY'|'TEST_FUNCTIONAL'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/${apisId}/${scriptType}/result`);
  }

  getScenarioTestResult (scenarioId: string, scriptType: 'TEST_PERFORMANCE'|'TEST_STABILITY'|'TEST_FUNCTIONAL'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/${scenarioId}/${scriptType}/result`);
  }

  getServiceTestResult (serviceId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${serviceId}/result`);
  }

  getApiTestResult (apisId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/${apisId}/result`);
  }

  startDebug <T> (params: T) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/debug/script/start`, params);
  }

  getDebugScriptInfo (scriptId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/debug/script/${scriptId}`);
  }
}

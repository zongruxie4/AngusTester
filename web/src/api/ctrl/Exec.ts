import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  addExecByScript (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params);
  }

  editExecByScript (id:string, params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/script/config`, params);
  }

  loadExecList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  updateExec (id:string, params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/config`, params);
  }

  updateExecSetting (id:string, params:any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/script/config`, params);
  }

  restart (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/start`, params);
  }

  stop (params:any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stop`, params);
  }

  del (ids:any): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, ids, { dataType: true });
  }

  loadEInExecuteList (params:{filters:Record<string, any>[]}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params, { silence: true });
  }

  getDetail (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, undefined, { silence: true });
  }

  getInfo (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/info`);
  }

  loadScriptByExecId (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/script`);
  }

  getAggregateData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/summary/list`, params);
  }

  loadThroughputData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/throughput`, params);
  }

  loadThreadData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/thread`, params);
  }

  loadTResponseTimeData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/score`, params);
  }

  loadErrorData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/error`, params);
  }

  loadErrorCounterLatest (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/errors/counter/latest`);
  }

  loadSampleErrorContent (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/error/content`, params);
  }

  loadRequestStatusCodeData (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/counter/latest`, params);
  }

  loadStatusCodeData (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/counter/latest`);
  }

  loadSummary (id:string, taskName:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/summary/${taskName}`);
  }

  loadMetricsScore (id:string, params:{pageNo:number, pageSize:number}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/score`, params, { silence: true });
  }

  loadMetricsSummaries (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/metrics/summaries`);
  }

  loadExtensionContent (id:string, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/sample/extension/content`, params);
  }

  loadExecTestResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/result`);
  }

  loadApiExecTestResult (apisId: string, scriptType: 'TEST_PERFORMANCE'|'TEST_STABILITY'|'TEST_FUNCTIONAL'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/${apisId}/${scriptType}/result`);
  }

  loadScenarioTestResult (scenarioId: string, scriptType: 'TEST_PERFORMANCE'|'TEST_STABILITY'|'TEST_FUNCTIONAL'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/${scenarioId}/${scriptType}/result`);
  }

  loadServiceTestResult (serviceId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${serviceId}/result`);
  }
}

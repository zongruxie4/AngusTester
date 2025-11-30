import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/setting';
  }

  getUserApiProxy () : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/apis/proxy`);
  }

  enabledUserApiProxy (params: {name: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/user/apis/proxy/enabled`, params);
  }

  patchUserApiProxyUrl (params = { url: '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/user/apis/proxy`, params);
  }

  getTenantApiProxy (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/apis/proxy`);
  }

  updateTenantApiProxy (params:{enabled:boolean, url:string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/apis/proxy`, params);
  }

  getEvaluationIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/evaluation`);
  }

  saveEvaluationIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/evaluation`, params);
  }

  getPerfIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/perf`);
  }

  savePerIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/perf`, params);
  }

  getStabilityIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/stability`);
  }

  saveStabilityIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/stability`, params);
  }

  getFuncIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/func`);
  }

  saveFuncIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/func`, params);
  }

  putEventNotice (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/tester/event`, params);
  }

  getEventNoticeType () : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/tester/event`);
  }
}

import { http } from '@xcan-angus/tools';

type SettingKey = 'API_LOG_CONFIG' | 'COMMON_DATA_PERMISSION' | 'HEALTH_CHECK' | 'LOCALE' | 'MAX_METRICS_DAYS' | 'MAX_RESOURCE_ACTIVITIES' | 'OPERATION_LOG_CONFIG' | 'PREF_INDICATOR' | 'QUOTA' | 'SECURITY' | 'SOCIAL' | 'STABILITY_INDICATOR' | 'SYSTEM_LOG_CONFIG' | 'THEME';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/setting';
  }

  getSettingByKey (key:SettingKey): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${key}`);
  }

  loadSampleList (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/API_SAMPLE`);
  }

  getProxy (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/apis/proxy`);
  }

  putProxy (params: {name: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/user/apis/proxy/enabled`, params);
  }

  patchClientUrl (params = { url: '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/user/apis/proxy`, params);
  }

  loadOtherPermissions (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/data/permission`, params);
  }

  editPermissions (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/data/permission`, params);
  }

  loadDataList (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/apis/proxy`);
  }

  putProxySetting (params:{enabled:boolean, url:string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/apis/proxy`, params);
  }

  loadPerfIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/perf`);
  }

  savePerIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/perf`, params);
  }

  loadStabilityIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/stability`);
  }

  saveStabilityIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/stability`, params);
  }

  loadFuncIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/func`);
  }

  saveFuncIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/func`, params);
  }

  loadQuotaList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota/search`, params);
  }

  loadQuotaByName (resourceName: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota/${resourceName}`);
  }

  putEventNotice (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/tester/event`, params);
  }

  getEventNoticeType () : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/tester/event`);
  }
}

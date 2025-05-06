import {GM, http} from '@xcan-angus/tools';

type SettingKey = 'API_LOG_CONFIG' | 'COMMON_DATA_PERMISSION' | 'HEALTH_CHECK' | 'LOCALE' | 'MAX_METRICS_DAYS' | 'MAX_RESOURCE_ACTIVITIES' | 'OPERATION_LOG_CONFIG' | 'PREF_INDICATOR' | 'QUOTA' | 'SECURITY' | 'SOCIAL' | 'STABILITY_INDICATOR' | 'SYSTEM_LOG_CONFIG' | 'THEME';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/setting';
  }

  // 获取样例 list
  getSettingByKey (key:SettingKey): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${key}`);
  }

  // 获取样例 list
  loadSampleList (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/API_SAMPLE`);
  }

  // 获取代理配置
  getProxy (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/apis/proxy`);
  }

  // 修改代理配置
  putProxy (params: {name: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/user/apis/proxy/enabled`, params);
  }

  // 更新客户端代理地址
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

  // 获取默认性能指标
  loadPerfIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/perf`);
  }

  // 保存默认性能指标
  savePerIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/perf`, params);
  }

  // 获取默认稳定性指标
  loadStabilityIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/stability`);
  }

  // 保存默认稳定性指标
  saveStabilityIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/stability`, params);
  }

  // 获取默认功能指标
  loadFuncIndicator (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/indicator/func`);
  }

  // 保存默认功能指标
  saveFuncIndicator (params = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/indicator/func`, params);
  }

  // 获取空间配额 list
  loadQuotaList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota/search`, params);
  }

  // 获取某项资源配额
  loadQuataByName (resourceName: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota/${resourceName}`);
  }

  putEventNotice (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/tenant/tester/event`, params);
  }

}

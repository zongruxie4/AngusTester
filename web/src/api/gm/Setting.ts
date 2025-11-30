import { http } from '@xcan-angus/infra';

type SettingKey = 'API_LOG_CONFIG' | 'HEALTH_CHECK' | 'LOCALE' | 'MAX_METRICS_DAYS' | 'MAX_RESOURCE_ACTIVITIES' | 'OPERATION_LOG_CONFIG' | 'QUOTA' | 'SECURITY' | 'SOCIAL' | 'SYSTEM_LOG_CONFIG' | 'THEME';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/setting';
  }

  getSettingByKey (key:SettingKey): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${key}`);
  }

  getQuotaList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota`, params);
  }

  getQuotaByName (resourceName: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/quota/${resourceName}`);
  }

}

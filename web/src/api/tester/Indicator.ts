import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/indicator';
  }

  addStability (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stability`, params);
  }

  addPerf (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/perf`, params);
  }

  updateStability (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/stability`, params);
  }

  updatePerf (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/perf`, params);
  }

  deleteStability (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/stability`, { ids });
  }

  deletePerf (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/perf`, { ids });
  }

  getPerfDetail (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/perf`);
  }

  getStabilityDetail (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/stability`);
  }

  getStabilityList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/stability`, { ...params, fullTextSearch: true });
  }

  getPerfList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/perf`, { ...params, fullTextSearch: true });
  }

  getDefaultPerf (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/perf/detailOrDefault`);
  }

  getDefaultStability (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/stability/detailOrDefault`);
  }

  cancelApiStability (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/API/${id}/stability/cancel`);
  }

  cancelApiPerf (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/API/${id}/perf/cancel`);
  }

  cancelProStability (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${targetType}/${id}/stability/cancel`);
  }

  cancelProPerf (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${targetType}/${id}/perf/cancel`);
  }

  cancelSceneStability (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/SCENARIO/${id}/stability/cancel`);
  }

  cancelScenePerf (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/SCENARIO/${id}/perf/cancel`);
  }
}

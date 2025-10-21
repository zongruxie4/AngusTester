import { http } from '@xcan-angus/infra';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/indicator';
  }

  /**
   * Update performance indicator configuration
   * @param params - Performance indicator parameters
   * @returns Promise with error and response data
   */
  updatePerformanceIndicator (params: {targetId: string; targetType: string; threads: string; tps: string; errorRate: string; duration: {value: string; unit: string}}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/perf`, params);
  }

  /**
   * Update stability indicator configuration
   * @param params - Stability indicator parameters
   * @returns Promise with error and response data
   */
  updateStabilityIndicator (params: {targetId: string; targetType: string; threads: string; tps: string; errorRate: string; duration: {value: string; unit: string}}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/stability`, params);
  }

  /**
   * Update functionality indicator configuration
   * @param params - Functionality indicator parameters
   * @returns Promise with error and response data
   */
  updateFunctionalityIndicator (params: {targetId: string; targetType: string; }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/func`, params);
  }

  /**
   * Load performance indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  loadPerformanceIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/perf/detailOrDefault`);
  }

  /**
   * Load stability indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  loadStabilityIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/stability/detailOrDefault`);
  }

  /**
   * Load functionality indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  loadFunctionalityIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/func/detailOrDefault`);
  }

  getDefaultPerformance (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/perf/detailOrDefault`);
  }

  getDefaultStability (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/stability/detailOrDefault`);
  }

  /**
   * Delete performance indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  deletePerformanceIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/perf`);
  }

  /**
   * Delete stability indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  deleteStabilityIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/stability`);
  }

  /**
   * Delete functionality indicator configuration
   * @param targetType - Type of target (API, SERVICE, SCENARIO)
   * @param targetId - ID of the target
   * @returns Promise with error and response data
   */
  deleteFunctionalityIndicator (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/func`);
  }
}

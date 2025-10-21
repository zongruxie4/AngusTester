// Infrastructure imports
import { TESTER, http } from '@xcan-angus/infra';

// TODO 删除重复API文件

let baseUrl = '';

/**
 * API class for indicator management operations
 */
class IndicatorAPI {
  constructor () {
    baseUrl = TESTER + '/indicator';
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

// Export indicator API instance with legacy aliases for backward compatibility
export const indicator = new IndicatorAPI();

// Legacy method aliases for backward compatibility
export const indicatorLegacy = {
  modifyPerf: indicator.updatePerformanceIndicator,
  modifyStability: indicator.updateStabilityIndicator,
  modifyFunc: indicator.updateFunctionalityIndicator,
  loadPerf: indicator.loadPerformanceIndicator,
  loadStaibility: indicator.loadStabilityIndicator,
  loadFunc: indicator.loadFunctionalityIndicator,
  delPerf: indicator.deletePerformanceIndicator,
  delStaibility: indicator.deleteStabilityIndicator,
  delFunc: indicator.deleteFunctionalityIndicator
};

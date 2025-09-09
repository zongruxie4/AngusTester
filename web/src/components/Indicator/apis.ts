import { TESTER, http } from '@xcan-angus/infra';

let baseUrl = '';
class API {
  constructor () {
    baseUrl = TESTER + '/indicator';
  }

  // 修改性能指标
  modifyPerf (params: {targetId: string; targetType: string; threads: string; tps: string; errorRate: string; duration: {value: string; unit: string}}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/perf`, params);
  }

  // 修改稳定性指标
  modifyStability (params: {targetId: string; targetType: string; threads: string; tps: string; errorRate: string; duration: {value: string; unit: string}}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/stability`, params);
  }

  // 修改功能指标
  modifyFunc (params: {targetId: string; targetType: string; }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/func`, params);
  }

  // 获取性能指标
  loadPerf (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/perf/detailOrDefault`);
  }

  // 获取稳定性指标
  loadStaibility (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/stability/detailOrDefault`);
  }

  // 获取功能能指标
  loadFunc (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/func/detailOrDefault`);
  }

  // 删除性能指标
  delPerf (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/perf`);
  }

  // 删除稳定性指标
  delStaibility (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/stability`);
  }

  // 删除功能指标
  delFunc (targetType: 'API'|'SERVICE'|'SCENARIO', targetId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetType}/${targetId}/func`);
  }
}

export const indicator = new API();

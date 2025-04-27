import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/indicator';
  }

  // 稳定性指标 list
  loadStabilityList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/stability/search`, params);
  }

  // 性能指标 list
  loadPerfList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/perf/search`, params);
  }

  // 添加稳定性指标
  addStability (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/stability`, params);
  }

  // 添加性能指标
  addPerf (params = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/perf`, params);
  }

  // 更新稳定性指标
  updateStability (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/stability`, params);
  }

  // 更新性能指标
  updatePerf (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/perf`, params);
  }

  // 删除稳定性指标
  delStability (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/stability`, { ids });
  }

  // 删除性能指标
  delPerf (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/perf`, { ids });
  }

  // 获取性能指标详情
  getPerfDetail (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/perf`);
  }

  // 获取目稳定性指标详情
  getStabilityDetail (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/stability`);
  }

  // <- 指标申请列表 ->
  // 申请稳定性指标的 list
  getStabilityApplyList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/stability/audit/search`, params);
  }

  // 申请性能指标的 list
  getPerfApplyList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/perf/audit/search`, params);
  }

  // 提交修改接口稳定性指标
  modifyApiStability (params = { id: '' }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/API/${params.id}/stability/apply`, params);
  }

  // 提交修改接口性能指标
  modifyApiPerf (params = { id: '' }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/API/${params.id}/perf/apply`, params);
  }

  // 提交修改项目稳定性指标
  modifyProStability (params = { id: '', targetType: '' }): Promise<[Error | null, any]> {
    const { id, targetType, ...param } = params;
    return http.post(`${baseUrl}/${targetType}/${id}/stability/apply`, param);
  }

  // 提交修改项目性能指标
  modifyProPerf (params = { id: '', targetType: '' }): Promise<[Error | null, any]> {
    const { id, targetType, ...param } = params;
    return http.post(`${baseUrl}/${targetType}/${id}/perf/apply`, param);
  }

  // 提交修改场景稳定性指标
  modifySceneStability (params = { id: '' }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/SCENARIO/${params.id}/stability/apply`, params);
  }

  // 提交修改场景性能指标
  modifyScenePerf (params = { id: '' }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/SCENARIO/${params.id}/perf/apply`, params);
  }

  // 获取单个接口性能指标
  loadApiPerf (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/API/${id}/perf/audit`);
  }

  // 获取单个接口稳定性指标
  loadApiStability (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/API/${id}/stability/audit`);
  }

  // 获取单个目标性能指标
  loadPerf (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/perf/detailOrDefault`);
  }

  // 获取单个目标稳定性指标
  loadStability (id: string, targetType?: 'API' | 'PROJECT' | 'SERVICE' | 'SCENARIO'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${id}/stability/detailOrDefault`);
  }

  // 获取单个场景性能指标
  loadScenePerf (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/SCENARIO/${id}/perf/audit`);
  }

  // 获取单个场景稳定性指标
  loadSceneStability (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/SCENARIO/${id}/stability/audit`);
  }

  // 取消修改接口稳定性指标
  cancelApiStability (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/API/${id}/stability/cancel`);
  }

  // 取消修改接口性能指标
  cancelApiPerf (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/API/${id}/perf/cancel`);
  }

  // 取消修改项目稳定性指标
  cancelProStability (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${targetType}/${id}/stability/cancel`);
  }

  // 取消修改项目性能指标
  cancelProPerf (id: string, targetType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${targetType}/${id}/perf/cancel`);
  }

  // 取消修改场景稳定性指标
  cancelSceneStability (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/SCENARIO/${id}/stability/cancel`);
  }

  // 取消修改场景性能指标
  cancelScenePerf (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/SCENARIO/${id}/perf/cancel`);
  }

  // 获取接口稳定性审批
  getStabilityApprovel (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/stability/audit/${id}`);
  }

  // 获取接口性能审批
  getPerfApprovel (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/perf/audit/${id}`);
  }

  // 审核接口稳定性指标
  approvelStability (params = { id: '', status: '', remark: '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/stability/audit`, params);
  }

  // 审核接口性能指标
  approvelPerf (params = { id: '', status: '', remark: '' }): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/perf/audit`, params);
  }

  editPerformance (targetType:string, targetId:string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${targetType}/${targetId}/perf/apply`, params);
  }

  loadStabilityIndex (targetType:string, targetId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetType}/${targetId}/stability/audit`);
  }

  editStabilityIndex (targetType:string, targetId:string, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${targetType}/${targetId}/stability/apply`, params);
  }
}

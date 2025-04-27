import { http } from '@xcan-angus/tools';
import { handleHeaders } from '@/utils/index';

let baseUrl: string;
export default class AuthPolicy {
  constructor (prefix: string) {
    baseUrl = prefix + '/auth';
  }

  // 查询 授权策略
  getAuthPolicy (params): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/policy/search`, params);
  }

  // 查询用户策略
  getUserPolicy (userId:string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/user/${userId}/policy`);
  }

  // 查询组策略
  getGroupPolicy<T> (groupId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/group/${groupId}/policy`, params);
  }

  // 用户添加策略
  createUserPolicy (userId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/user/${userId}/policy`, policyIds);
  }

  // 用户删除策略
  deleteUserPolicy (userId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/user/${userId}/policy`, { policyIds: policyIds });
  }

  // 查询用户已授权的策略
  getUserPolicyList<T> (userId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/user/${userId}/policy/associated`, params);
  }

  // 组添加策略
  createGroupPolicy (groupId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/group/${groupId}/policy`, policyIds);
  }

  // 组删除策略
  deleteGroupPolicy (groupId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/group/${groupId}/policy`, { policyIds: policyIds });
  }

  // 查询组已授权的策略
  getPolicyGroup<T> (groupId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/group/${groupId}/policy/associated`, params);
  }

  loadDetailById (id: string, tenantId: string): Promise<[Error|null, any]> { // 查询策略详情
    return http.get(`${baseUrl}/policy/${id}`, {}, handleHeaders(tenantId));
  }

  addPolicy<T> (params:{dtos:T}): Promise<[Error|null, any]> { // 添加策略
    return http.post(`${baseUrl}/policy`, params.dtos);
  }

  patchPolicy<T> (params:{dtos:T}): Promise<[Error|null, any]> { // 修改策略
    return http.patch(`${baseUrl}/policy`, params.dtos);
  }

  putPolicyFunc (params, policyId: string): Promise<[Error|null, any]> { // 权限策略 - 分配菜单
    return http.put(`${baseUrl}/policy/${policyId}/func`, params.appFuncIds);
  }

  deletePolicy (ids: string[]): Promise<[Error|null, any]> { // 删除策略
    return http.del(`${baseUrl}/policy`, { ids });
  }

  toggleEnable<T> (params:T, tenantId: string): Promise<[Error|null, any]> { // 启用、禁用策略
    return http.patch(`${baseUrl}/policy/enabled`, [params], handleHeaders(tenantId));
  }

  // 策略资源
  loadPolicyResource (policyId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/policy/${policyId}/func/tree`);
  }

  initGrant<T> (params:T, tenantId: string): Promise<[Error|null, any]> { // 授权初始化
    return http.post(`${baseUrl}/policy/init`, params, handleHeaders(tenantId));
  }

  addPolicyUser (params, tenantId: string): Promise<[Error|null, any]> { // 策略授权给用户
    return http.post(`${baseUrl}/${params.id}/user`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyUsers (params, tenantId: string): Promise<[Error|null, any]> { // 根据策略ID查询策略下的用户
    return http.get(`${baseUrl}/${params.id}/user`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyUsers (params, tenantId: string): Promise<[Error|null, any]> { // 删除策略下的用户
    return http.del(`${baseUrl}/${params.id}/user`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  addPolicyGroup (params, tenantId: string): Promise<[Error|null, any]> { // 策略授权给组
    return http.post(`${baseUrl}/${params.id}/group`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyGroups (params, tenantId: string): Promise<[Error|null, any]> { // 根据策略ID查询策略下的组
    return http.get(`${baseUrl}/${params.id}/group`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyGroups (params, tenantId: string): Promise<[Error|null, any]> { // 删除策略下的组
    return http.del(`${baseUrl}/${params.id}/group`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  addPolicyDept (params, tenantId: string): Promise<[Error|null, any]> { // 策略授权给部门
    return http.post(`${baseUrl}/${params.id}/dept`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyDepts (params, tenantId: string): Promise<[Error|null, any]> { // 根据策略ID查询策略下的部门
    return http.get(`${baseUrl}/${params.id}/dept`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyDepts (params, tenantId: string): Promise<[Error|null, any]> { // 删除策略下的部门
    return http.del(`${baseUrl}/${params.id}/dept`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  // 部门授权策略
  getDeptPolicy (deptId: string, params): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/dept/${deptId}/policy`, params);
  }

  // 给部门添加策略
  addPolicyByDept (deptId: string, policyIds: string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/dept/${deptId}/policy`, policyIds);
  }

  // 给部门删除策略
  delPolicyByDept (deptId: string, policyIds: string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/dept/${deptId}/policy`, { policyIds });
  }
}

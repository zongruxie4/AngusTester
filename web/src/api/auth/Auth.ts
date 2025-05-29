import { http } from '@xcan-angus/tools';
import { handleHeaders } from '@/utils/index';

let baseUrl: string;
export default class AuthPolicy {
  constructor (prefix: string) {
    baseUrl = prefix + '/auth';
  }

  getAuthPolicy (params): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/policy/search`, params);
  }

  getUserPolicy (userId:string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/user/${userId}/policy`);
  }

  getGroupPolicy<T> (groupId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/group/${groupId}/policy`, params);
  }

  createUserPolicy (userId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/user/${userId}/policy`, policyIds);
  }

  deleteUserPolicy (userId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/user/${userId}/policy`, { policyIds: policyIds });
  }

  getUserPolicyList<T> (userId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/user/${userId}/policy/associated`, params);
  }

  createGroupPolicy (groupId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/group/${groupId}/policy`, policyIds);
  }

  deleteGroupPolicy (groupId:string, policyIds:string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/group/${groupId}/policy`, { policyIds: policyIds });
  }

  getPolicyGroup<T> (groupId:string, params?:T): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/group/${groupId}/policy/associated`, params);
  }

  loadDetailById (id: string, tenantId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/policy/${id}`, {}, handleHeaders(tenantId));
  }

  addPolicy<T> (params:{dtos:T}): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/policy`, params.dtos);
  }

  patchPolicy<T> (params:{dtos:T}): Promise<[Error|null, any]> {
    return http.patch(`${baseUrl}/policy`, params.dtos);
  }

  putPolicyFunc (params, policyId: string): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/policy/${policyId}/func`, params.appFuncIds);
  }

  deletePolicy (ids: string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/policy`, { ids });
  }

  toggleEnable<T> (params:T, tenantId: string): Promise<[Error|null, any]> {
    return http.patch(`${baseUrl}/policy/enabled`, [params], handleHeaders(tenantId));
  }

  loadPolicyResource (policyId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/policy/${policyId}/func/tree`);
  }

  initGrant<T> (params:T, tenantId: string): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/policy/init`, params, handleHeaders(tenantId));
  }

  addPolicyUser (params, tenantId: string): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/${params.id}/user`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyUsers (params, tenantId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/${params.id}/user`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyUsers (params, tenantId: string): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/${params.id}/user`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  addPolicyGroup (params, tenantId: string): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/${params.id}/group`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyGroups (params, tenantId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/${params.id}/group`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyGroups (params, tenantId: string): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/${params.id}/group`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  addPolicyDept (params, tenantId: string): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/${params.id}/dept`, params.dtos, handleHeaders(tenantId));
  }

  loadPolicyDepts (params, tenantId: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/${params.id}/dept`, params.dtos, handleHeaders(tenantId));
  }

  deletePolicyDepts (params, tenantId: string): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/${params.id}/dept`, { ids: params.ids }, handleHeaders(tenantId, true));
  }

  getDeptPolicy (deptId: string, params): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/dept/${deptId}/policy`, params);
  }

  addPolicyByDept (deptId: string, policyIds: string[]): Promise<[Error|null, any]> {
    return http.post(`${baseUrl}/dept/${deptId}/policy`, policyIds);
  }

  delPolicyByDept (deptId: string, policyIds: string[]): Promise<[Error|null, any]> {
    return http.del(`${baseUrl}/dept/${deptId}/policy`, { policyIds });
  }
}

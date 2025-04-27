import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/auth/policy';
  }

  getAuthPolicy (params): Promise<[Error | null, any]> {
    return http.get(baseUrl, params);
  }

  getFuncsByPolicy (policyId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${policyId}/func/tree`);
  }

  delAuthPolicy (params): Promise<[Error | null, any]> {
    return http.del(baseUrl, params, { paramsType: true });
  }

  enableStrategy (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  getStrategyDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  // 添加部门的授权策略
  addDeptAuthorize (deptId, deptIds:string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/dept/${deptId}`, deptIds);
  }

  // 添加用户的授权策略
  addUserAuthorize (userId:string, policyIds :string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/user/${userId}`, policyIds);
  }

  // 添加组的授权策略
  addGroupAuthorize (groupId, policyIds:string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/group/${groupId}`, policyIds);
  }

  // 获取应用下全部授权策略
  // loadAuthPolicyList (params:{pageNo:number, pageSize:number, appId:string}): Promise<[error | null, any]> {
  //   return http.get(`${baseUrl}`, params);
  // }

  // 获取用户的全部授权策略
  loadUserAuthorizedList (appId:string, userId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/${userId}`, { appId });
  }

  // 获取部门的全部授权策略
  loadDeptAuthorizedList (appId:string, deptId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/dept/${deptId}`, { appId });
  }

  loadDeptPolicy (policyId: string, param): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${policyId}/dept`, param);
  }

  delDeptPolicy (deptId, ids): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${deptId}/dept`, ids, { paramsType: true });
  }

  addPolicyDept (policyId, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${policyId}/dept`, params);
  }

  // 获取组的全部授权策略
  loadGroupAuthorizedList (appId:string, groupId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/group/${groupId}`, { appId });
  }

  loadGroupPolicy (policyId: string, param): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${policyId}/group`, param);
  }

  delGroupPolicy (groupId, ids): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${groupId}/group`, ids, { paramsType: true });
  }

  addGroups (policyId, groupIds): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${policyId}/group`, groupIds);
  }

  getPolicyUser (policyId, params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${policyId}/user`, params);
  }

  delPolicyUser (policyId, params): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${policyId}/user`, params, { paramsType: true });
  }

  addPolicyUser (policyId, params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${policyId}/user`, params);
  }
}

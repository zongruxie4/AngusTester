import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/app';
  }

  getAppList (params = {}): Promise<[Error | null, any]> {
    return http.get(baseUrl, params);
  }

  patchApp (params = {}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, [params]);
  }

  addAuthUser (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/user/policy/auth`, params);
  }

  addAuthDept (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/dept/policy/auth`, params);
  }

  addAuthGroup (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/group/policy/auth`, params);
  }

  getAuthAccount (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/account/current`);
  }

  getAuthUserList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/user`, params);
  }

  getAuthDeptList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/dept`, params);
  }

  getAuthGroupList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/group`, params);
  }

  deleteAuthUser (appId:string, userIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/user/auth`, { userIds: userIds });
  }

  deleteAuthDept (appId:string, deptIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/dept/auth`, { deptIds: deptIds });
  }

  deleteAuthGroup (appId:string, groupIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/group/auth`, { groupIds: groupIds });
  }
}

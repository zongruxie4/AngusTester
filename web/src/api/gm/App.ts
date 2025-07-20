import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/app';
  }

  loadAppInfo (params = {}): Promise<[Error | null, any]> {
    return http.get(baseUrl, params);
  }

  patchApp (params = {}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, [params]);
  }

  loadAppAuth (id:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/auth/account/current`);
  }

  loadDeptList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/dept`, params);
  }

  loadGroupList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/group`, params);
  }

  loadUserList<T> (appId:string, params:T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${appId}/auth/user`, params);
  }

  deleteUser (appId:string, userIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/user/auth`, { userIds: userIds });
  }

  deleteDept (appId:string, deptIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/dept/auth`, { deptIds: deptIds });
  }

  deleteGroup (appId:string, groupIds:string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${appId}/group/auth`, { groupIds: groupIds });
  }

  addDepts (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/dept/policy/auth`, params);
  }

  addUsers (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/user/policy/auth`, params);
  }

  addGroups (appId:string, params:{orgIds:string[], policyIds:string[]}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${appId}/group/policy/auth`, params);
  }
}

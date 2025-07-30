import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/project';
  }

  addProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  putProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.put(baseUrl, params);
  }

  updateProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  deleteProject (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  getProjectDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  getProjectList (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getJoinedProject (userId: string, params: {[key: string]: any} = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/${userId}/joined`, params);
  }

  getProjectMember (projectId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${projectId}/member/user`);
  }

  getTrashList (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash`, { ...params, infoScope: 'DETAIL', fullTextSearch: true });
  }

  getTrashCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/count`);
  }

  deleteAllTrash (params = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  deleteTrash (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${id}`);
  }

  backAllTrash (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  backTrash (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${id}/back`);
  }
}

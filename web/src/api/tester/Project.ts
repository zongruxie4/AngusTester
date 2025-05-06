import {http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/project';
  }

  loadProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  loadMyProject (params: {[key: string]: any}, userId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/${userId}/joined`, params);
  }

  addProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  replaceProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.put(baseUrl, params);
  }

  updateProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  getProjectDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  delProject (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  searchProject (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/search`, params);
  }

  searchTrash (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/search?infoScope=DETAIL`, params);
  }

  getTrashCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/count`);
  }

  delAllTrash (params = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash`, params);
  }

  delTrash (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${id}`);
  }

  backAllTrash (params = {}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/back`, params, { paramsType: true });
  }

  backTrash (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${id}/back`);
  }

  getMemberUser (projectId: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${projectId}/member/user`);
  }
}

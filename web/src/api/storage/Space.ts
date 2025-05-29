import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/space';
  }

  getSpaceList (params: {[key: string]: any}, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params, axiosConfig);
  }

  addSpace (params: {name: string, quotaSize: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  patch (params: {id: string, name?: string, quotaSize?: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  loadDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  delete (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids: [id] });
  }

  putAuth (authId: string, params: {permissions: string[]}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  updateAuthFlag (spaceId: string, enabled: boolean): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${spaceId}/auth/enabled?enabled=${enabled}`);
  }

  loadAuthority (params, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  addAuth (spaceId: string, param): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/auth`, param);
  }

  delAuth (params): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${params.id}`);
  }

  getFiles (params: {spaceId: string, filters?: Record<string, any>[], pageSize: number, pageNo: number, parentDirectoryId?: string, orderSort?: string, orderBy?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/search`, params);
  }

  addDirectory (params: {name: string, spaceId: string, parentDirectoryId?: number | string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/object/directory`, params);
  }

  fileRename (params: {id: string, name: string}): Promise<[Error | null, any]> {
    const { id, name } = params;
    return http.put(`${baseUrl}/object/${id}/rename?name=${name}`);
  }

  delFile (ids: (string|number)[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/object`, { ids });
  }

  getFileDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}`);
  }

  getPathChain (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}/navigation`);
  }

  moveFile (params: {objectIds: string[], targetDirectoryId?: string, targetSpaceId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/object/move`, params);
  }

  getAuth ({ id = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin: true });
  }

  getListAuth (params: {ids: string[], admin: boolean }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current`, params);
  }

  getFileUrl (params: {objectId: string; }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share/quick`, params);
  }

  getShareUrl (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  patchShare (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  loadSharedList (params: {spaceId: string, pageNo?: string | number, pageSize?: string | number, remark?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/search`, params);
  }

  delShare (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids });
  }

  loadShareInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/space';
  }

  addSpace (params: {name: string, quotaSize: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  patchSpace (params: {id: string, name?: string, quotaSize?: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteSpace (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids: [id] });
  }

  getSpaceDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  getSpaceList (params: {[key: string]: any}, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true }, axiosConfig);
  }

  getFileList (params: {spaceId: string, filters?: Record<string, any>[], pageSize: number, pageNo: number, parentDirectoryId?: string, orderSort?: string, orderBy?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object`, params);
  }

  addDirectory (params: {name: string, spaceId: string, parentDirectoryId?: number | string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/object/directory`, params);
  }

  renameFile (params: {id: string, name: string}): Promise<[Error | null, any]> {
    const { id, name } = params;
    return http.put(`${baseUrl}/object/${id}/rename?name=${name}`);
  }

  deleteFile (ids: (string|number)[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/object`, { ids });
  }

  getFileDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}`);
  }

  getFileNavigation (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}/navigation`);
  }

  moveFile (params: {objectIds: string[], targetDirectoryId?: string, targetSpaceId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/object/move`, params);
  }

  addSpaceAuth (spaceId: string, param): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${spaceId}/auth`, param);
  }

  putSpaceAuth (authId: string, params: {permissions: string[]}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${authId}`, params);
  }

  enabledSpaceAuth (spaceId: string, enabled: boolean): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${spaceId}/auth/enabled?enabled=${enabled}`);
  }

  getSpaceAuthList (params, axiosConfig = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/auth`, params, axiosConfig);
  }

  deleteSpaceAuth (params): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${params.id}`);
  }

  getSpaceCurrentAuth ({ id = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { admin: true });
  }

  getSpaceCurrentAuthList (params: {ids: string[], admin: boolean }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current`, params);
  }

  getQuickShareUrl (params: {objectId: string; }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share/quick`, params);
  }

  getShareUrl (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  patchShare (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  getSharedList (params: {spaceId: string, pageNo?: string | number, pageSize?: string | number, remark?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share`, { ...params, fullTextSearch: true });
  }

  deleteShare (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids });
  }

  getShareDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }
}

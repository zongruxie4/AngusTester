import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/space';
  }

  // 空间 list
  getSpaceList (params: {[key: string]: any}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 添加空间
  addSpace (params: {name: string, quotaSize: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 更新空间信息
  patch (params: {id: string, name?: string, quotaSize?: number, remark?: string, bizKey?: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  // 获取空间信息详情
  loadDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  // 删除
  delete (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids: [id] });
  }

  // 更新权限
  updateAuth (params: {id: string, permissions: string[]}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/auth/${params.id}`, { permissions: params.permissions });
  }

  // 更新 AuthFlag
  updateAuthFlag (params: {id: string, enabled: boolean}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params.id}/auth/enabled?enabled=${params.enabled}`);
  }

  // 获取权限数据
  loadAuthority (params): Promise<[Error | null, any]> {
    params.spaceId = params.id;
    return http.get(`${baseUrl}/auth`, params);
  }

  // 添加权限
  addAuth (params): Promise<[Error | null, any]> {
    const { id, ...param } = params;
    return http.post(`${baseUrl}/${id}/auth`, param);
  }

  // 删除权限
  delAuth (params): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/auth/${params.id}`);
  }

  // 获取空间下文件
  getFiles (params: {spaceId: string, filters?: Record<string, any>[], pageSize: number, pageNo: number, parentDirectoryId?: string, orderSort?: string, orderBy?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/search`, params);
  }

  // 添加文件夹
  addDirectory (params: {name: string, spaceId: string, parentDirectoryId?: number | string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/object/directory`, params);
  }

  // 更新文件/夹 name
  fileRename (params: {id: string, name: string}): Promise<[Error | null, any]> {
    const { id, name } = params;
    return http.put(`${baseUrl}/object/${id}/rename?name=${name}`);
  }

  // 删除文件/夹
  delFile (ids: (string|number)[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/object`, { ids });
  }

  // 文件/夹详情
  getFileDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}`);
  }

  // 文件/夹 路径
  getPathChain (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/object/${id}/navigation`);
  }

  // 移动文件/夹
  moveFile (params: {objectIds: string[], targetDirectoryId?: string, targetSpaceId: string}): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/object/move`, params);
  }

  // 获取单个空间权限
  getAuth ({ id = '' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}/user/auth/current`, { adminFlag: true });
  }

  // 获取多个空间权限
  getListAuth (params: {ids: string[], adminFlag: boolean }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/auth/current`, params);
  }

  // 获取文件链接
  getFileUrl (params: {objectId: string; }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share/quick`, params);
  }

  // 获取分享链接
  getShareUrl (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/share`, params);
  }

  // 更新分享内容
  patchShare (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/share`, params);
  }

  // 获取空间分享list
  loadSharedList (params: {spaceId: string, pageNo?: string | number, pageSize?: string | number, remark?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/search`, params);
  }

  // 删除分享链接
  delShare (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/share`, { ids });
  }

  // 获取分享信息
  loadShareInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/${id}`);
  }
}

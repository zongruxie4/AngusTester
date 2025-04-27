import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/space';
  }

  // 获取空间信息
  getSpaceInfo (params: {spt: string, sid: string, passd?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share`, params);
  }

  // 获取空间下文件列表
  getFileList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/object/search`, params);
  }

  // 获取文件信息
  getFileInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/object/${id}`);
  }
}

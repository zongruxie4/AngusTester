import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/space';
  }

  getSpaceInfo (params: {spt: string, sid: string, password?: string}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share`, params);
  }

  getFileList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/object`, params);
  }

  getFileInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/object/${id}`);
  }
}

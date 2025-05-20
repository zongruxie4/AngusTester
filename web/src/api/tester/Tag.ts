import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/tag';
  }

  search (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  updateTag (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteTag (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  addTag (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }
}

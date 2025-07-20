import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/module';
  }

  searchTree<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tree`, { ...params, fullTextSearch: true });
  }

  addModule <T> (params: T): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateModule <T> (params: T): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  delModule <T> (params: T) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, params);
  }
}

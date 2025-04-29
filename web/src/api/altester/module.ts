import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/module';
  }

  searchTree<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tree/search`, params);
  }

  // addModule <T> (params: T): Promise<[Error | null, any]> {
  //   return http.post(`${baseUrl}`, params);
  // }

  updateModule <T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, params);
  }
}

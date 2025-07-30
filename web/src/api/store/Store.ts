import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/store';
  }

  getStoreList <T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/cloud/goods`, { ...params, fullTextSearch: true });
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class Group {
  constructor (prefix: string) {
    baseUrl = prefix + '/group';
  }

  getGroupList<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, params);
  }
}

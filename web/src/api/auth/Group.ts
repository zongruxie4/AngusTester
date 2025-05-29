import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class Group {
  constructor (prefix: string) {
    baseUrl = prefix + '/group';
  }

  searchList<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }
}

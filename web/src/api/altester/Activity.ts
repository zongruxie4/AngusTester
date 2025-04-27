import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/activity';
  }

  loadActivities (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, params);
  }
}

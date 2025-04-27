import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/case';
  }

  // TODO Q3 API未提取出来

  loadFuncCase (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  loadFavourite (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/favourite/search`, params);
  }

  loadFollow (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/follow/search`, params);
  }
}

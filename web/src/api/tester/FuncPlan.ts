import {http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/plan';
  }

  // loadFuncCase (params): Promise<[Error | null, any]> {
  //   return http.get(`${baseUrl}/search`, params);
  // }
  //
  // loadFavourite (params): Promise<[Error | null, any]> {
  //   return http.get(`${baseUrl}/favourite/search`, params);
  // }
  //
  // loadFollow (params): Promise<[Error | null, any]> {
  //   return http.get(`${baseUrl}/follow/search`, params);
  // }

  getPlanInfo (planId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${planId}`);
  }
}

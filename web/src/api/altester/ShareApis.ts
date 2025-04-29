import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/apis';
  }

  // 获取分享信息
  getApiShareInfo<T> (params: T): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/view`, params);
  }

  // 获取项目下 apis
  getApisList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/apis/search`, params);
  }

  // 获取api 信息
  getApiInfo (params: {id: string, passd?: string, sid: string, spt: string}): Promise<[Error | null, any]> {
    const { id, ...param } = params;
    return http.get(`${baseUrl}/share/apis/${id}`, param);
  }

  // 获取所有的 funcs(mock 函数) @todo
  getFuncs (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/share/function/template`);
  }
}

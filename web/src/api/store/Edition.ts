import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/edition';
  }

  // 获取系统信息
  getSysEdition (goodsCode = 'AngusTester'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/installed?goodsCode=${goodsCode}`);
  }
}

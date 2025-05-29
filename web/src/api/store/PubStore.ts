import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/store';
  }

  getGoodsVersion (goodsCode = 'AngusTester'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/goods/${goodsCode}/online`);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/store';
  }

  // 获取节点 订单 id
  getGoodsVersion (goodsCode = 'AngusLoad'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/goods/${goodsCode}/online`);
  }
}

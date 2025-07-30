import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/edition';
  }

  getSysEdition (goodsCode = 'AngusTester'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/installed?goodsCode=${goodsCode}`);
  }
}

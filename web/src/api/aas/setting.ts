import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class Setting {
  constructor (prefix: string) {
    baseUrl = prefix + '/setting';
  }

  getApiProxy () : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/user/apis/proxy`);
  }
}

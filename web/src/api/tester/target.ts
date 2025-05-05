import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/target';
  }

  getParamsVariableValue (apiId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${apiId}/API/parameter/data/value`);
  }

  delVariable (targetId: string, targetType: string, ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${targetId}/${targetType}/variable`, ids, { dataType: true });
  }
}

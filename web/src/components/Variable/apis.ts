import { http, TESTER } from '@xcan-angus/infra';

let baseUrl: string;

class API {
  constructor () {
    baseUrl = TESTER + '/variable';
  }

  // 删除变量
  delVariables (params: { ids: string[] }): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, params);
  }

  // 增加变量
  addVariables (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 替换变量
  putVariables (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  // 修改变量状态
  patchVariableEnabled (params: { id: string, enabled: boolean }[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }
}

const varaibleApis = new API();

export default varaibleApis;

import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/variable';
  }

  // 获取变量列表
  loadVariablesList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 获取继承变量
  loadExtendsList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/inherited`, params);
  }

  // 增加变量
  addVariables (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  // 修改变量
  patchVariables (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  // 修改变量状态
  patchVariableEnabled (params: { id: string, enabled: boolean }[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  // 删除变量
  delVariables (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  // 替换变量
  putVariables (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  // 获取所有变量
  getVariables (params: { names?: string[], targetId: string, targetType: 'API' | 'DIR' | 'PROJECT' | 'SCENARIO' | 'SERVICE' | 'TASK' | 'VARIABLE' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/value/all`, params);
  }

  // 获取变量详情
  getVariableInfo (id:string) {
    return http.get(`${baseUrl}/${id}`);
  }

  // 获取变量替换值
  getVariableValue (names: string[], targetId?: string, targetType = 'API'): Promise<[Error | null, any]> {
    const params = {
      names,
      targetType,
      targetId
    };
    if (!targetId) {
      delete params.targetId;
    }
    return http.get(`${baseUrl}/value/all`, params);
  }

  //
  importVariable (formData: FormData): Promise<[Error | null, any]>{
    return http.post(`${baseUrl}/import`, formData, { silence: true });
  }

  getTargetDetail (targetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetId}/target`);
  }

  cloneVariable (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, params);
  }

  previewValue (params, axiosConf): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/value/preview`, params, axiosConf);
  }

}

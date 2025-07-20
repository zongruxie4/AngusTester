import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/variable';
  }

  loadVariablesList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  loadExtendsList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/inherited`, params);
  }

  addVariables (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  patchVariables (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  patchVariableEnabled (params: { id: string, enabled: boolean }[]): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  delVariables (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  putVariables (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  getVariables (params: { names?: string[], targetId: string, targetType: 'API' | 'DIR' | 'PROJECT' | 'SCENARIO' | 'SERVICE' | 'TASK' | 'VARIABLE' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/value/all`, params);
  }

  getVariableInfo (id:string) {
    return http.get(`${baseUrl}/${id}`);
  }

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

  importVariable (formData: FormData): Promise<[Error | null, any]> {
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

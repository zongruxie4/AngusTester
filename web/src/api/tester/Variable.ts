import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/variable';
  }

  addVariables (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  putVariables (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  patchVariables (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteVariables (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  importVariables (formData: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData, { silence: true });
  }

  cloneVariable (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, params);
  }

  previewVariableValue (params, axiosConf): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/value/preview`, params, axiosConf);
  }

  getVariableDetail (id:string) {
    return http.get(`${baseUrl}/${id}`);
  }

  getVariablesList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getVariableTargetDetail (targetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${targetId}/target`);
  }

  getVariableValue (params: { names?: string[], targetId: string, targetType: 'API' | 'DIR' | 'PROJECT' | 'SCENARIO' | 'SERVICE' | 'TASK' | 'VARIABLE' }): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/value/all`, params);
  }
}

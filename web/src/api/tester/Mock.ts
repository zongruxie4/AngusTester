import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/mock';
  }

  getAllFunction (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/functions`);
  }

  generateFunctionValue (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/text/data/batch`, params);
  }

  syncServiceInstanceConfig (id:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/service/${id}/instance/sync`);
  }

  syncApiInstanceConfig (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/apis/${id}/instance/sync`);
  }

  addService (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service`, params);
  }

  addServiceByAssoc (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service/association/services`, params);
  }

  addServiceFromFile (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service/file/import`, params);
  }

  patchService (params: any): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/service`, params);
  }

  deleteService (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/service`, { ids }, { paramsType: true });
  }

  deleteServiceByForce (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/service?force=true`, { ids }, { paramsType: true });
  }

  startService (ids: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service/start`, ids);
  }

  stopService (ids: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service/stop`, ids);
  }

  getServiceDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${id}`);
  }

  getServiceList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service`, { ...params, fullTextSearch: true });
  }

  getServiceApiIds (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${id}/association/apis/id`);
  }

  assocMockService (mockServiceId: string, projectId:string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/service/association/${mockServiceId}/${projectId}`);
  }

  cancelMockServiceAssoc (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/service/${id}/association`);
  }

  assocMockApi (mockApiId: string, apisId: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/apis/association/${mockApiId}/${apisId}`);
  }

  cancelMockApiAssoc (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/apis/association?`, { ids });
  }

  assocApiToMock (mockServiceId: string, apisId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/apis/association/${mockServiceId}/${apisId}`);
  }

  copyApiToMock (mockServiceId: string, apisId: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/apis/copy/${apisId}/${mockServiceId}`);
  }

  addMockApi (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/apis`, params);
  }

  updateMockApi (params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/apis`, params);
  }

  deleteMockApi (params: any): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/apis`, params, { paramsType: true });
  }

  cloneMockApi (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/apis/${id}/clone`);
  }

  getMockApiDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/${id}`);
  }

  getMockApiList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis`, { ...params, fullTextSearch: true });
  }

  addMockApiResponse (id: string, params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/apis/${id}/response`, params);
  }

  updateMockApiResponse (id: string, params: any): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/apis/${id}/response`, params);
  }

  getMockApiResponse (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/${id}/response`);
  }

  getMockApiLogDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/apis/log/${id}`);
  }

  loadMockApiLogList (serviceId: string, params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/service/${serviceId}/apis/log`, { ...params, fullTextSearch: true });
  }

  importDemoMockApi (mockServiceId:string) {
    return http.post(`${baseUrl}/service/${mockServiceId}/example/apis/import`);
  }

  mockData (params: {configuration: Record<string, any>; mockData: Record<string, any>}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/data/script`, params);
  }

  importService (formData: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/service/import`, formData);
  }

  execMockData (params: {configuration: Record<string, any>; mockData: Record<string, any>, plugin: string, scriptId: string, projectId: string}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/data/execution`, params);
  }

  getGenerateScriptContent (params: Record<string, any>): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/data/script/content`, params);
  }

}

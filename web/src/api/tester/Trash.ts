import { http } from '@xcan-angus/tools';

let baseUrl = '';
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/trash';
  }

  loadCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/count`);
  }

  loadAllocateList (params:any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  del (params: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${params}`, {});
  }

  delAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`);
  }

  back (params: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${params}/back`);
  }

  backAll (): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/back`);
  }

  loadScenarioCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario/count`);
  }

  loadScenarioDirList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario`, { ...params, targetType: 'DIR', fullTextSearch: true });
  }

  loadScenarioSceneList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/scenario`, { ...params, targetType: 'SCENARIO', fullTextSearch: true });
  }

  deleteScenario (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/scenario/${id}`);
  }

  delScenarioAll (): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/scenario`);
  }

  recoveryScenario (id): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/scenario/${id}/back`);
  }

  recoveryScenarioAll (): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/scenario/back`);
  }

  loadTaskCount (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/task/count`);
  }
}

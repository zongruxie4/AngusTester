import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/dataset';
  }

  loadDataSetList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  del (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  putDataSet (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  addDataSet (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  cloneDataSet (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, params);
  }

  getDataSetInfo (dataSetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${dataSetId}`);
  }

  getTarget (dataSetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${dataSetId}/target`);
  }

  importDataSet (formData: FormData, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData, axiosConf);
  }

  previewValue (params, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/value/preview`, params, axiosConf);
  }
}

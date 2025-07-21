import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/dataset';
  }

  addDataSet (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  putDataSet (params): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}`, params);
  }

  cloneDataSet (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/clone`, params);
  }

  deleteDataSet (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  getDataSetDetail (dataSetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${dataSetId}`);
  }

  getDataSetList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getDataSetTarget (dataSetId: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${dataSetId}/target`);
  }

  importDataSet (formData: FormData, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData, axiosConf);
  }

  previewDataSetValue (params, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/value/preview`, params, axiosConf);
  }
}

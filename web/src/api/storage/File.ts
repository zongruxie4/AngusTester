import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/file';
  }

  compressFile (params: {ids: string[], name?: string, parentDirectoryId?: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/compress`, { ...params, format: 'zip' });
  }

  getMetrics (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/metrics`);
  }
}

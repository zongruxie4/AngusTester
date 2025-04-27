import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/file';
  }

  downFile (params:{filename: string, [key: string]: string}): Promise<[Error | null, any]> {
    const { filename, ...param } = params;
    return http.get(`${baseUrl}/${filename}`, param);
  }

  // 压缩文件
  compressFile (params: {ids: string[], name?: string, parentDirectoryId?: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/compress`, { ...params, format: 'zip' });
  }

  getMetrics (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/metrics`);
  }

  getFileMessage (id): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  modifyFileName (id:string|number, params:{name:string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/rename`, params, { paramsType: true });
  }

  getFileTree (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/mockTree`);
  }

  // loadshares (params:{pageNo:number, pageSize:number, targetId:string, targetType:'FILE' | 'PROJECT' | 'APIS'}): Promise<[error | null, any]> {
  //   return http.get(`comm/api${version}/share`, params);
  // }

  addScript (params): Promise<[Error | null, any]> {
    return http.post('/altester/api/v1/mock/file/script', params);
  }
}

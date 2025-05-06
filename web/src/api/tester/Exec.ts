import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }
  // 执行测试结果
  loadExecTestResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/result`);
  }

  getExecResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/result`);
  }

  getExecInfo (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}`);
  }

  getSampleExtensionContent <T> (execId: string, params: T) : Promise<string[]> {
    return http.get(`${baseUrl}/${execId}/sample/extension/content`, params);
  }

  execByScript (params: {scriptId: string}, axiosConf = {}) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/byscript`, params, axiosConf);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/exec';
  }

  // TODO Q3 API未提取出来

  // 执行测试结果
  loadExecTestResult (execId:string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${execId}/test/result`);
  }
}

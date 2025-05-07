import {http, TESTER} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node';
  }

  loadNodes (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  loadList (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  // 添加节点
  add (params): Promise<[Error | null, any]> {
    return http.post(baseUrl, params);
  }

  // 更新节点
  update (params): Promise<[Error | null, any]> {
    return http.patch(baseUrl, params);
  }

  // 删除节点
  delete (params: any): Promise<[Error | null, any]> {
    return http.del(baseUrl, params);
  }

  // 安装代理
  installAgent (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${params.id}/agent/install`, params);
    // return http.get(`${baseUrl}/agent/installCmd`, { nodeId: params.id });
  }

  // 禁用
  enable (params: Array<any>): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/enabled`, params);
  }

  // 测试链接
  test (params: any): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/test`, params);
  }

  // 详情
  detail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`, {});
  }

  // 重启代理
  restartProxy (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/agent/restart`);
  }

  loadRunnerProcess (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process`, params, { paramsType: true });
  }

  killRunnerProcess (params) : Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/info/runner/process/kill`, params);
  }
}

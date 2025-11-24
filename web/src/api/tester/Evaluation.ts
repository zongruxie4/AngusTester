import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/project/evaluation';
  }

  /**
   * 创建评估
   */
  addEvaluation (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  /**
   * 更新评估
   */
  updateEvaluation (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  /**
   * 获取评估列表
   */
  getEvaluationList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  /**
   * 获取评估详情
   */
  getEvaluationDetail (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  /**
   * 删除评估
   */
  deleteEvaluation (id: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  /**
   * 生成评估结果
   */
  generateResult (id: string): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/result`);
  }
}

import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/project/evaluation';
  }

  addEvaluation (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateEvaluation (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  getEvaluationList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, fullTextSearch: true });
  }

  getEvaluationDetail (id: number): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  deleteEvaluation (id: number): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}`);
  }

  generateResult (id: number): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/result`);
  }
}


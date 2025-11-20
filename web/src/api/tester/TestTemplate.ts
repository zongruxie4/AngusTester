import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/func/test/template';
  }

  addTemplate (params): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, params);
  }

  updateTemplate (params): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}`, params);
  }

  deleteTemplate (templateId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${templateId}`);
  }

  getTemplateList (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`);
  }
}


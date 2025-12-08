import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/template';
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

  importTemplate (formData: FormData): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/import`, formData);
  }

  downloadTemplate (templateId: string, format: 'excel'|'csv'|'json'): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${templateId}/export?format=${format}`, undefined, {
      responseType: 'blob'
    });
  }
}


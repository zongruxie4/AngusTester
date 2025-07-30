import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/node/info';
  }

  geAgentInstallationCmd (params: any): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${params.id}/agent/install/cmd`);
  }
}

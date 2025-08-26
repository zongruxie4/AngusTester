import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/ai';
  }

  getChartResult (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/chat/result`, params);
  }

  /**
   * <p>
   * Loads the AI agent configuration from the GM settings.
   * </p>
   * @returns Promise that resolves to the AI agent data or null if an error occurs
   */
  async getAIAgentSetting(): Promise<any> {
    const [error, res] = await http.get(`${baseUrl}/setting/AI_AGENT`);
    if (error) {
      return null;
    }
    return res?.data?.aiAgent;
  }
}

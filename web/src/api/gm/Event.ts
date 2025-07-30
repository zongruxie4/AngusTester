import { http } from '@xcan-angus/infra';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/event';
  }

  getCurrentTemplateList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/template/current`, { ...params, bigBizKey: 'AngusTester' });
  }

  getRecordList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}`, { ...params, bigBizKey: 'AngusTester' });
  }

  getChannelDetail (channelType:string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/channel/channel/${channelType}`);
  }

  saveChannelSetting (params: {id:string, channelIds:string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/channel`, params);
  }

  getCurrentChannels (id: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/template/${id}/current`);
  }

  putReceiver (params: {id: string, noticeTypes: string[], receiverIds: string[], receiverTypes: string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/receiver`, params);
  }
}

import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/event';
  }

  loadStatistics (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/exec/statistics`);
  }

  loadPushConfigList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/template/current`, { ...params, bigBizKey: 'AngusTester' });
  }

  loadPushRecordList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, { ...params, bigBizKey: 'AngusTester' });
  }

  loadReceiveSettingDetail (channelType:string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/channel/channel/${channelType}`);
  }

  saveReceiveSetting (params: {id:string, channelIds:string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/channel`, params);
  }

  loadCurrentChannels (id: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/template/${id}/current`);
  }

  putReceiver (params: {id: string, noticeTypes: string[], receiverIds: string[], receiverTypes: string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/receiver`, params);
  }

}

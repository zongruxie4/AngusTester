import {http} from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/event';
  }

  loadStatistics (): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/exec/statistics`);
  }

  // 推送配置
  loadPushConfigList (params = {}): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/template/current`, { ...params, bigBizKey: 'AngusTester' });
  }

  // 推送记录
  loadPushRecordList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, { ...params, bigBizKey: 'AngusTester' });
  }

  // 接收通道详情
  loadReceiveSettingDetail (channelType:string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/channel/channel/${channelType}`);
  }

  // 配置接收通道
  saveReceiveSetting (params: {id:string, channelIds:string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/channel`, params);
  }

  // 获取已经配置的通道地址
  loadCurrentChannels (id: string): Promise<[Error|null, any]> {
    return http.get(`${baseUrl}/template/${id}/current`);
  }

  // 配置接收人
  putReceiver (params: {id: string, noticeTypes: string[], receiverIds: string[], receiverTypes: string[]}): Promise<[Error|null, any]> {
    return http.put(`${baseUrl}/template/receiver`, params);
  }

  getEventNoticeType () : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/tenant/tester/event`);
  }
}

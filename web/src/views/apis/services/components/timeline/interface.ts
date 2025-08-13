import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);


export interface Column {
  name: string,
  key: string,
  time: number,
  delay: number
}

export const columns:Column[] = [
  {
    name: t('service.timeline.items.dnsLookup'),
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: t('service.timeline.items.tcpConnection'),
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: t('service.timeline.items.ssl'),
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: t('service.timeline.items.requestSent'),
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('service.timeline.items.waiting'),
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('service.timeline.items.contentDownload'),
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
];

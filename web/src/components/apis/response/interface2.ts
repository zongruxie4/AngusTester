import { useI18n } from 'vue-i18n';
const { t } = useI18n();

export interface Column {
  name: string,
  key: string,
  time: number,
  delay: number
}

export const columns:Column[] = [
  {
    name: t('xcan_responseTimeAnalysis.dnsLookup'),
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: t('xcan_responseTimeAnalysis.tcpConnection'),
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: t('xcan_responseTimeAnalysis.ssl'),
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: t('xcan_responseTimeAnalysis.requestSent'),
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('xcan_responseTimeAnalysis.waiting'),
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: t('xcan_responseTimeAnalysis.contentDownload'),
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
];

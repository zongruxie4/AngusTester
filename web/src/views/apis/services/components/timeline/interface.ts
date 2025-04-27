export interface Column {
  name: string,
  key: string,
  time: number,
  delay: number
}

export const columns:Column[] = [
  {
    name: '域名解析(DNS lookup)',
    key: 'domainLookupEnd-domainLookupStart',
    time: 0,
    delay: 0
  },
  {
    name: 'TCP连接',
    key: 'connectEnd-connectStart',
    time: 0,
    delay: 0
  },
  {
    name: 'SSL',
    key: 'secureConnectionEnd-secureConnectionStart',
    time: 0,
    delay: 0
  },
  {
    name: '发送请求(Request sent)',
    key: 'responseEnd-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: '等待处理(Waiting)',
    key: 'responseStart-requestStart',
    time: 0,
    delay: 0
  },
  {
    name: '下载结果(Content download)',
    key: 'responseEnd-responseStart',
    time: 0,
    delay: 0
  }
];

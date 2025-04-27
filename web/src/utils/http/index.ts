import axios from 'axios';

import performanceObserver from './performanceObserver';

interface HeaderConfig {
  [key: string]: string
}

interface Config {
  url: string,
  method: Method,
  data: RequestBodyParam,
  headers: ParamsItem[],
  assertList?: AssertItem[]
}

const httpClient = axios.create({
  withCredentials: false
});

performanceObserver.bootstrap();

const bootstrap = async (config: Config): Promise<ResponseState> => {
  const { url, method, data, headers } = config;
  const _headers = getHeaders(headers);
  const _data = getData(data);
  const axiosConfig = {
    url: url.startsWith('http') ? url : 'http://' + url,
    method,
    data: _data,
    headers: _headers
  };

  // enctype是application/x-www-form-urlencoded的时候，GET请求需要拼接到url后
  if (method === 'GET' && _data instanceof URLSearchParams) {
    const tempUrl = new URL(axiosConfig.url);
    if (tempUrl.search) {
      axiosConfig.url += '&' + _data.toString();
    } else {
      axiosConfig.url += '?' + _data.toString();
    }
  }

  const target = { id: axiosConfig.url, timestap: Date.now() };
  // 设置performanceObserver监测的目标
  performanceObserver.setTarget(target);

  return new Promise((resolve) => {
    httpClient.request(axiosConfig)
      .then(async res => {
        const observer = await performanceObserver.getObserver(target);
        resolve(formatSuccessResult([null, res], observer));
      }).catch(async err => {
        resolve({ status: 0, errorMessage: err.message, config: { headers: err.config.headers } });
      });
  });
};

const formatSuccessResult = async ([error, res], observer) => {
  const size = res?.headers['content-length'] || res?.data?.size || getBodySize(res?.data);
  const result = { ...res, size: formatBytes(size), performance: observer, status: error ? 0 : res.status };
  // 因为axios的responseType是blob，因此要根据data.type进行数据转换
  const contentType = res.headers['content-type'];
  if (contentType.includes('stream')) {
    return result;
  }

  if (res && res.data) {
    const reader = new FileReader();
    reader.readAsText(res.data);
    const promise = new Promise((resolve) => {
      reader.onload = function () {
        const content = reader.result;// 内容就在这里
        if (content && contentType.includes('json')) {
          resolve(JSON.parse(content as string));
        } else {
          resolve(content);
        }
      };
    });
    return await promise.then((_data) => {
      result.data = _data;
      return result;
    });
  }
  return result;
};

const getData = (data: RequestBodyParam): Record<string, any> | string | undefined => {
  if (!data?.contentType) {
    return {};
  }

  if (data.contentType === RadioItem.encoded) {
    return data.formData?.reduce((prevValue, { name, value }) => {
      if (name) {
        prevValue.append(name, value);
      }

      return prevValue;
    }, new URLSearchParams()) || {};
  }

  if (data.contentType === RadioItem.formData) {
    return data.formData?.reduce((prevValue, { name, value }) => {
      if (name) {
        prevValue.append(name, value);
      }
      return prevValue;
    }, new FormData());
  }

  if (data.contentType === 'application/octet-stream') {
    return data.binary;
  }

  return data.rawContent;
};

const getHeaders = (_headers: ParamsItem[]): HeaderConfig => {
  const headers: HeaderConfig = _headers?.reduce((prevValue, currentValue) => {
    if (currentValue.name) {
      prevValue[currentValue.name] = currentValue.value;
    }
    return prevValue;
  }, {} as HeaderConfig) || {};

  return headers;
};

// c 参数：表示要被转化的容量大小，以字节为单
// b 参数：表示如果转换时出小数，四舍五入保留多少位 默认为2位小数
const formatBytes = (size: number, decimal = 2): string => {
  if (size === 0) return '0 B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + ' ' + e[f];
};

const getBodySize = (data: any): number => {
  if (!data) {
    return 0;
  }

  const blob = new Blob([JSON.stringify(data, null, 0)], { type: 'text/plain' });
  return blob.size;
};

export default { bootstrap };

import qs from 'qs';
import { API_EXTENSION_KEY } from '@/utils/ApiUtils/index';

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;
// 请求参数的字段
export interface ParamsItem {
  // 参数名称
  name: string;

  // 参数类型
  in: 'path'|'query';
  // 参数描述
  description: string;

  // 设为变量

  key?:symbol;

  // 参数值 设为变量
  [key: string]: any;
}

const paramsTypeOpt = [
  {
    value: 'query',
    label: 'query'
  },
  {
    value: 'path',
    label: 'path'
  }
];

// select options 的字段
export interface OptionItem {
  label: string,
  value: string
}

export const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: 'query',
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    schema: {
      type: 'string'
    },
    ...config
  };
};

const getUriByParams = (uri: string, paths: ParamsItem[], querys?:ParamsItem[]): string => {
  if (!uri) {
    return '';
  }

  const pathUri = uri.split('?')[0] || '/';

  // 正则变量
  // const rexVar = '';
  // path类型参数替换为格式为：{name}的占位符
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname = '';
  // eslint-disable-next-line prefer-regex-literals
  const pathReg = new RegExp(/{((?!{).)*}/g); // 匹配不包含 { 的任意字符串
  const preUrl = decodeURIComponent(originalPath);
  const uriPath = preUrl.match(pathReg); // 拿到 uri 上所有 {} 部分
  if (paths?.length) {
    let tempPath = preUrl;
    // let tempPath = paths.reduce((prevValue, currentValue) => {
    //   rexVar += '(?!' + currentValue.name + ')';
    //   return prevValue;
    // }, decodeURIComponent(originalPath));
    if (paths?.length > (uriPath?.length || 0)) {
      tempPath += `/{${paths?.[paths?.length - 1].name}}`;
    }
    uriPath?.forEach((i, idx) => {
      tempPath = tempPath.replace(i, paths[idx]?.name ? `{${paths[idx].name}}` : '');
    });
    // const pattern = new RegExp('{\\b(' + rexVar + '\\w)+\\b}', 'gi');
    pathname = tempPath;
  } else {
    pathname = originalPath.replace(/{\S+}/g, '');
  }
  pathname = pathname.replace(/\/{2,}/g, '/').replace(/\/$/, '');

  // query类型参数替换为name=value格式
  const queryObj = {};

  (querys || []).forEach(i => {
    queryObj[i.name] = i[valueKey];
  });
  const searchParams = qs.stringify(queryObj);

  return (pathname + '?' + searchParams?.toString()).replace(/\?$/, '');
};

const getParamsByUri = (uri = ''): ParamsItem[] => {
  // 获取path类型参数
  const temp: ParamsItem[] = uri.match(/{[^{}]+}/gi)?.map((item): ParamsItem => {
    return getDefaultParams({ name: item.replace(/{(\S*)}/gi, '$1'), in: 'path' });
  }) || [];

  if (/\?/.test(uri)) {
    // 获取query类型参数
    new URLSearchParams(uri.replace(/\S+\?(\S*)/g, '$1')).forEach((value, key) => {
      temp.push(getDefaultParams({ name: key, [valueKey]: value, [enabledKey]: true, in: 'query', schema: { type: 'string' } }));
    });
  }
  return temp;
};

export { getUriByParams, getParamsByUri, paramsTypeOpt };

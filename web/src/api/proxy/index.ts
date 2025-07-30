import { PUB_TESTER, http } from '@xcan-angus/infra';

/**
 * 将直连URL转换为代理URL
 * @param {string} directUrl 直连URL，例如：http://ip:port/actuator/runner/log/123?aa=11
 * @returns {string} 代理URL
 */
function convertToProxyUrl (directUrl):string {
  try {
    // 解析原始URL
    const url = new URL(directUrl);

    // 获取路径部分
    const pathName = url.pathname;

    // 获取查询参数
    const searchParams = url.searchParams;

    // 构建代理URL
    const proxyUrl = `${PUB_TESTER}/proxy`;

    // 添加targetAddr参数
    const targetAddr = `${url.origin}`;
    searchParams.set('targetAddr', targetAddr);

    return `${proxyUrl}${pathName}?${searchParams.toString()}`;
  } catch (e) {
    return directUrl; // 如果转换失败，返回原始URL
  }
}

export const getDataByProxy = async (directUrl: string, params = {}, axiosConf = {}) : Promise<[Error | null, any]> => {
  const fetchUrl = convertToProxyUrl(directUrl);
  return http.get(fetchUrl, params, axiosConf);
};

import { ref, computed } from 'vue';
import { ParameterIn, SchemaType } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { getDefaultParams } from '@/views/apis/services/protocol/http/utils';
import { HTTP_HEADERS } from '@/utils/constant';

/**
 * 参数管理composable
 * <p>
 * 管理API的各种参数，包括查询参数、路径参数、请求头、Cookie等
 * </p>
 */
export function useParameterManager () {
  const { valueKey, enabledKey } = API_EXTENSION_KEY;

  // 参数状态
  const parameters = ref<ParamsItem[]>([]);
  const headerList = ref<ParamsItem[]>([]);
  const cookieList = ref<ParamsItem[]>([]);
  const requestBody = ref<RequestBodyParam>({});
  const contentType = ref<string | null>(null);

  // 计算属性
  const parametersNum = computed(() => parameters.value.length);
  const headerCount = computed(() => {
    let base = headerList.value.filter(i => !!i.name).length;
    if (contentType.value) {
      base += 1;
    }
    return base;
  });
  const cookieCount = computed(() => cookieList.value.filter(i => !!i.name).length);

  /**
   * 更新参数列表
   * <p>
   * 更新查询和路径参数列表
   * </p>
   * @param data - 新的参数列表
   */
  const changeParamList = (data: ParamsItem[]): void => {
    parameters.value = data.filter(i => !!i.name || !!i[valueKey]);
  };

  /**
   * 更新请求头列表
   * <p>
   * 更新请求头参数列表
   * </p>
   * @param data - 新的请求头列表
   */
  const changeHeaderList = (data: ParamsItem[]): void => {
    headerList.value = data;
  };

  /**
   * 更新Cookie列表
   * <p>
   * 更新Cookie参数列表
   * </p>
   * @param data - 新的Cookie列表
   */
  const changeCookieList = (data: ParamsItem[]): void => {
    cookieList.value = data;
  };

  /**
   * 更新请求体
   * <p>
   * 更新请求体参数
   * </p>
   * @param data - 新的请求体数据
   */
  const changeRequestBody = (data: RequestBodyParam): void => {
    requestBody.value = data;
  };

  /**
   * 添加查询参数
   * <p>
   * 添加新的查询参数到参数列表
   * </p>
   * @param data - 要添加的参数数据
   * @param requestParamsRef - 请求参数组件引用
   */
  const addQueryParam = (data: any, requestParamsRef: any) => {
    setTimeout(() => {
      requestParamsRef.value.addParam(data);
    });
  };

  /**
   * 从URL提取查询参数
   * <p>
   * 从endpoint URL中提取查询参数并转换为参数列表
   * </p>
   * @param endpoint - API端点URL
   * @returns 查询参数列表
   */
  const extractQueryParamsFromUrl = (endpoint: string): ParamsItem[] => {
    let queryStrParam: ParamsItem[] = [];
    if (endpoint && endpoint.split('?')[1]) {
      const queryString = endpoint.split('?')[1];
      const queryStrArr = queryString.split('&');
      queryStrParam = queryStrArr.map(keyValue => {
        const [key, value] = keyValue.split('=');
        return getDefaultParams({ [valueKey]: value || '', name: key });
      });
    }
    return queryStrParam;
  };

  /**
   * 处理参数数据
   * <p>
   * 处理从API详情加载的参数数据，包括类型转换和默认值设置
   * </p>
   * @param parameters - 原始参数数据
   * @param endpoint - API端点URL
   * @returns 处理后的参数列表
   */
  const processParameters = (parameters: any[], endpoint: string) => {
    // 从URL提取查询参数
    const queryStrParam = extractQueryParamsFromUrl(endpoint);

    // 处理参数类型转换
    for (const key in parameters) {
      if (parameters[key].$ref) {
        // 处理引用参数 - 这里需要调用getModelFromRef方法
        parameters[key] = { name: undefined, schema: { type: SchemaType.string }, in: ParameterIn.query, [valueKey]: '' };
      }

      // 处理参数值类型转换
      if ([SchemaType.object, SchemaType.array, SchemaType.boolean, SchemaType.integer, SchemaType.number]
        .includes(parameters[key].schema?.type) && parameters[key]?.[valueKey] && typeof parameters[key]?.[valueKey] === 'string') {
        try {
          parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
        } catch {
          console.log(parameters[key][valueKey] + 'is not an object');
        }
      }
    }

    // 设置参数列表
    const processedParams = (parameters?.filter(item => [ParameterIn.query, ParameterIn.path]
      .includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    return [...queryStrParam, ...processedParams];
  };

  /**
   * 处理请求头数据
   * <p>
   * 处理从API详情加载的请求头数据
   * </p>
   * @param parameters - 原始参数数据
   * @returns 处理后的请求头列表
   */
  const processHeaders = (parameters: any[]) => {
    return (parameters?.filter(i => i.in === ParameterIn.header && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  };

  /**
   * 处理Cookie数据
   * <p>
   * 处理从API详情加载的Cookie数据
   * </p>
   * @param parameters - 原始参数数据
   * @returns 处理后的Cookie列表
   */
  const processCookies = (parameters: any[]) => {
    return (parameters?.filter(i => i.in === ParameterIn.cookie && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  };

  /**
   * 提取内容类型
   * <p>
   * 从请求头中提取内容类型并更新状态
   * </p>
   * @param headers - 请求头列表
   */
  const extractContentType = (headers: ParamsItem[]) => {
    const contentTypeIndex = headers.findIndex(i => i.name === HTTP_HEADERS.CONTENT_TYPE);
    if (contentTypeIndex > -1) {
      contentType.value = headers[contentTypeIndex]?.[valueKey];
      if (contentType.value) {
        headers.splice(contentTypeIndex, 1);
      }
    }
  };

  /**
   * 获取有效的请求头
   * <p>
   * 获取有效的请求头列表（过滤掉空名称）
   * </p>
   * @returns 有效的请求头列表
   */
  const getValidHeaders = () => {
    return headerList.value.filter(i => !!i.name);
  };

  /**
   * 重置参数状态
   * <p>
   * 重置所有参数相关状态到初始值
   * </p>
   */
  const resetParameterState = () => {
    parameters.value = [];
    headerList.value = [];
    cookieList.value = [];
    requestBody.value = {};
    contentType.value = null;
  };

  return {
    // 状态
    parameters,
    headerList,
    cookieList,
    requestBody,
    contentType,

    // 计算属性
    parametersNum,
    headerCount,
    cookieCount,

    // 方法
    changeParamList,
    changeHeaderList,
    changeCookieList,
    changeRequestBody,
    addQueryParam,
    extractQueryParamsFromUrl,
    processParameters,
    processHeaders,
    processCookies,
    extractContentType,
    getValidHeaders,
    resetParameterState
  };
}

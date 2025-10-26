import { ref, computed } from 'vue';
import { ParameterIn, SchemaType } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { getDefaultParams } from '@/views/apis/services/protocol/http/utils';
import { HTTP_HEADERS } from '@/utils/constant';

/**
 * Parameter management composable
 * <p>
 * Manages various API parameters including query, path, headers, and cookies.
 * </p>
 */
export function useParameterManager (state: any) {
  const { valueKey, enabledKey } = API_EXTENSION_KEY;

  // Parameter state
  const parameters = ref<ParamsItem[]>([]);
  const headerList = ref<ParamsItem[]>([]);
  const cookieList = ref<ParamsItem[]>([]);
  const requestBody = ref<RequestBodyParam>({});
  const contentType = ref<string | null>(null);

  // Computed
  const parametersNum = computed(() => state.parameters.length);
  const headerCount = computed(() => {
    let base = state.headerList.filter(i => !!i.name).length;
    if (contentType.value) {
      base += 1;
    }
    return base;
  });
  const cookieCount = computed(() => state.cookieList.filter(i => !!i.name).length);

  /**
   * Update parameter list
   * <p>
   * Update query and path parameters list
   * </p>
   * @param data - New parameter list
   */
  const changeParamList = (data: ParamsItem[]): void => {
    // parameters.value = data.filter(i => !!i.name || !!i[valueKey]);
    state.parameters = data.filter(i => !!i.name || !!i[valueKey]);
  };

  /**
   * Update header list
   * <p>
   * Update request header parameters list
   * </p>
   * @param data - New header list
   */
  const changeHeaderList = (data: ParamsItem[]): void => {
    state.headerList = data;
  };

  /**
   * Update cookie list
   * <p>
   * Update cookie parameter list
   * </p>
   * @param data - New cookie list
   */
  const changeCookieList = (data: ParamsItem[]): void => {
    state.cookieList = data;
  };

  /**
   * Update request body
   * <p>
   * Update request body parameters
   * </p>
   * @param data - New request body data
   */
  const changeRequestBody = (data: RequestBodyParam): void => {
    state.requestBody = data;
  };

  /**
   * Add query parameter
   * <p>
   * Add a new query parameter to the parameters list
   * </p>
   * @param data - Parameter data to add
   * @param requestParamsRef - Request parameters component ref
   */
  const addQueryParam = (data: any, requestParamsRef: any) => {
    setTimeout(() => {
      requestParamsRef.value.addParam(data);
    });
  };

  /**
   * Extract query parameters from URL
   * <p>
   * Extract query parameters from the endpoint URL and convert to a list
   * </p>
   * @param endpoint - API endpoint URL
   * @returns Query parameters list
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
   * Process parameters
   * <p>
   * Process parameters loaded from API details, including type coercion and defaults
   * </p>
   * @param parameters - Raw parameter data
   * @param endpoint - API endpoint URL
   * @returns Processed parameter list
   */
  const processParameters = (parameters: any[], endpoint: string) => {
    // Extract query params from URL
    const queryStrParam = extractQueryParamsFromUrl(endpoint);

    // Handle parameter type conversion
    for (const key in parameters) {
      if (parameters[key].$ref) {
        // Handle $ref parameter - getModelFromRef should be invoked here
        parameters[key] = { name: undefined, schema: { type: SchemaType.string }, in: ParameterIn.query, [valueKey]: '' };
      }

      // Convert parameter value types
      if ([SchemaType.object, SchemaType.array, SchemaType.boolean, SchemaType.integer, SchemaType.number]
        .includes(parameters[key].schema?.type) && parameters[key]?.[valueKey] && typeof parameters[key]?.[valueKey] === 'string') {
        try {
          parameters[key][valueKey] = JSON.parse(parameters[key][valueKey]);
        } catch {
          console.log(parameters[key][valueKey] + 'is not an object');
        }
      }
    }

    // Build parameter list
    const processedParams = (parameters?.filter(item => [ParameterIn.query, ParameterIn.path]
      .includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    return [...queryStrParam, ...processedParams];
  };

  /**
   * Process headers
   * <p>
   * Process headers loaded from API details
   * </p>
   * @param parameters - Raw parameters
   * @returns Processed header list
   */
  const processHeaders = (parameters: any[]) => {
    return (parameters?.filter(i => i.in === ParameterIn.header && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  };

  /**
   * Process cookies
   * <p>
   * Process cookies loaded from API details
   * </p>
   * @param parameters - Raw parameters
   * @returns Processed cookie list
   */
  const processCookies = (parameters: any[]) => {
    return (parameters?.filter(i => i.in === ParameterIn.cookie && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));
  };

  /**
   * Extract content type
   * <p>
   * Extract content type from request headers and update state
   * </p>
   * @param headers - Request header list
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
   * Get valid headers
   * <p>
   * Get valid header list (filter out empty names)
   * </p>
   * @returns Valid header list
   */
  const getValidHeaders = () => {
    return headerList.value.filter(i => !!i.name);
  };

  /**
   * Reset parameter state
   * <p>
   * Reset all parameter-related state to initial values
   * </p>
   */
  const resetParameterState = () => {
    // parameters.value = [];
    // headerList.value = [];
    // cookieList.value = [];
    // requestBody.value = {};
    state.parameters = [];
    state.headerList = [];
    state.cookieList = [];
    state.requestBody = {};
    contentType.value = null;
  };

  return {
    // State
    parameters,
    headerList,
    cookieList,
    requestBody,
    contentType,

    // Computed
    parametersNum,
    headerCount,
    cookieCount,

    // Methods
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

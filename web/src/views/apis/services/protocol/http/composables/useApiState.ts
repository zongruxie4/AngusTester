import { ref, reactive, computed, watch } from 'vue';
import { HttpMethod, ParameterIn, SchemaType, ActionOnEOF, SharingMode } from '@xcan-angus/infra';
import { apis, services } from '@/api/tester';
import { ApiPermission, ApiServerSource, ApiStatus } from '@/enums/enums';
import { API_EXTENSION_KEY, getModelDataByRef } from '@/utils/apis';
import { deconstruct } from '@/utils/swagger';
import { ServerInfo } from '@/views/apis/server/types';
import { RequestBodyParam } from '@/views/apis/services/protocol/http/requestBody/types';
import { ParamsItem, ApisFormEdit } from '@/views/apis/services/protocol/types';
import { AuthenticationItem } from '@/views/apis/services/protocol/http/Authorization';
import { RequestSetting } from '@/views/apis/services/protocol/http/types';

/**
 * API state management composable
 * <p>
 * Manages base API state including API details, permissions, server info, etc.
 * </p>
 */
export function useApiState (props: {
  id?: string;
  serviceId?: string;
  projectId: string;
  valueObj: Record<string, any>;
}) {
  const { serverSourceKey, requestSettingKey, valueKey, enabledKey } = API_EXTENSION_KEY;

  // API base info
  const saveParams = ref<ApisFormEdit>({
    id: undefined,
    operationId: undefined,
    summary: undefined,
    ownerId: undefined,
    serviceId: undefined,
    serviceName: undefined,
    ownerName: undefined,
    unarchivedId: undefined,
    responses: undefined
  });

  // API status
  const isUnarchivedApi = ref<boolean>(true);
  const apisStatus = ref<ApiStatus>();
  const loadingApiDetail = ref(false);
  const apiAuths = ref<ApiPermission[]>([]);

  // Server info
  const currentServer = ref<ServerInfo>({ url: '' });
  const defaultCurrentServer = ref<ServerInfo>();
  const availableServers = ref<ServerInfo[]>([]);

  // API method
  const apiMethod = ref<HttpMethod>(HttpMethod.GET);
  const apiUri = ref<string>();

  // Main state
  const state = reactive({
    authentication: { type: null } as AuthenticationItem,
    parameter: {},
    assertTypeOptions: [],
    assertConditionOptions: [],
    paramTypeOptions: [],
    bodyParamTypeOptions: [],
    contentTypeOptions: [],
    formDataTypeOptions: [],
    methodOptions: [],
    authTypeOptions: [],
    parameters: [] as ParamsItem[],
    requestBody: {} as RequestBodyParam,
    headerList: [] as ParamsItem[],
    cookieList: [] as ParamsItem[],
    assertions: [] as any[],
    secured: false,
    publishFlag: false
  });

  // Request setting
  const setting = ref<RequestSetting>({
    enableParamValidation: false,
    connectTimeout: 6000,
    readTimeout: 60000,
    retryNum: 0,
    maxRedirects: 1
  });

  // Authentication
  const defaultAuthentication = ref<AuthenticationItem>({ type: null });
  const authInHeader = ref<Record<string, any>>({});

  // Content-Type
  const contentType = ref<string | null>(null);
  const resolvedRefModels = ref<Record<string, string>>({});

  // Dataset parameterization config
  const datasetActionOnEOF = ref<ActionOnEOF>(ActionOnEOF.RECYCLE);
  const datasetSharingMode = ref<SharingMode>(SharingMode.ALL_THREAD);

  // Computed
  const parametersNum = computed(() => state.parameters.length);
  const headerCount = computed(() => {
    let base = state.headerList.filter(i => !!i.name).length;
    if (authInHeader.value) {
      const length = Object.keys(authInHeader.value).length;
      base += length;
    }
    if (contentType.value) {
      base += 1;
    }
    return base;
  });
  const cookieCount = computed(() => state.cookieList.filter(i => !!i.name).length);
  const hasBodyContent = computed(() => !!contentType.value || !!state.requestBody.content);

  /**
   * Load API permissions
   * <p>
   * Fetch current user's permissions for the API and update apiAuths.
   * </p>
   */
  const loadApiAuth = async () => {
    const [error, res] = await apis.getCurrentAuth(props.id as string);
    if (error) {
      return;
    }
    apiAuths.value = (res.data?.permissions || []).map(i => i.value);
  };

  /**
   * Load service servers
   * <p>
   * Fetch available server URLs for the given service and update availableServers.
   * </p>
   * @param serviceId - Service ID to load servers for
   */
  const loadServiceServers = async (serviceId: string) => {
    const [error, resp] = await services.getServicesServerUrlInfo(serviceId);
    if (error) {
      return;
    }
    availableServers.value = (resp.data || []).map(i => ({ ...i, ...(i.extensions || {}) }));
  };

  /**
   * Load API details
   * <p>
   * Fetch detailed API info including parameters, requestBody, authentication, etc.
   * Handles archived and unarchived APIs.
   * </p>
   */
  const loadApiDetail = async (): Promise<void> => {
    loadingApiDetail.value = true;
    let result;
    if (isUnarchivedApi.value) {
      result = await apis.getUnarchivedApiDetail(props.id as string);
    } else {
      result = await apis.getApiDetail(props.id as string);
    }
    loadingApiDetail.value = false;
    const [error, res] = result;
    if (error) {
      return;
    }

    const {
      assertions,
      authentication,
      operationId,
      id,
      method,
      summary,
      ownerId,
      ownerName,
      parameters,
      serviceId,
      serviceName,
      requestBody,
      endpoint,
      status,
      responses,
      auth,
      serviceAuth,
      datasetActionOnEOF: _datasetActionOnEOF,
      datasetSharingMode: _datasetSharingMode
    } = res.data;

    // 加载服务服务器（如果服务ID可用）
    if (serviceId) {
      await loadServiceServers(serviceId);
    }
    apisStatus.value = status?.value;

    // 设置数据集配置
    datasetActionOnEOF.value = _datasetActionOnEOF?.value || _datasetActionOnEOF || ActionOnEOF.RECYCLE;
    datasetSharingMode.value = _datasetSharingMode?.value || _datasetSharingMode || SharingMode.ALL_THREAD;

    // 为已归档API加载API权限
    if (!isUnarchivedApi.value && auth && serviceAuth) {
      await loadApiAuth();
    }

    // 更新保存参数
    saveParams.value.id = id;
    saveParams.value.operationId = operationId;
    saveParams.value.summary = summary;
    saveParams.value.ownerId = ownerId;
    saveParams.value.ownerName = ownerName;
    saveParams.value.serviceId = serviceId;
    saveParams.value.serviceName = serviceName;
    saveParams.value.responses = responses;

    apiMethod.value = method.value || method;

    // 设置当前服务器
    if (isUnarchivedApi.value) {
      currentServer.value = res.data?.currentServer || { url: '' };
    } else {
      const availableServersFromPro = (res.data?.availableServers || []).map(i => ({ ...i, ...(i.extensions || {}) }));
      if (availableServersFromPro.length) {
        currentServer.value = availableServersFromPro.find(i => i[serverSourceKey] === ApiServerSource.CURRENT_REQUEST) || availableServersFromPro[0];
      } else {
        currentServer.value = { url: '' };
      }
      availableServers.value = (availableServersFromPro || []).filter(i => i[serverSourceKey] !== ApiServerSource.CURRENT_REQUEST);
    }
    defaultCurrentServer.value = JSON.parse(JSON.stringify(currentServer.value));

    // 处理请求设置
    if (res.data[requestSettingKey]) {
      const _setting = res.data[requestSettingKey];
      setting.value = {
        ..._setting,
        connectTimeout: (_setting.connectTimeout || '').replace('ms', ''),
        readTimeout: (_setting.readTimeout || '').replace('ms', '')
      };
    }

    // 处理断言
    state.assertions = assertions?.map(item => ({
      ...item,
      assertionCondition: item.assertionCondition?.value,
      extraction: item.extraction
        ? ({
            ...item.extraction,
            method: item.extraction.method.value
          })
        : null
    })) || [];

    // 处理认证
    state.authentication = authentication ? { ...authentication, ...(authentication.extensions || {}) } : { type: null };
    defaultAuthentication.value = JSON.parse(JSON.stringify(state.authentication));
    state.publishFlag = (status?.value === ApiStatus.RELEASED);
    state.secured = !!state.authentication.type || !!(state.authentication as any).$ref;

    // 处理参数
    for (const key in parameters) {
      if (parameters[key].$ref) {
        // Handle $ref parameter
        // getModelFromRef should be invoked here, but keep composable pure with a placeholder
        parameters[key] = { name: undefined, schema: { type: SchemaType.string }, in: ParameterIn.query, [valueKey]: '' };
      }

      // Convert parameter value types when provided as strings
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
    state.parameters = (parameters?.filter(item => [ParameterIn.query, ParameterIn.path]
      .includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    state.headerList = (parameters?.filter(i => i.in === ParameterIn.header && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    apiUri.value = endpoint ? endpoint.split('?')[0] : undefined;

    // Extract Content-Type from headers
    const contentTypeIndex = state.headerList.findIndex(i => i.name === 'Content-Type');
    if (contentTypeIndex > -1) {
      contentType.value = state.headerList[contentTypeIndex]?.[valueKey];
      if (contentType.value) {
        state.headerList.splice(contentTypeIndex, 1);
      }
    }

    state.cookieList = (parameters?.filter(i => i.in === ParameterIn.cookie && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    state.requestBody = requestBody || {};

    if (res.data.resolvedRefModels) {
      resolvedRefModels.value = res.data.resolvedRefModels;
    }
  };

  /**
   * Handle status change
   * <p>
   * Update API status based on changes of props.id
   * </p>
   */
  const handleStatusChange = async (value: string) => {
    const [error] = await apis.patchApiStatus({ status: value, id: props.id });
    if (error) {
      return;
    }
    apisStatus.value = value as ApiStatus;
  };

  /**
   * Update API info
   * <p>
   * Update base API info and trigger related updates
   * </p>
   */
  const updateApiInfo = async (info: any) => {
    saveParams.value.id = info.id;
    saveParams.value.ownerId = info.ownerId;
    saveParams.value.serviceId = info.serviceId;
    saveParams.value.summary = info.name;
    isUnarchivedApi.value = false;
  };

  /**
   * Update unarchived API info
   * <p>
   * Update base info for unarchived API
   * </p>
   */
  const updateUnarchivedApiInfo = async (info: any) => {
    saveParams.value.id = info.id;
    saveParams.value.summary = info.name;
    isUnarchivedApi.value = true;
  };

  /**
   * Get model data from $ref
   * <p>
   * Fetch model by reference and deconstruct it.
   * </p>
   * @param ref - Model reference
   * @returns Deconstructed model data
   */
  const getModelFromRef = async (ref: string) => {
    if (!saveParams.value.serviceId) {
      return {};
    }
    const [error, resp] = await getModelDataByRef(saveParams.value.serviceId, ref);
    if (error) {
      return {};
    }
    return deconstruct(resp.data || {});
  };

  // Watch props.id changes
  watch(() => props.id, () => {
    if (props.valueObj.unarchived || !props.id) {
      isUnarchivedApi.value = true;
      if (props.id) {
        loadApiDetail();
      }
    } else {
      isUnarchivedApi.value = false;
      loadApiDetail();
    }
  }, {
    immediate: true
  });

  return {
    // State
    saveParams,
    isUnarchivedApi,
    apisStatus,
    loadingApiDetail,
    apiAuths,
    currentServer,
    defaultCurrentServer,
    availableServers,
    apiMethod,
    apiUri,
    state,
    setting,
    defaultAuthentication,
    authInHeader,
    contentType,
    resolvedRefModels,
    datasetActionOnEOF,
    datasetSharingMode,

    // Computed
    parametersNum,
    headerCount,
    cookieCount,
    hasBodyContent,

    // Methods
    loadApiDetail,
    loadApiAuth,
    loadServiceServers,
    handleStatusChange,
    updateApiInfo,
    updateUnarchivedApiInfo,
    getModelFromRef
  };
}

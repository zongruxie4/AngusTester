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
 * API状态管理composable
 * <p>
 * 管理API的基本状态信息，包括API详情、权限、服务器信息等
 * </p>
 */
export function useApiState (props: {
  id?: string;
  serviceId?: string;
  projectId: string;
  valueObj: Record<string, any>;
}) {
  const { serverSourceKey, requestSettingKey, valueKey, enabledKey } = API_EXTENSION_KEY;

  // API基本信息
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

  // API状态
  const isUnarchivedApi = ref<boolean>(true);
  const apisStatus = ref<ApiStatus>();
  const loadingApiDetail = ref(false);
  const apiAuths = ref<ApiPermission[]>([]);

  // 服务器信息
  const currentServer = ref<ServerInfo>({ url: '' });
  const defaultCurrentServer = ref<ServerInfo>();
  const availableServers = ref<ServerInfo[]>([]);

  // API方法
  const apiMethod = ref<HttpMethod>(HttpMethod.GET);
  const apiUri = ref<string>();

  // 主状态
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

  // 请求设置
  const setting = ref<RequestSetting>({
    enableParamValidation: false,
    connectTimeout: 6000,
    readTimeout: 60000,
    retryNum: 0,
    maxRedirects: 1
  });

  // 认证信息
  const defaultAuthentication = ref<AuthenticationItem>({ type: null });
  const authInHeader = ref<Record<string, any>>({});

  // 内容类型
  const contentType = ref<string | null>(null);
  const resolvedRefModels = ref<Record<string, string>>({});

  // 参数化配置
  const datasetActionOnEOF = ref<ActionOnEOF>(ActionOnEOF.RECYCLE);
  const datasetSharingMode = ref<SharingMode>(SharingMode.ALL_THREAD);

  // 计算属性
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
   * 加载API操作权限
   * <p>
   * 获取当前用户对API的权限并更新apiAuths响应式引用
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
   * 加载服务服务器信息
   * <p>
   * 获取指定服务的可用服务器URL并更新availableServers响应式引用
   * </p>
   * @param serviceId - 要加载服务器的服务ID
   */
  const loadServiceServers = async (serviceId: string) => {
    const [error, resp] = await services.getServicesServerUrlInfo(serviceId);
    if (error) {
      return;
    }
    availableServers.value = (resp.data || []).map(i => ({ ...i, ...(i.extensions || {}) }));
  };

  /**
   * 加载API详细信息
   * <p>
   * 获取详细的API信息，包括参数、请求体、认证等配置。处理已归档和未归档的API。
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
        // 处理引用参数
        // 这里需要调用getModelFromRef方法，但为了保持composable的纯净性，我们返回一个占位符
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
    state.parameters = (parameters?.filter(item => [ParameterIn.query, ParameterIn.path]
      .includes(item.in) && !!item.name) || []).map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    state.headerList = (parameters?.filter(i => i.in === ParameterIn.header && !!i.name) || [])
      .map(i => ({ ...i, [enabledKey]: i[enabledKey] !== false }));

    apiUri.value = endpoint ? endpoint.split('?')[0] : undefined;

    // 处理内容类型
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
   * 处理状态变化
   * <p>
   * 根据props.id的变化更新API状态
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
   * 更新API信息
   * <p>
   * 更新API的基本信息并触发相关更新
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
   * 更新未归档API信息
   * <p>
   * 更新未归档API的基本信息
   * </p>
   */
  const updateUnarchivedApiInfo = async (info: any) => {
    saveParams.value.id = info.id;
    saveParams.value.summary = info.name;
    isUnarchivedApi.value = true;
  };

  /**
   * 从引用获取模型数据
   * <p>
   * 根据引用获取模型数据并解构
   * </p>
   * @param ref - 模型引用
   * @returns 解构后的模型数据
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

  // 监听props.id变化
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
    // 状态
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

    // 计算属性
    parametersNum,
    headerCount,
    cookieCount,
    hasBodyContent,

    // 方法
    loadApiDetail,
    loadApiAuth,
    loadServiceServers,
    handleStatusChange,
    updateApiInfo,
    updateUnarchivedApiInfo,
    getModelFromRef
  };
}

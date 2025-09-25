import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';

import { mock } from '@/api/tester';
import type { MockServiceDetail, ServerSettings, CorsSetting, ApiSecurityItem } from '../types';

/**
 * Composable for managing mock service settings
 * Handles form state, editing operations, and API interactions
 */
export function useMockSetting (id: string) {
  const { t } = useI18n();

  // Reactive state for form data
  const infoFormState = ref({
    serviceDomainUrl: '',
    name: ''
  });

  const securityFormState = ref<{ apisSecurity: ApiSecurityItem[] }>({
    apisSecurity: []
  });

  const apisCors = ref<CorsSetting>({
    allowCorsCredentials: true,
    allowCorsOrigin: '*',
    allowCorsRequestHeaders: '',
    allowCorsRequestMethods: '',
    allowExposeHeaders: '',
    enabled: true
  });

  const setting = ref<ServerSettings>({
    useSsl: false,
    workThreadNum: '256',
    enableNettyLog: false,
    sendRequestLog: false,
    logFileLevel: 'BASIC',
    maxContentLength: '1048576000',
    workPushbackThreadNum: '8',
    maxPushbackConnectTimeout: '5000',
    maxPushbackRequestTimeout: '-1'
  });

  // UI state management
  const mockServiceInfo = ref<MockServiceDetail>();
  const loading = ref(false);
  const editionType = ref<string>();

  // Edit mode flags for different form sections
  const editServiceDomain = ref(false);
  const editName = ref(false);
  const editCredentials = ref(false);
  const editOrigin = ref(false);
  const editRequestHeaders = ref(false);
  const editRequestMethods = ref(false);
  const editExposeHeaders = ref(false);
  const editWorkThreadNum = ref(false);
  const editMaxContentLength = ref(false);
  const editWorkPushbackThreadNum = ref(false);
  const editMaxPushbackConnectTimeout = ref(false);
  const editMaxPushbackRequestTimeout = ref(false);

  const isOpenSurety = ref(false);

  // Options for form controls
  const corsCookieOptions = [
    { label: 'true', value: true },
    { label: 'false', value: false }
  ];

  const inOptions = ref([
    { label: 'header', value: 'header' },
    { label: 'query', value: 'query' }
  ]);

  /**
   * Load mock service details from API
   * Initializes form state with service data
   */
  const loadInfo = async () => {
    loading.value = true;
    const [error, { data }] = await mock.getServiceDetail(id);
    loading.value = false;

    if (error) return;

    mockServiceInfo.value = JSON.parse(JSON.stringify(data));
    infoFormState.value.name = data.name;
    infoFormState.value.serviceDomainUrl = data.serviceDomainUrl;

    // Initialize security settings
    securityFormState.value.apisSecurity = data.apisSecurity?.length
      ? data.apisSecurity.map(item => ({ ...item, in: item.in.value }))
      : [];
    isOpenSurety.value = !!data.apisSecurity?.length;

    // Initialize CORS and settings
    apisCors.value = JSON.parse(JSON.stringify(data.apisCors));
    const _setting = JSON.parse(JSON.stringify(data.setting));
    setting.value = { ..._setting, logFileLevel: _setting.logFileLevel.value };
  };

  /**
   * Handle security API change
   * Manages the enable/disable state of security settings
   */
  const apiSuretyChange = async (value: boolean) => {
    isOpenSurety.value = value;

    if (value) {
      securityFormState.value.apisSecurity.push({
        keyName: '',
        value: '',
        in: 'header'
      });
    } else {
      securityFormState.value.apisSecurity = [];

      if (!mockServiceInfo.value?.apisSecurity?.length) return;

      const _params = { id: mockServiceInfo.value.id, apisSecurity: [] };
      loading.value = true;
      const [error] = await mock.patchService(_params);
      loading.value = false;

      if (error) {
        securityFormState.value.apisSecurity = mockServiceInfo.value.apisSecurity?.length
          ? mockServiceInfo.value.apisSecurity
          : [];
        isOpenSurety.value = !!mockServiceInfo.value.apisSecurity?.length;
        return;
      }

      await loadInfo();
      notification.success(t('actions.tips.modifySuccess'));
    }
  };

  /**
   * Generic edit handler for form fields
   * Manages edit mode, validation, and API updates
   */
  const handleEdit = (
    key: string,
    type: 'open' | 'cancel' | 'save',
    formType: 'infoForm' | 'settingForm' | 'apisCorsForm'
  ) => {
    const editMap = {
      infoForm: {
        serviceDomainUrl: editServiceDomain,
        name: editName
      },
      settingForm: {
        workThreadNum: editWorkThreadNum,
        maxContentLength: editMaxContentLength,
        workPushbackThreadNum: editWorkPushbackThreadNum,
        maxPushbackConnectTimeout: editMaxPushbackConnectTimeout,
        maxPushbackRequestTimeout: editMaxPushbackRequestTimeout
      },
      apisCorsForm: {
        allowCorsCredentials: editCredentials,
        allowCorsOrigin: editOrigin,
        allowCorsRequestHeaders: editRequestHeaders,
        allowCorsRequestMethods: editRequestMethods,
        allowExposeHeaders: editExposeHeaders
      }
    };

    const editValue = editMap[formType][key];

    if (type === 'open') {
      editValue.value = true;
    }

    // Handle different form types
    if (formType === 'infoForm') {
      handleInfoFormEdit(key, type, editValue);
    } else if (formType === 'settingForm') {
      handleSettingFormEdit(key, type, editValue);
    } else if (formType === 'apisCorsForm') {
      handleApisCorsFormEdit(key, type, editValue);
    }
  };

  /**
   * Handle info form edit operations
   */
  const handleInfoFormEdit = (key: string, type: string, editValue: any) => {
    if (!mockServiceInfo.value) return;

    if (type === 'cancel') {
      infoFormState.value[key] = mockServiceInfo.value[key];
      editValue.value = false;
    }

    if (type === 'save') {
      if (infoFormState.value[key] === mockServiceInfo.value[key]) {
        editValue.value = false;
        return;
      }

      const _params = key === 'serviceDomainUrl'
        ? { id: mockServiceInfo.value.id, serviceDomain: infoFormState.value[key] }
        : { id: mockServiceInfo.value.id, [key]: infoFormState.value[key] };

      updateService(_params, key, editValue);
    }
  };

  /**
   * Handle setting form edit operations
   */
  const handleSettingFormEdit = (key: string, type: string, editValue: any) => {
    if (!mockServiceInfo.value) return;

    if (type === 'cancel') {
      setting.value[key] = mockServiceInfo.value.setting[key];
      editValue.value = false;
    }

    if (type === 'save') {
      if (setting.value[key] === mockServiceInfo.value.setting[key]) {
        editValue.value = false;
        return;
      }

      if (setting.value[key] === '' || setting.value[key] === undefined) {
        setting.value[key] = mockServiceInfo.value.setting[key];
        editValue.value = false;
        return;
      }

      const _setting = JSON.parse(JSON.stringify(mockServiceInfo.value.setting));
      const _params = { id: mockServiceInfo.value.id, setting: _setting };
      _params.setting[key] = setting.value[key];

      updateService(_params, `setting.${key}`, editValue);
    }
  };

  /**
   * Handle CORS form edit operations
   */
  const handleApisCorsFormEdit = (key: string, type: string, editValue: any) => {
    if (!mockServiceInfo.value) return;

    if (type === 'cancel') {
      apisCors.value[key] = mockServiceInfo.value.apisCors[key];
      editValue.value = false;
    }

    if (type === 'save') {
      if (apisCors.value[key] === mockServiceInfo.value.apisCors[key]) {
        editValue.value = false;
        return;
      }

      if (apisCors.value[key] === '' || apisCors.value[key] === undefined) {
        apisCors.value[key] = mockServiceInfo.value.apisCors[key];
        editValue.value = false;
        return;
      }

      const _apisCors = JSON.parse(JSON.stringify(mockServiceInfo.value.apisCors));
      const _params = { id: mockServiceInfo.value.id, apisCors: _apisCors };
      _params.apisCors[key] = apisCors.value[key];

      updateService(_params, `apisCors.${key}`, editValue);
    }
  };

  /**
   * Update service via API call
   */
  const updateService = async (params: any, key: string, editValue: any) => {
    if (!mockServiceInfo.value) return;

    loading.value = true;
    const [error] = await mock.patchService(params);
    loading.value = false;

    if (error) return;

    // Update local state
    const keyPath = key.split('.');
    if (keyPath.length === 1) {
      mockServiceInfo.value[key] = params[key];
    } else {
      mockServiceInfo.value[keyPath[0]][keyPath[1]] = params[keyPath[0]][keyPath[1]];
    }

    notification.success(t('actions.tips.modifySuccess'));
    editValue.value = false;
  };

  /**
   * Handle setting flag changes (switches and radio buttons)
   */
  const settingFlagChange = (key: string, value?: string | boolean) => {
    if (!mockServiceInfo.value) return;

    const _setting = JSON.parse(JSON.stringify(mockServiceInfo.value.setting));
    const _params = { id: mockServiceInfo.value.id, setting: _setting };

    if (['useSsl', 'logFileLevel', 'enableNettyLog', 'sendRequestLog'].includes(key)) {
      _params.setting[key] = value;
    }

    updateSetting(_params, key);
  };

  /**
   * Update setting via API call
   */
  const updateSetting = async (_params: any, key: string) => {
    if (!mockServiceInfo.value) return;

    loading.value = true;
    const [error] = await mock.patchService(_params);
    loading.value = false;

    if (error) return;

    mockServiceInfo.value.setting[key] = _params.setting[key];
    setting.value[key] = _params.setting[key];
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Handle CORS enabled flag change
   */
  const apisCorsEnabledFlagChange = (value: boolean) => {
    if (!mockServiceInfo.value) return;

    const _apisCors = JSON.parse(JSON.stringify(mockServiceInfo.value.apisCors));
    const _params = { id: mockServiceInfo.value.id, apisCors: _apisCors };
    _params.apisCors.enabled = value;
    updateApiCors(_params, 'enabled');
  };

  /**
   * Update CORS settings via API call
   */
  const updateApiCors = async (_params: any, key: string) => {
    if (!mockServiceInfo.value) return;

    loading.value = true;
    const [error] = await mock.patchService(_params);
    loading.value = false;

    if (error) return;

    mockServiceInfo.value.apisCors[key] = _params.apisCors[key];
    apisCors.value[key] = _params.apisCors[key];
    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Save security settings
   */
  const saveSureTy = async () => {
    if (!mockServiceInfo.value) return;

    const _params = {
      id: mockServiceInfo.value.id,
      apisSecurity: securityFormState.value.apisSecurity.length
        ? securityFormState.value.apisSecurity
        : []
    };

    loading.value = true;
    const [error] = await mock.patchService(_params);
    loading.value = false;

    if (error) {
      securityFormState.value.apisSecurity = mockServiceInfo.value.apisSecurity?.length
        ? mockServiceInfo.value.apisSecurity
        : [];
      isOpenSurety.value = !!mockServiceInfo.value.apisSecurity?.length;
      return;
    }

    notification.success(t('actions.tips.modifySuccess'));
  };

  /**
   * Add new security item
   */
  const addApisSecurityItem = () => {
    if (securityFormState.value.apisSecurity.length === 10) return;

    securityFormState.value.apisSecurity.push({
      keyName: '',
      value: '',
      in: 'header'
    });
  };

  /**
   * Delete security item by index
   */
  const delApisSecurityItem = (index: number) => {
    securityFormState.value.apisSecurity.splice(index, 1);
    if (!securityFormState.value.apisSecurity.length) {
      isOpenSurety.value = false;
    }
  };

  /**
   * Validate security key name for duplicates
   */
  const keNameValidator = async (_rule: Rule, value: string) => {
    if (!value) {
      return Promise.reject(new Error(t('mock.detail.validation.enterParamName')));
    }

    const keyNames = new Set();
    const hasDuplicates = securityFormState.value.apisSecurity.some(obj => {
      if (keyNames.has(obj.keyName)) {
        return true;
      }
      keyNames.add(obj.keyName);
      return false;
    });

    if (hasDuplicates) {
      return Promise.reject(new Error(t('mock.detail.validation.paramNameDuplicate')));
    }

    return Promise.resolve();
  };

  /**
   * Check if user has edit authorization
   */
  const hasEditAuth = computed(() => {
    if (!mockServiceInfo.value) return false;
    if (!mockServiceInfo.value.auth) return true;
    return !!mockServiceInfo.value?.currentAuths?.map(item => item.value).includes('MODIFY');
  });

  // Initialize on mount
  onMounted(async () => {
    editionType.value = appContext.getEditionType();
    await loadInfo();
  });

  return {
    // State
    infoFormState,
    securityFormState,
    apisCors,
    setting,
    mockServiceInfo,
    loading,
    editionType,
    isOpenSurety,

    // Edit flags
    editServiceDomain,
    editName,
    editCredentials,
    editOrigin,
    editRequestHeaders,
    editRequestMethods,
    editExposeHeaders,
    editWorkThreadNum,
    editMaxContentLength,
    editWorkPushbackThreadNum,
    editMaxPushbackConnectTimeout,
    editMaxPushbackRequestTimeout,

    // Options
    corsCookieOptions,
    inOptions,

    // Methods
    loadInfo,
    apiSuretyChange,
    handleEdit,
    settingFlagChange,
    apisCorsEnabledFlagChange,
    saveSureTy,
    addApisSecurityItem,
    delApisSecurityItem,
    keNameValidator,
    hasEditAuth
  };
}

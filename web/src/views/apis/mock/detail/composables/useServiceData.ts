import { onMounted, ref } from 'vue';
import { mock } from '@/api/tester';

import {
  CorsConfig,
  MockServiceInfo,
  SecurityForm,
  ServiceInfoForm,
  ServiceSettings
} from "@/views/apis/mock/detail/types";

/**
 * Composable for managing mock service data loading and state.
 * <p>
 * Handles fetching service details and initializing form states
 * for different configuration sections.
 */
export function useServiceData(serviceId: string) {
  const mockServiceInfo = ref<MockServiceInfo>();
  const loading = ref(false);

  // Form states for different sections
  const infoFormState = ref<ServiceInfoForm>({
    serviceDomainUrl: '',
    name: ''
  });

  const securityFormState = ref<SecurityForm>({
    apisSecurity: []
  });

  const corsConfig = ref<CorsConfig>({
    allowCorsCredentials: true,
    allowCorsOrigin: '*',
    allowCorsRequestHeaders: '',
    allowCorsRequestMethods: '',
    allowExposeHeaders: '',
    enabled: true
  });

  const settings = ref<ServiceSettings>({
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

  /**
   * Load service information and populate form states.
   */
  const loadServiceInfo = async () => {
    if (!serviceId) return;

    loading.value = true;
    const [error, { data }] = await mock.getServiceDetail(serviceId);
    loading.value = false;

    if (error) return;

    // Store original data for comparison and reset
    mockServiceInfo.value = JSON.parse(JSON.stringify(data));

    // Populate form states
    infoFormState.value.name = data.name;
    infoFormState.value.serviceDomainUrl = data.serviceDomainUrl;

    securityFormState.value.apisSecurity = data.apisSecurity?.length
      ? data.apisSecurity.map(item => ({ ...item, in: item.in.value }))
      : [];

    corsConfig.value = JSON.parse(JSON.stringify(data.apisCors));

    const settingsCopy = JSON.parse(JSON.stringify(data.setting));
    settings.value = {
      ...settingsCopy,
      logFileLevel: settingsCopy.logFileLevel.value
    };
  };

  onMounted(() => {
    loadServiceInfo();
  });

  return {
    mockServiceInfo,
    loading,
    infoFormState,
    securityFormState,
    corsConfig,
    settings,
    loadServiceInfo
  };
}

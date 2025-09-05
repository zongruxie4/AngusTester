import { ref } from 'vue';
import { mock } from '@/api/tester';

/**
 * Composable for managing Mock service information
 * Handles service details and URL options
 */
export function useMockServiceInfo () {
  // Service URL options for dropdown
  const serviceUrlOptions = ref<{ label: string; value: string; }[]>([]);

  /**
   * Load service information and populate URL options
   * @param serviceId - Mock service ID
   */
  const loadServiceInfo = async (serviceId: string) => {
    const [error, res] = await mock.getServiceDetail(serviceId);
    if (error || !res?.data) {
      return;
    }

    const { serviceDomainUrl, serviceHostUrl } = res.data;
    const options: { label: string; value: string; }[] = [];

    if (serviceDomainUrl) {
      options.push({ label: serviceDomainUrl, value: serviceDomainUrl });
    }

    if (serviceHostUrl) {
      options.push({ label: serviceHostUrl, value: serviceHostUrl });
    }

    serviceUrlOptions.value = options;
  };

  return {
    serviceUrlOptions,
    loadServiceInfo
  };
}

import { ref } from 'vue';
import { AuthObjectType, CreatedAt } from '@xcan-angus/infra';
import { MonitorInfo, NoticeSetting, ServerSetting, TimeSetting, EditFormState } from '@/views/scenario/monitor/types';

/**
 * Composable for managing form data state
 */
export function useFormData () {
  // Form state management
  const formState = ref<EditFormState>({
    scenarioId: undefined,
    description: '',
    name: ''
  });

  // Time setting configuration
  const timeSetting = ref<TimeSetting>({
    createdAt: CreatedAt.NOW
  });

  // Notice setting configuration
  const noticeSetting = ref<NoticeSetting>({
    enabled: true,
    orgType: AuthObjectType.USER,
    orgs: []
  });

  // Server setting configuration
  const serverSetting = ref<ServerSetting[]>([]);

  // Organization list for notice recipients
  const orgs = ref<{ name: string; id: string }[]>([]);

  /**
   * Set form data from monitor information
   * @param data - Monitor information data
   */
  const setFormData = (data: MonitorInfo | null) => {
    if (!data) {
      // Reset to default values
      formState.value = {
        scenarioId: undefined,
        description: '',
        name: ''
      };
      timeSetting.value = {
        createdAt: CreatedAt.NOW
      };
      noticeSetting.value = {
        enabled: true,
        orgType: AuthObjectType.DEPT,
        orgs: []
      };
      serverSetting.value = [];
      return;
    }

    const { scenarioId, description, name } = data;
    formState.value = {
      scenarioId,
      description,
      name
    };

    timeSetting.value = data.timeSetting || {
      createdAt: CreatedAt.NOW
    };

    noticeSetting.value = data.noticeSetting
      ? {
          ...data.noticeSetting,
          orgType: typeof data.noticeSetting.orgType === 'object'
            ? (data.noticeSetting.orgType as any).value
            : data.noticeSetting.orgType || AuthObjectType.USER,
          orgs: (data.noticeSetting.orgs || []).map((i: any) => ({
            id: i.id || i,
            name: i.name || i.fullName || ''
          }))
        }
      : {
          enabled: true,
          orgType: AuthObjectType.DEPT,
          orgs: []
        };

    serverSetting.value = data.serverSetting || [];
  };

  /**
   * Reset form data to initial state
   */
  const resetFormData = () => {
    setFormData(null);
  };

  return {
    formState,
    timeSetting,
    noticeSetting,
    serverSetting,
    orgs,
    setFormData,
    resetFormData
  };
}

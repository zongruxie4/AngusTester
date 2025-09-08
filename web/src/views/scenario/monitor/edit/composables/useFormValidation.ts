import { computed, ref } from 'vue';
import { NoticeSetting, ServerSetting, EditFormState } from '@/views/scenario/monitor/types';
import { CreateTimeSetting } from '@/types/types';

/**
 * Composable for form validation and parameter building
 */
export function useFormValidation () {
  const formRef = ref();
  const createdDateRef = ref();
  const activeTabKey = ref('time');

  /**
   * Check if form is in edit mode
   * @returns boolean
   */
  const isEditMode = computed(() => {
    return false; // This will be overridden in the component
  });

  /**
   * Build monitor parameters from form data
   * @param formState - Form state
   * @param timeSetting - Time setting
   * @param noticeSetting - Notice setting
   * @param serverSetting - Server setting
   * @param projectId - Project ID
   * @param dataId - Data ID (for edit mode)
   * @returns MonitorParams
   */
  const buildMonitorParams = (
    formState: EditFormState,
    timeSetting: CreateTimeSetting,
    noticeSetting: NoticeSetting,
    serverSetting: ServerSetting[],
    projectId: string,
    dataId?: string
  ): MonitorParams => {
    const { scenarioId, description, name } = formState;

    return {
      scenarioId,
      description,
      name,
      id: dataId || undefined,
      projectId,
      timeSetting: createdDateRef.value?.getData() || timeSetting,
      noticeSetting,
      serverSetting
    };
  };

  /**
   * Validate form and handle submission
   * @param onSubmit - Submit callback function
   * @param noticeSetting - Notice setting for validation
   * @param orgs - Organization list for validation
   */
  const validateAndSubmit = async (
    onSubmit: () => Promise<void>,
    noticeSetting: NoticeSetting,
    orgs: { name: string; id: string }[]
  ) => {
    // Check if notification is enabled but no organizations selected
    if (!orgs.length && noticeSetting?.enabled) {
      activeTabKey.value = 'notice';
      return;
    }

    try {
      await formRef.value?.validate();
      await onSubmit();
    } catch (error) {
      console.error('Form validation failed:', error);
    }
  };

  /**
   * Switch to specific tab
   * @param tabKey - Tab key to switch to
   */
  const switchToTab = (tabKey: string) => {
    activeTabKey.value = tabKey;
  };

  return {
    formRef,
    createdDateRef,
    activeTabKey,
    isEditMode,
    buildMonitorParams,
    validateAndSubmit,
    switchToTab
  };
}

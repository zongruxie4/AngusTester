import { ref } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { exec } from '@/api/ctrl';
import type { UseConfigData } from '../types';
import { useI18n } from 'vue-i18n';

/**
 * Composable for managing configuration data in execution detail
 * Handles saving settings and form validation
 *
 * @param execId - The execution ID for saving configuration
 * @param execName - The execution name
 * @param scriptInfo - Script information object
 * @param loading - Loading state getter function
 * @param emit - Vue emit function for component events
 * @returns Configuration data management functions and reactive state
 */
export const useConfigData = (execId: string, execName: string, scriptInfo: any, loading: any, emit: any) => {
  const { t } = useI18n();
  // Reference to the execution setting form component
  const execSettingFormRef = ref();

  /**
   * Scroll to the first error element in a form
   * Used to automatically scroll to validation errors
   *
   * @param formName - CSS class name of the form
   * @param errors - Validation errors object
   */
  const scrollToErrorElement = (formName: string, errors: any) => {
    // Find the form element in the DOM
    const formElement = document.querySelector(`.${formName}`);
    if (!formElement || !errors[formName]?.errorFields?.length) {
      return;
    }

    // Get the class name of the first error field
    const errEleClass = errors[formName].errorFields[0].name.join('-');
    const errorElement = formElement.querySelector(`.${errEleClass}`);
    if (!errorElement) {
      return;
    }

    // Scroll to the error element
    errorElement.scrollIntoView({
      block: 'center',
      inline: 'start'
    });
  };

  /**
   * Save execution settings
   * Validates form and sends data to API
   * Shows success notification on successful save
   */
  const saveSetting = async () => {
    // Validate form data
    let hasErr = true;
    const data = await execSettingFormRef.value.isValid();
    hasErr = data.valid;
    const errors = data.errors;

    // Form names to check for validation errors
    const formNames = ['execut-form', 'global-form', 'http-form', 'websocket-form', 'jdbc-form'];

    // Check each form for validation errors
    for (const formName of formNames) {
      if (errors[formName]?.errorFields?.length) {
        // Open the appropriate parameter section based on form type
        if (formName === 'execut-form') {
          execSettingFormRef.value.openExecutParames();
        }

        if (formName === 'global-form') {
          execSettingFormRef.value.openGlobalParames();
        }

        if (formName === 'http-form' || formName === 'websocket-form' || formName === 'jdbc-form') {
          execSettingFormRef.value.openPulginParames();
        }

        // Scroll to the first error
        scrollToErrorElement(formName, errors);
        break;
      }
    }

    // If there are validation errors, stop execution
    if (!hasErr) {
      return;
    }

    // Prepare parameters for API call
    const params: any = {
      name: execName,
      scriptType: scriptInfo.type,
      ...execSettingFormRef.value.getData()
    };

    // Show loading state and call API
    emit('update:loading', true);
    const [error] = await exec.putScriptConfig(execId, params);
    emit('update:loading', false);

    // Handle API error
    if (error) {
      return;
    }

    // Show success notification
    notification.success(t('actions.tips.modifySuccess'));
  };

  // Return reactive state and functions
  return {
    execSettingFormRef,
    saveSetting,
    scrollToErrorElement
  } as UseConfigData;
};

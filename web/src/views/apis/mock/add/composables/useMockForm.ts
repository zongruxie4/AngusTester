/**
 * Composable for managing mock service form state and validation
 * Handles form data, validation rules, and form submission logic
 */
import { computed, ref } from 'vue';
import type { RuleObject } from 'ant-design-vue/es/form';
import { useI18n } from 'vue-i18n';
import { MockServiceForm } from '../types';

/**
 * Composable for managing mock service form state and validation
 * @param isPrivate - Whether the application is in private edition mode
 * @param activeTab - Current active tab index
 * @returns Form state, validation rules, and helper functions
 */
export function useMockForm (isPrivate: boolean, activeTab: number) {
  const { t } = useI18n();

  // Form state
  const formState = ref<MockServiceForm>({
    name: '',
    serviceDomain: '',
    servicePort: '',
    nodeId: undefined,
    serviceId: '',
    apiIds: [],
    importType: 'OpenAPI',
    file: undefined,
    text: ''
  });

  // Domain validation regex
  const domainRegex = /^(?=.{1,253}$)([a-z0-9]|[a-z0-9][a-z0-9-]{0,61}[a-z0-9])\.angusmock\.cloud$/;

  /**
   * Validate service domain
   * @param _rule - Validation rule
   * @param value - Domain value to validate
   * @returns Promise resolving or rejecting based on validation result
   */
  const validateServiceDomain = async (_rule: RuleObject, value: string): Promise<void> => {
    if (!value) {
      return Promise.reject(new Error(t('mock.addMock.validation.enterDomain')));
    } else if (!domainRegex.test(value + '.angusmock.cloud')) {
      return Promise.reject(new Error(t('mock.addMock.validation.enterCorrectDomain')));
    } else {
      return Promise.resolve();
    }
  };

  /**
   * Validate required field
   * @param messageKey - Translation key for the validation message
   * @returns Validation rule object
   */
  const createRequiredRule = (messageKey: string): RuleObject => {
    return {
      required: true,
      message: t(messageKey),
      trigger: 'change'
    };
  };

  /**
   * Create domain validation rule
   * @returns Domain validation rule object
   */
  const createDomainRule = (): RuleObject => {
    return {
      required: true,
      message: '', // Message handled in validator
      trigger: 'change',
      validator: validateServiceDomain
    };
  };

  // Form validation rules
  const rules = computed(() => {
    // Base rules applicable to all forms
    const baseRules = {
      name: [createRequiredRule('mock.addMock.validation.enterName')],
      servicePort: [createRequiredRule('mock.addMock.validation.enterPort')],
      nodeId: [createRequiredRule('mock.addMock.validation.selectNode')]
    };

    // Rules for private edition (no domain required)
    const privateRules = {
      ...baseRules
    };

    // Rules for public edition (domain required)
    let publicRules: Record<string, RuleObject | RuleObject[]> = {
      ...baseRules,
      serviceDomain: [createDomainRule()]
    };

    // Additional rules for service association tab
    if (activeTab === 1) {
      publicRules = {
        ...publicRules,
        serviceId: [createRequiredRule('mock.addMock.validation.selectProjectService')]
      };
    }

    return isPrivate ? privateRules : publicRules;
  });

  /**
   * Check if file is provided (for file upload tab)
   * @returns True if no file or text content is provided, false otherwise
   */
  const isFileMissing = (): boolean => {
    return !formState.value.file && !formState.value.text;
  };

  /**
   * Reset form state for serviceId when changing tabs
   */
  const resetServiceId = (): void => {
    formState.value.serviceId = '';
  };

  /**
   * Clear text content
   */
  const clearTextContent = (): void => {
    formState.value.text = '';
  };

  return {
    formState,
    rules,
    checkFile: isFileMissing,
    resetServiceId,
    clearTextContent
  };
}

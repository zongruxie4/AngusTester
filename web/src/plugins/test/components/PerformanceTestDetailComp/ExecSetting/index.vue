
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { notification, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { exec } from '@/api/tester';

import ExecSettingForm from '@/components/exec/config/index.vue';

// Router instance for navigation
const router = useRouter();

/**
 * Script information interface
 */
interface ScriptInfo {
  id: string;        // Script identifier
  [key: string]: any; // Additional script properties
}

/**
 * Form validation error field interface
 */
interface ErrorField {
  name: string[];    // Field name path
  errors: string[];  // Validation error messages
}

/**
 * Form validation errors interface
 */
interface FormErrors {
  errorFields?: ErrorField[];  // Array of error fields
}

/**
 * Validation result interface
 */
interface ValidationResult {
  valid: boolean;                              // Whether validation passed
  errors: Record<string, FormErrors>;          // Errors grouped by form name
}

/**
 * Component props interface
 */
interface Props {
  execId: string;          // Execution unique identifier
  scriptInfo: ScriptInfo;  // Script configuration information
  loading: boolean;        // Loading state during save operation
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  execId: '',
  scriptInfo: undefined,
  loading: false
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:loading', value: boolean): void;  // Emit loading state changes
}>();

/**
 * Reference to the ExecSettingForm component
 */
const execSettingFormRef = ref();

/**
 * Scroll to the first error element in a form
 * Finds the form by class name, locates the first error field, and scrolls it into view
 *
 * @param formName - Form class name to search for
 * @param errors - Validation errors object containing error fields
 */
const scrollToErrorElement = (
  formName: string,
  errors: Record<string, FormErrors>
): void => {
  // Find form element by class name
  const formElement = document.querySelector(`.${formName}`);

  // Early return if form not found or no errors
  if (!formElement || !errors[formName]?.errorFields?.length) {
    return;
  }

  // Build error element class name from field path
  const errEleClass = errors[formName].errorFields![0].name.join('-');

  // Find error element within form
  const errorElement = formElement.querySelector(`.${errEleClass}`);

  // Early return if error element not found
  if (!errorElement) {
    return;
  }

  // Scroll error element into view, centered vertically
  errorElement.scrollIntoView({
    block: 'center',
    inline: 'start'
  });
};

/**
 * Form names used for validation
 */
const FORM_NAMES = [
  'execut-form',     // Execution parameters form
  'global-form',     // Global parameters form
  'http-form',       // HTTP plugin form
  'websocket-form',  // WebSocket plugin form
  'jdbc-form'        // JDBC plugin form
] as const;

/**
 * Save execution settings
 * Validates all forms, expands sections with errors, scrolls to errors,
 * and saves configuration to API if valid
 */
const saveSetting = async (): Promise<void> => {
  // Validate all forms
  const data: ValidationResult = await execSettingFormRef.value.isValid();
  const isValid = data.valid;
  const errors = data.errors;

  // Check each form for validation errors
  for (const formName of FORM_NAMES) {
    if (errors[formName]?.errorFields?.length) {
      // Expand the section containing errors
      if (formName === 'execut-form') {
        execSettingFormRef.value.openExecutParames();
      }

      if (formName === 'global-form') {
        execSettingFormRef.value.openGlobalParames(true);
      }

      if (formName === 'http-form' || formName === 'websocket-form' || formName === 'jdbc-form') {
        execSettingFormRef.value.openPulginParames(true);
      }

      // Scroll to first error in this form
      scrollToErrorElement(formName, errors);
      break; // Stop at first error
    }
  }

  // Early return if validation failed
  if (!isValid) {
    return;
  }

  // Get form data
  const params = {
    ...execSettingFormRef.value.getData()
  };

  // Save to API
  emit('update:loading', true);
  const [error] = await exec.putExecScriptConfig(props.execId, params.value);
  emit('update:loading', false);

  // Handle error
  if (error) {
    return;
  }

  // Show success message and navigate to execution list
  notification.success(t('actions.tips.modifySuccess'));
  router.push('/execution');
};
</script>

<template>
  <!-- Spinning wrapper for loading state during save -->
  <Spin
    ref="exexFormRef"
    :spinning="loading"
    class="h-full overflow-y-auto py-3.5 px-8 w-full">
    <!-- Content container with max width -->
    <div style="max-width: 1440px;">
      <!-- Execution settings form component -->
      <ExecSettingForm
        ref="execSettingFormRef"
        :scriptId="props.scriptInfo.id"
        :scriptInfo="props.scriptInfo" />

      <!-- Action buttons: Save and Cancel -->
      <div class="flex pl-3.5 mt-10 pb-8">
        <!-- Save button (validates and saves settings) -->
        <Button
          size="small"
          type="primary"
          class="mr-5"
          @click="saveSetting">
          {{ t('actions.save') }}
        </Button>

        <!-- Cancel button (navigates back to execution list) -->
        <RouterLink to="/execution">
          <Button size="small">{{ t('actions.cancel') }}</Button>
        </RouterLink>
      </div>
    </div>
  </Spin>
</template>

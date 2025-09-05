import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import YAML from 'yaml';
import { notification } from '@xcan-angus/vue-ui';

/**
 * Composable for YAML editor functionality
 * Handles YAML parsing, validation, and data conversion
 */
export function useYamlEditor () {
  const { t } = useI18n();
  const content = ref('');
  const loading = ref(true);

  /**
   * Convert object to YAML string
   * @param value - Object to convert
   */
  const convertToYaml = (value: { [key: string]: any } | undefined) => {
    if (!value) {
      return;
    }

    try {
      content.value = YAML.stringify(value);
    } catch (error) {
      content.value = JSON.stringify(value, null, 2);
    }
  };

  /**
   * Validate YAML content
   * @returns True if YAML is valid, false otherwise
   */
  const isValid = (): boolean => {
    try {
      YAML.parse(content.value);
      return true;
    } catch (error) {
      notification.error(t('mock.mockApis.codeForm.notifications.yamlFormatError'));
      return false;
    }
  };

  /**
   * Get parsed data from YAML content
   * @returns Parsed object data
   */
  const getData = (): { [key: string]: any } => {
    return YAML.parse(content.value);
  };

  return {
    content,
    loading,
    convertToYaml,
    isValid,
    getData
  };
}

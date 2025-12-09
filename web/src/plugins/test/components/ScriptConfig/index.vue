<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import YAML from 'yaml';
import { notification, MonacoEditor } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { ScenarioConfig } from '@/plugins/test/types/index';

/**
 * Component props interface
 */
export interface Props {
  value: ScenarioConfig['script'] | string;  // Script configuration object to display
  language?: string;
  readonly?: boolean;
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  language: 'yaml',
  readonly: true
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Editor content in YAML format
 */
const content = ref('');

/**
 * Monaco Editor loading state
 */
const loading = ref(true);

/**
 * Component mount lifecycle hook
 * Sets up watcher to convert script configuration to YAML format
 */
onMounted(() => {
  /**
   * Watch for value prop changes
   * Converts script configuration object to YAML string for editor display
   * Falls back to JSON format if YAML conversion fails
   */
  watch(() => props.value, (newValue) => {
    // Early return if no value provided
    if (!newValue) {
      return;
    }
    if (props.language === 'yaml') {
      try {
        // Try to convert to YAML format (preferred)
        content.value = YAML.stringify(newValue);
      } catch (error) {
        // Fallback to JSON format if YAML conversion fails
        content.value = JSON.stringify(newValue, null, 2);
      }
    } else {
      content.value = props.value as string;
    }

  }, { 
    immediate: true,  // Execute immediately on mount
    deep: true        // Watch nested properties
  });
});

/**
 * Validate YAML content
 * Attempts to parse the current editor content as YAML
 * Shows error notification if parsing fails
 * 
 * @returns true if content is valid YAML, false otherwise
 */
const isValid = (): boolean => {
  if (props.language === 'yaml') {
    try {
      YAML.parse(content.value);
      return true;
    } catch (error) {
      notification.error(t('ftpPlugin.scriptConfig.messages.yamlFormatError'));
      return false;
    }
  }
  return true
};

/**
 * Get parsed configuration data
 * Parses the current YAML content and returns as JavaScript object
 * Note: Should call isValid() first to ensure content is valid YAML
 * 
 * @returns Parsed configuration object
 */
const getData = (): { [key: string]: any }|string => {
  if (props.language === 'yaml') {
    return YAML.parse(content.value);
  }
  return content.value as string;
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  isValid,
  getData
});
</script>

<template>
  <!--
    Monaco Editor for YAML configuration display
    - Read-only mode to prevent editing
    - Full width and height with vertical padding
    - YAML syntax highlighting
    - Two-way binding for loading state and content
  -->
  <MonacoEditor
    v-model:loading="loading"
    v-model:value="content"
    :readOnly="props.readonly"
    class="w-full h-full py-3"
    :language="props.language" />
</template>

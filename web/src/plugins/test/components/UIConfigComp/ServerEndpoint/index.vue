<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Tooltip, Popover } from '@xcan-angus/vue-ui';
import { Radio } from 'ant-design-vue';
import { ServerInfo } from '@/views/apis/server/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  server: ServerInfo; // Server configuration with URL, variables, and description
  endpoint: string; // API endpoint path
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  server: undefined,
  endpoint: undefined
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'change', value: ServerInfo): void; // Emit server configuration changes
}>();

/**
 * Computed array of server variables
 * Converts variables object to array format for rendering
 *
 * @returns Array of variable objects with name and properties
 */
const variables = computed(() => {
  if (!props.server?.variables) {
    return [];
  }

  return Object.entries(props.server.variables).map(([_name, value]) => {
    return {
      _name,
      ...value
    };
  });
});

/**
 * Computed server URL
 * @returns Server base URL or undefined
 */
const serverUrl = computed(() => {
  return props.server?.url;
});

/**
 * Computed server description
 * @returns Server description text or undefined
 */
const description = computed(() => {
  return props.server?.description;
});

/**
 * Handle variable default value change
 * Updates the selected variable's default value and emits change
 *
 * @param key - Variable name/key
 * @param value - New default value
 */
const radioChange = (key: string, value: string): void => {
  // Deep clone server data to avoid mutation
  const data: ServerInfo = JSON.parse(JSON.stringify(props.server));

  // Update variable default value
  if (data?.variables?.[key]) {
    data.variables[key].defaultValue = value;
  }

  emit('change', data);
};

/**
 * Popover overlay style configuration
 */
const overlayStyle = {
  minWidth: '300px',
  maxWidth: '450px'
};
</script>

<template>
  <!--
    Composite container showing server URL and endpoint
    - Flexbox layout with 40% server URL, 60% endpoint
    - White background with rounded corners
    - Fixed height (28px)
  -->
  <div class="flex-1 flex items-center h-7 leading-7 bg-white rounded composite-container">
    <!-- Server URL section with variable configuration popover -->
    <Popover
      :overlayStyle="overlayStyle"
      placement="bottomLeft">
      <!-- Popover content: variable configuration list -->
      <template #content>
        <template v-if="variables">
          <!-- Individual variable configuration -->
          <div
            v-for="item in variables"
            :key="item._name"
            class="leading-5 text-3 space-y-1 mb-3">
            <!-- Variable name header -->
            <div class="flex items-center space-x-2">
              <div class="w-1 h-1 rounded-lg bg-slate-600"></div>
              <div class="text-theme-title font-bold">{{ item._name }}</div>
            </div>

            <!-- Variable allowable values list -->
            <div class="text-theme-content space-y-0.5">
              <div
                v-for="_ele in item.allowableValues"
                :key="_ele"
                class="flex items-center pl-7">
                <!-- Value text (truncated) -->
                <div class="flex-1 truncate">{{ _ele }}</div>

                <!-- Radio button with default label -->
                <div class="flex items-center flex-shrink-0 space-x-1">
                  <!-- "Default" label for currently selected value -->
                  <div
                    v-if="props.server?.variables?.[item._name]?.defaultValue === _ele"
                    class="text-theme-sub-content">
                    <span>{{ t('common.default') }}</span>
                  </div>

                  <!-- Radio button for value selection -->
                  <Radio
                    :checked="props.server?.variables?.[item._name]?.defaultValue === _ele"
                    @change="radioChange(item._name, _ele)" />
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>

      <!-- Popover title: server URL and description -->
      <template #title>
        <div class="leading-4 py-2 space-y-2">
          <div class="text-theme-title font-bold break-all">{{ serverUrl }}</div>
          <div class="text-theme-content break-all">{{ description }}</div>
        </div>
      </template>

      <!-- Server URL display (40% width, clickable to open popover) -->
      <div style="flex:1 1 40%;" class="truncate px-1.75 text-3">
        {{ serverUrl }}
      </div>
    </Popover>

    <!-- Vertical divider between server URL and endpoint -->
    <div class="w-0 h-3.5 border-r border-solid border-theme-divider"></div>

    <!-- Endpoint path section with tooltip -->
    <Tooltip placement="topLeft">
      <!-- Tooltip content: full endpoint path -->
      <template #title>{{ props.endpoint }}</template>

      <!-- Endpoint path display (60% width, truncated) -->
      <div style="flex:1 1 60%;" class="truncate px-1.75 text-3">
        {{ props.endpoint }}
      </div>
    </Tooltip>
  </div>
</template>

<style scoped>
.composite-container {
  transition: all 300ms linear 0ms;
  background-color: #f5f5f5;
}
</style>

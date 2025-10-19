<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { FormItem, Form } from 'ant-design-vue';
import { useRoute } from 'vue-router';
import { Icon, Input, Grid } from '@xcan-angus/vue-ui';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { useI18n } from 'vue-i18n';

/**
 * Script information interface
 */
interface ScriptInfo {
  name: string;                      // Script name
  plugin?: string;                   // Associated plugin name
  type?: {                           // Script type enum
    value: string;
    message: string;
  };
  description: string;               // Script description
  source?: {                         // Script source enum
    value: string;
    message: string;
  };
  createdByName?: string;            // Creator username
  createdDate?: string;              // Creation timestamp
  lastModifiedDate?: string;         // Last modification timestamp
  lastModifiedByName?: string;       // Last modifier username
}

/**
 * Component props interface
 */
interface Props {
  scriptInfo: ScriptInfo | undefined;  // Script metadata object
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  scriptInfo: undefined
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * View mode type: info (read-only), edit (modify), or add (create)
 */
type ViewMode = 'info' | 'edit' | 'add';

// Get current route information
const route = useRoute();

/**
 * Current view mode (synced with route query parameter)
 */
const type = ref<ViewMode>(route.query.type as ViewMode);

/**
 * Initial values for form reset when switching modes
 */
const initName = ref<string>();           // Initial name value
const initDescription = ref<string>();    // Initial description value

/**
 * Watch for route query type changes
 * Switches between view modes and preserves/restores form data
 */
watch(() => route.query.type, (newValue) => {
  type.value = newValue as ViewMode;

  if (newValue !== 'info') {
    // Entering edit/add mode: save current values for potential rollback
    initName.value = formData.name;
    initDescription.value = formData.description;
  } else {
    // Returning to info mode: restore initial values (canceling edits)
    formData.name = initName.value || '';
    formData.description = initDescription.value || '';
  }
});

/**
 * Panel expansion state
 */
const isSpread = ref(true);

/**
 * Form instance reference for validation
 */
const formRef = ref();

/**
 * Form data model
 * Contains both editable fields and read-only metadata
 */
const formData = reactive({
  name: '',                      // Script name (editable)
  plugin: '',                    // Plugin name (read-only)
  type: undefined as string | undefined,  // Script type value (editable)
  description: '',               // Script description (editable)
  source: '',                    // Source value (read-only)
  createdBy: '',                 // Creator username (read-only)
  exec: '',                      // Execution info (unused)
  createdDate: '',               // Creation timestamp (read-only)
  lastModifiedDate: '',          // Last modification timestamp (read-only)
  lastModifiedByName: '',        // Last modifier username (read-only)
  typeName: '',                  // Script type display name (read-only)
  sourceName: ''                 // Source display name (read-only)
});

/**
 * Grid column configuration for info view mode
 * Defines the layout and field mapping for displaying script metadata
 */
const infoConfig = [[
  {
    label: t('common.scriptName'),
    dataIndex: 'name'
  },
  {
    label: t('common.scriptType'),
    dataIndex: 'typeName'
  },
  {
    label: t('common.plugin'),
    dataIndex: 'plugin'
  },
  {
    label: t('common.source'),
    dataIndex: 'sourceName'
  },
  {
    label: t('common.creator'),
    dataIndex: 'createdBy'
  },
  {
    label: t('common.createdDate'),
    dataIndex: 'createdDate'
  },
  {
    label: t('common.modifier'),
    dataIndex: 'lastModifiedByName'
  },
  {
    label: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate'
  },
  {
    label: t('common.description'),
    dataIndex: 'description'
  }
]];

/**
 * Validate form fields
 * Triggers Ant Design form validation
 * 
 * @returns Promise that resolves with validation result
 */
const validate = () => {
  return formRef.value.validate();
};

/**
 * Get editable form data
 * Extracts only the fields that can be modified
 * 
 * @returns Object containing name, type, and description
 */
const getFormData = () => {
  const { name, type, description } = formData;
  return { name, type, description };
};

/**
 * Toggle panel expansion state
 * Expands or collapses the script info panel
 */
const toggle = (): void => {
  isSpread.value = !isSpread.value;
};

/**
 * Watch for scriptInfo prop changes
 * Updates form data when new script information is provided
 */
watch(() => props.scriptInfo, (newValue) => {
  if (newValue) {
    // Populate form with script data (with default empty strings for optional fields)
    formData.name = newValue.name;
    formData.plugin = newValue?.plugin || '';
    initName.value = newValue.name;
    formData.type = newValue.type?.value;
    formData.typeName = newValue.type?.message || '';
    formData.description = newValue.description;
    formData.source = newValue.source?.value || '';
    formData.sourceName = newValue.source?.message || '';
    formData.createdBy = newValue.createdByName || '';
    formData.createdDate = newValue.createdDate || '';
    formData.lastModifiedDate = newValue.lastModifiedDate || '';
    formData.lastModifiedByName = newValue.lastModifiedByName || '';
  }
}, {
  deep: true,      // Watch nested properties
  immediate: true  // Execute immediately on mount
});

/**
 * Expose methods and properties for parent component access
 */
defineExpose({ getFormData, validate, isSpread, toggle });
</script>

<template>
  <!-- Main container with relative positioning for toggle icon -->
  <div class="relative pt-2 pr-3.5">
    <!-- Content area (shown when panel is expanded) -->
    <template v-if="isSpread">
      <!-- Edit/Add mode: Display form with validation -->
      <Form
        v-if="type !== 'info'"
        ref="formRef"
        :model="formData"
        layout="vertical"
        size="small">
        <!-- Script type field (required) -->
        <FormItem
          :label="t('common.scriptType')"
          name="type"
          :rules="[{ required: true, message: t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptTypeRequired') }]">
          <SelectEnum
            v-model:value="formData.type"
            enumKey="ScriptType"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptTypePlaceholder')" />
        </FormItem>
        
        <!-- Script name field (required, max 200 chars) -->
        <FormItem
          :label="t('common.scriptName')"
          name="name"
          size="small"
          :rules="[{ required: true, message: t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptNameRequired') }]">
          <Input
            v-model:value="formData.name"
            :maxlength="200"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.scriptNamePlaceholder')" />
        </FormItem>
        
        <!-- Description field (required, max 800 chars, textarea) -->
        <FormItem
          :label="t('common.description')"
          name="description"
          :rules="[{ required: true, message: t('ftpPlugin.performanceTestDetail.scriptInfo.form.descriptionRequired') }]">
          <Input
            v-model:value="formData.description"
            type="textarea"
            :placeholder="t('ftpPlugin.performanceTestDetail.scriptInfo.form.descriptionPlaceholder')"
            :showCount="!!formData.description"
            :autoSize="{ minRows: 4, maxRows: 6 }"
            :maxlength="800" />
        </FormItem>
      </Form>
      
      <!-- Info mode: Display read-only grid -->
      <Grid
        v-else
        :columns="infoConfig"
        :dataSource="formData"
        marginBottom="18px"
        fontSize="12px">
        <!-- Custom plugin field rendering with badge style -->
        <template #plugin="{ text }">
          <template v-if="text">
            <!-- Plugin badge with blue background -->
            <span 
              class="px-3 py-0.5 rounded-xl" 
              style="background-color: rgba(0, 119, 255, 10%); color: rgba(0, 119, 255, 100%)">
              {{ text }}
            </span>
          </template>
          <template v-else>
            --
          </template>
        </template>
      </Grid>
    </template>
    
    <!-- Toggle icon for expand/collapse functionality -->
    <div
      class="switch-icon"
      :class="{ '-left-3.25 transform rotate-180': !isSpread, '-left-0.75': isSpread }"
      @click="toggle">
      <!-- Collapsed state: show left arrow -->
      <template v-if="isSpread">
        <Icon class="icon" icon="icon-zhediejiantouyou" />
      </template>
      <!-- Expanded state: show right arrow (rotated) -->
      <template v-else>
        <Icon class="icon" icon="icon-zhediejiantouzuo" />
      </template>
    </div>
  </div>
</template>

<style scoped>
/**
 * Toggle icon container
 * Positioned absolutely at bottom-left with dynamic horizontal position
 * based on expansion state
 */
.switch-icon {
  @apply absolute bottom-20 h-12.5 cursor-pointer overflow-hidden select-none;

  z-index: 1;
}

/**
 * Icon within toggle button
 * Centered both horizontally and vertically within container
 */
.switch-icon .icon {
  @apply absolute left-1 top-1/2;

  transform: translateX(-50%) translateY(-50%);
}
</style>

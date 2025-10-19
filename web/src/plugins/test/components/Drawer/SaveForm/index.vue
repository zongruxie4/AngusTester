
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TreeSelect } from 'ant-design-vue';
import { modules } from '@/api/tester';
import { Input, IconRequired, IconCopy, Icon } from '@xcan-angus/vue-ui';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Form data interface for save operation
 */
export interface SaveFormData {
  name: string;         // Name of the item
  description: string;  // Description of the item
  moduleId?: string;    // Optional module ID for categorization
}

/**
 * Component props interface
 */
export interface Props {
  value: {
    id: string;              // Unique identifier
    name: string;            // Item name
    description: string;     // Item description
    scriptId?: string;       // Optional script identifier
    scriptName?: string;     // Optional script name
  };
  projectId: string;         // Project identifier for loading modules
  loading: boolean;          // Loading state for save button
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loading: false,
  projectId: ''
});

/**
 * Event emitters for save and cancel actions
 */
const emit = defineEmits<{
  (e: 'save', value: SaveFormData): void;
  (e: 'cancel'): void;
}>();

/**
 * Form field reactive references
 */
const name = ref<string>();              // Item name
const nameError = ref(false);            // Name validation error state
const description = ref<string>();       // Item description
const moduleId = ref<string>();          // Selected module ID

/**
 * Handle name input change
 * Clears validation error when user starts typing
 * @param event - Input change event
 */
const nameChange = (event: { target: { value?: string; } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
};

/**
 * Handle description input change
 * @param event - Input change event
 */
const descriptionChange = (event: { target: { value?: string; } }) => {
  const value = event.target.value;
  description.value = value;
};

/**
 * Validate form fields
 * Currently only validates that name is not empty
 * @returns true if form is valid, false otherwise
 */
const isValid = (): boolean => {
  nameError.value = false;
  let errorNum = 0;
  
  // Name is required
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  }

  return !errorNum;
};

/**
 * Get form data for submission
 * @returns Form data object with name, description, and moduleId
 */
const getData = (): SaveFormData => {
  return {
    name: name.value || '',
    description: description.value || '',
    moduleId: moduleId.value
  };
};

/**
 * Handle save button click
 * Validates form and emits save event if valid
 */
const save = () => {
  if (!isValid()) {
    return;
  }

  emit('save', getData());
};

/**
 * Handle cancel button click
 * Resets form and emits cancel event
 */
const cancel = () => {
  emit('cancel');
  init();
};

/**
 * Module Management
 */

// Module tree data for TreeSelect component
const moduleTreeData = ref<any[]>([]);

/**
 * Load module tree data from API
 * Fetches hierarchical module structure for the current project
 */
const getModuleTreeData = async () => {
  if (!props.projectId) {
    return;
  }
  
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  
  if (error) {
    return;
  }
  
  moduleTreeData.value = data || [];
};

/**
 * Initialize form with prop values
 * Resets validation state and populates form fields
 */
const init = () => {
  nameError.value = false;
  const data = props.value;
  
  if (!data) {
    return;
  }

  const { name: _name, description: _description } = data;
  name.value = _name;
  description.value = _description;
};

/**
 * Component lifecycle hook
 * Sets up watcher for prop changes and loads module data
 */
onMounted(() => {
  // Watch for value prop changes and reinitialize form
  watch(() => props.value, () => {
    init();
  }, { immediate: true });

  // Load module tree data on mount
  getModuleTreeData();
});

/**
 * Computed Properties
 */

/**
 * Get script ID from props
 * @returns Script ID or empty string
 */
const scriptId = computed(() => {
  return props.value?.scriptId || '';
});

/**
 * Get script name from props
 * @returns Script name or empty string
 */
const scriptName = computed(() => {
  return props.value?.scriptName || '';
});

/**
 * Get item ID (only if script ID exists)
 * @returns Item ID or empty string
 */
const id = computed(() => {
  if (!scriptId.value) {
    return '';
  }

  return props.value?.id;
});

/**
 * Expose methods for parent component access
 */
defineExpose({ isValid, getData });
</script>

<template>
  <!-- Main form container -->
  <div class="space-y-4 leading-5">
    <!-- ID field (displayed only when script ID exists) -->
    <div v-if="id" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.id') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="id" class="truncate">{{ id }}</span>
        <IconCopy class="flex-shrink-0" :copyText="id" />
      </div>
    </div>

    <!-- Name field (required) -->
    <div class="space-y-0.5">
      <div class="flex items-center">
        <IconRequired />
        <span>{{ t('common.name') }}</span>
      </div>
      <Input
        :maxlength="200"
        :value="name"
        :error="nameError"
        :placeholder="t('common.placeholders.searchKeyword')"
        trim
        @change="nameChange" />
    </div>

    <!-- Script name field (displayed only when available) -->
    <div v-if="scriptName" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.scriptName') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="scriptName" class="truncate">{{ scriptName }}</span>
        <IconCopy class="flex-shrink-0" :copyText="scriptName" />
      </div>
    </div>

    <!-- Script ID field (displayed only when available) -->
    <div v-if="scriptId" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.scriptId') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="scriptId" class="truncate">{{ scriptId }}</span>
        <IconCopy class="flex-shrink-0" :copyText="scriptId" />
      </div>
    </div>

    <!-- Module selection field -->
    <div class="space-y-0.5">
      <div class="flex items-center">{{ t('common.module') }}</div>
      <TreeSelect
        v-model:value="moduleId"
        dropdownClassName="module_tree"
        :treeData="moduleTreeData"
        :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
        :virtual="false"
        class="w-full"
        size="small"
        showSearch
        allowClear
        :placeholder="t('common.placeholders.selectOrSearchModule')">
        <!-- Custom template for tree node display -->
        <template #title="item">
          <div class="flex items-center" :title="item.name">
            <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
            <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
          </div>
        </template>
      </TreeSelect>
    </div>

    <!-- Description field (textarea with character counter) -->
    <div class="space-y-0.5">
      <div class="flex items-center">{{ t('common.description') }}</div>
      <Input
        :maxlength="800"
        :value="description"
        :autoSize="{ minRows: 5, maxRows: 5 }"
        showCount
        type="textarea"
        :placeholder="t('httpPlugin.drawerItem.descriptionPlaceholder')"
        trim
        @change="descriptionChange" />
    </div>

    <!-- Action buttons: Confirm and Cancel -->
    <div class="flex items-center space-x-2 !mt-5">
      <!-- Confirm button (validates and saves form) -->
      <Button
        type="primary"
        size="small"
        :loading="props.loading"
        @click="save">
        {{ t('actions.confirm') }}
      </Button>

      <!-- Cancel button (resets form and closes drawer) -->
      <Button
        type="default"
        size="small"
        @click="cancel">
        {{ t('actions.cancel') }}
      </Button>
    </div>
  </div>
</template>

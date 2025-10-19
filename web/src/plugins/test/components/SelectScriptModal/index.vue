/**
<script setup lang="ts">
import { computed, ref, onMounted, watch, inject, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox } from 'ant-design-vue';
import { Modal, Scroll, Input, Icon, notification } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, TESTER, SearchCriteria } from '@xcan-angus/infra';
import YAML from 'yaml';
import { script } from '@/api/tester';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Script data item interface
 */
type DataItem = {
  id: string;                              // Script unique identifier
  name: string;                            // Script name
  plugin: string;                          // Associated plugin name
  type: { value: string; message: string; }; // Script type enum
}

/**
 * Component props interface
 */
export interface Props {
  visible: boolean;  // Modal visibility state
  plugin: string;    // Plugin filter for script list
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  visible: false
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;  // Update modal visibility
  (e: 'ok', value: any): void;                  // Emit selected script data
  (e: 'cancel'): void;                          // Emit cancel event
}>();

/**
 * Inject project ID from parent context
 * Used for filtering scripts by project
 */
const projectId = inject<Ref<string>>('projectId', ref(''));

/**
 * State Management
 */
const inputValue = ref<string>();       // Search input value
const checkedId = ref<string>();        // Currently selected script ID
const dataList = ref<DataItem[]>([]);   // Visible script list (from scroll component)
const loading = ref(false);             // Loading state during script fetch

/**
 * Handle search input change
 * Debounced to avoid excessive API calls during typing
 * 
 * @param event - Input change event
 */
const inputChange = debounce(duration.search, (event: any) => {
  const value = event.target?.value;
  inputValue.value = value;
});

/**
 * Handle scroll component data change
 * Updates local data list with currently visible items
 * 
 * @param data - Array of visible script items from scroll component
 */
const scrollChange = (data: DataItem[]): void => {
  dataList.value = data;
};

/**
 * Handle checkbox selection change
 * Implements single selection behavior (radio-like with checkboxes)
 * 
 * @param event - Checkbox change event
 * @param id - Script ID to select/deselect
 */
const checkChange = (event: { target: { checked: boolean } }, id: string): void => {
  const checked = event.target.checked;
  
  if (checked) {
    // Select this script
    checkedId.value = id;
    return;
  }

  // Deselect
  checkedId.value = undefined;
};

/**
 * Handle OK button click
 * Fetches selected script details, parses YAML content, and emits to parent
 */
const ok = async (): Promise<void> => {
  // Early return if no script selected
  if (!checkedId.value) {
    return;
  }

  // Fetch script details
  loading.value = true;
  const [error, { data }] = await script.getScriptDetail(checkedId.value);
  loading.value = false;
  
  if (error) {
    return;
  }

  try {
    // Parse script content as YAML
    let fileContent = data.content;
    fileContent = fileContent.replace(/\\\n/g, '');  // Remove line continuations
    fileContent = YAML.parse(fileContent);
    
    // Emit parsed content and close modal
    emit('ok', fileContent);
    emit('update:visible', false);
  } catch (error) {
    // Show error notification if YAML parsing fails
    const err = error as Error;
    notification.error(err.message);
  }
};

/**
 * Handle cancel button click
 * Emits cancel event and closes modal
 */
const cancel = (): void => {
  emit('cancel');
  emit('update:visible', false);
};

/**
 * Reset component state
 * Clears selection, search input, data list, and loading state
 */
const reset = (): void => {
  checkedId.value = undefined;
  inputValue.value = undefined;
  dataList.value = [];
  loading.value = false;
};

/**
 * Component mount lifecycle hook
 * Sets up watcher for modal visibility to reset state when opened
 */
onMounted(() => {
  /**
   * Watch for modal visibility changes
   * Resets component state when modal becomes visible
   */
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    // Reset state when modal opens
    reset();
  });
});

/**
 * Computed scroll component parameters
 * Builds query parameters for script list API including filters and project context
 * 
 * @returns Object containing filters, plugin, and project ID
 */
const scrollParams = computed(() => {
  const params = { 
    filters: [] as SearchCriteria[], 
    plugin: props.plugin, 
    projectId: projectId.value 
  };
  
  // Add name filter if search input has value
  if (inputValue.value) {
    params.filters = [{
      key: 'name',
      op: SearchCriteria.OpEnum.Match,
      value: inputValue.value
    }];
  }

  return params;
});

/**
 * API endpoint for script list
 */
const action = `${TESTER}/script`;
</script>

<template>
  <!-- Script selection modal dialog -->
  <Modal
    :title="t('commonPlugin.selectScriptModal.title')"
    :visible="props.visible"
    :centered="true"
    :width="700"
    :confirmLoading="loading"
    class="select-api-modal-wrap"
    @ok="ok"
    @cancel="cancel">
    <!-- Modal body container -->
    <div class="h-full space-y-2.5">
      <!-- Search input with icon suffix -->
      <Input
        :value="inputValue"
        :allowClear="true"
        :placeholder="t('common.placeholders.searchKeyword')"
        trim
        @change="inputChange">
        <template #suffix>
          <Icon icon="icon-sousuo" />
        </template>
      </Input>

      <!-- Script list container (scrollable area + header) -->
      <div style="height: calc(100% - 38px);">
        <!-- Table header row -->
        <div class="bg-table-header flex items-center pl-2 h-8 leading-5 rounded">
          <!-- Checkbox column header (empty) -->
          <div class="w-4 h-5 flex items-center justify-center flex-shrink-0"></div>
          
          <!-- Name column header -->
          <div class="flex-1 px-2">{{ t('common.name') }}</div>
          
          <!-- Plugin column header -->
          <div class="flex-shrink-0 px-2 w-25">{{ t('common.plugin') }}</div>
          
          <!-- Type column header -->
          <div class="flex-shrink-0 px-2 w-28">{{ t('common.type') }}</div>
        </div>

        <!-- Virtual scroll component for efficient list rendering -->
        <Scroll
          v-if="props.visible"
          style="height: calc(100% - 32px);"
          :action="action"
          :lineHeight="32"
          :params="scrollParams"
          class="py-1"
          @change="scrollChange">
          <!-- Script item row -->
          <div
            v-for="item in dataList"
            :key="item.id"
            class="api-item flex items-center h-7 pl-2 leading-5 mb-1 rounded cursor-pointer">
            <!-- Checkbox cell -->
            <div class="w-4 h-5 flex items-center justify-center flex-shrink-0">
              <Checkbox
                :checked="checkedId === item.id"
                class="checkbox-box-white"
                @change="checkChange($event, item.id)" />
            </div>
            
            <!-- Name cell (truncated with tooltip) -->
            <div :title="item.name" class="flex-1 truncate px-2">
              {{ item.name }}
            </div>
            
            <!-- Plugin cell -->
            <div class="flex-shrink-0 px-2 w-25">
              {{ item.plugin }}
            </div>
            
            <!-- Type cell (display name) -->
            <div class="flex-shrink-0 px-2 w-28">
              {{ item.type.message }}
            </div>
          </div>
        </Scroll>
      </div>
    </div>
  </Modal>
</template>

<style>
/**
 * Modal wrapper styles
 * Sets modal height to 60% of viewport with 480px minimum
 */
.select-api-modal-wrap {
  height: calc(60%);
  min-height: 480px;
}

/**
 * Modal content full height
 */
.select-api-modal-wrap .ant-modal-content {
  height: 100%;
}

/**
 * Modal body height
 * Subtracts header and footer heights (92px total)
 */
.select-api-modal-wrap .ant-modal-body {
  height: calc(100% - 92px);
}
</style>

<style scoped>
/**
 * Table header background color
 * Light gray background for header row
 */
.bg-table-header {
  background-color: rgba(247, 248, 251, 100%);
}

/**
 * White checkbox background
 * Overrides default checkbox background color
 */
.checkbox-box-white :deep(.ant-checkbox) {
  background-color: #fff;
}

/**
 * Script item hover effect
 * Highlights row on mouse hover
 */
.api-item:hover {
  background-color: var(--content-tabs-bg-hover);
}

/**
 * Checkbox wrapper alignment
 * Centers checkbox vertically within row
 */
.ant-checkbox-wrapper {
  align-items: center;
  line-height: 20px;
}

/**
 * Checkbox positioning
 * Removes default top offset for proper alignment
 */
:deep(.ant-checkbox) {
  top: 0;
}
</style>

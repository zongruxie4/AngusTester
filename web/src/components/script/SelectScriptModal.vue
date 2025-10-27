<script setup lang="ts">
import { computed, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox } from 'ant-design-vue';
import YAML from 'yaml';
import { Icon, Input, Modal, notification, Scroll } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, TESTER, ScriptType, EnumMessage, SearchCriteria } from '@xcan-angus/infra';
import { script } from '@/api/tester';

/**
 * Data structure for script items
 */
type DataItem = {
  id: string;
  name: string;
  plugin: string;
  type: EnumMessage<ScriptType>;
}

/**
 * Component props interface
 */
export interface Props {
  visible: boolean;
  plugin?: string;
}

// Component Setup
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  plugin: 'Http'
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: any): void;
  (e: 'cancel'): void;
}>();

// Reactive State Management
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
// Search input value
const searchInputValue = ref<string>();
// Selected script ID
const selectedScriptId = ref<string>();
// Script data list
const scriptDataList = ref<DataItem[]>([]);
// Loading state for modal confirmation
const isLoading = ref(false);

/**
 * Parameters for scroll component API request
 * Filters scripts based on search input and project ID
 */
const getQueryParams = computed(() => {
  const params = { filters: [] as SearchCriteria[], plugin: props.plugin, projectId: projectId.value };
  if (searchInputValue.value) {
    params.filters = [{
      key: 'name',
      op: SearchCriteria.OpEnum.Match,
      value: searchInputValue.value
    }];
  }
  return params;
});

// API endpoint for script data
const queryAction = `${TESTER}/script`;

/**
 * Handle search input changes with debouncing
 * Updates the search input value after a delay to reduce API calls
 */
const handleSearchInputChange = debounce(duration.search, (event: Event) => {
  const target = event.target as HTMLInputElement;
  searchInputValue.value = target.value;
});

/**
 * Handle scroll data changes
 * Updates the script data list when scroll component fetches new data
 */
const handleScrollChange = (data: DataItem[]) => {
  scriptDataList.value = data;
};

/**
 * Handle checkbox selection changes
 * Updates the selected script ID based on checkbox state
 */
const handleCheckboxChange = (event: { target: { checked: boolean } }, id: string) => {
  const isChecked = event.target.checked;
  if (isChecked) {
    selectedScriptId.value = id;
    return;
  }
  selectedScriptId.value = undefined;
};

/**
 * Handle row click event
 * Selects the script when clicking on a row
 */
const handleRowClick = (id: string) => {
  if (selectedScriptId.value === id) {
    selectedScriptId.value = undefined;
  } else {
    selectedScriptId.value = id;
  }
};

/**
 * Reset component state when modal visibility changes
 */
const resetComponentState = () => {
  selectedScriptId.value = undefined;
  searchInputValue.value = undefined;
  scriptDataList.value = [];
  isLoading.value = false;
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }
    resetComponentState();
  });
});

/**
 * Handle modal confirmation
 * Fetches selected script details and emits the parsed content
 */
const handleConfirm = async () => {
  if (!selectedScriptId.value) {
    notification.warning(t('commonPlugin.selectScriptModal.selectScriptTip'));
    return;
  }

  isLoading.value = true;
  const [error, { data }] = await script.getScriptDetail(selectedScriptId.value || '');
  isLoading.value = false;

  if (error) {
    return;
  }

  try {
    let fileContent = data.content;
    // Remove line continuation characters
    fileContent = fileContent.replace(/\\\n/g, '');
    // Parse YAML content
    fileContent = YAML.parse(fileContent);
    emit('ok', fileContent);
    emit('update:visible', false);
  } catch (error: unknown) {
    notification.error((error as Error)?.message || 'Failed to parse script content');
  }
};

/**
 * Handle modal cancellation
 * Emits cancel events and closes the modal
 */
const handleCancel = () => {
  emit('cancel');
  emit('update:visible', false);
};
</script>
<template>
  <Modal
    :title="t('commonPlugin.selectScriptModal.title')"
    :visible="props.visible"
    :centered="true"
    :width="800"
    :confirmLoading="isLoading"
    class="select-api-script-modal-wrap"
    @ok="handleConfirm"
    @cancel="handleCancel">
    <div class="script-selector-container">
      <!-- Search Section -->
      <div class="search-section">
        <Input
          :value="searchInputValue"
          :allowClear="true"
          :placeholder="t('common.placeholders.searchKeyword')"
          trim
          class="search-input"
          @change="handleSearchInputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" class="search-icon" />
          </template>
        </Input>
      </div>

      <!-- Table Section -->
      <div class="table-section">
        <div class="table-header">
          <div class="table-cell checkbox-cell"></div>
          <div class="table-cell name-cell">{{ t('common.name') }}</div>
          <div class="table-cell plugin-cell">{{ t('common.plugin') }}</div>
          <div class="table-cell type-cell">{{ t('common.type') }}</div>
        </div>

        <Scroll
          v-if="props.visible"
          :action="queryAction"
          :lineHeight="40"
          :params="getQueryParams"
          class="table-body"
          @change="handleScrollChange">
          <div
            v-for="item in scriptDataList"
            :key="item.id"
            :class="['script-item', { 'selected': selectedScriptId === item.id }]"
            @click="handleRowClick(item.id)">
            <div class="table-cell checkbox-cell">
              <Checkbox
                :checked="selectedScriptId===item.id"
                class="script-checkbox"
                @change="handleCheckboxChange($event,item.id)" />
            </div>
            <div :title="item.name" class="table-cell name-cell script-name">
              {{ item.name }}
            </div>
            <div class="table-cell plugin-cell">
              <span class="plugin-tag">{{ item.plugin }}</span>
            </div>
            <div class="table-cell type-cell">
              <span class="type-tag">{{ item.type.message }}</span>
            </div>
          </div>
        </Scroll>
      </div>
    </div>
  </Modal>
</template>

<style>
.select-api-script-modal-wrap {
  height: calc(60%);
  min-height: 480px;
}

.select-api-script-modal-wrap .ant-modal-content {
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.select-api-script-modal-wrap .ant-modal-body {
  height: calc(100% - 92px);
  padding: 0;
}
</style>

<style scoped>
.script-selector-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
}

/* Search Section Styles */
.search-section {
  margin-bottom: 16px;
}

.search-input {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s;
}

.search-input:hover,
.search-input:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-icon {
  color: #bfbfbf;
}

/* Table Section Styles */
.table-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  height: 40px;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 500;
  padding: 0 12px;
}

.table-body {
  flex: 1;
  overflow-y: auto;
}

.script-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.script-item:hover {
  background-color: #f5f5f5;
}

.script-item.selected {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}

/* Table Cell Styles */
.table-cell {
  padding: 8px 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.checkbox-cell {
  flex: 0 0 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.name-cell {
  flex: 1;
}

.plugin-cell {
  flex: 0 0 100px;
}

.type-cell {
  flex: 0 0 160px;
}

.script-name {
  font-size: 12px;
  color: #333;
}

.plugin-tag {
  background-color: #f0f5ff;
  color: #2f54eb;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-tag {
  background-color: #f9f0ff;
  color: #722ed1;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.script-checkbox {
  transform: scale(0.8);
}

/* Scrollbar Styles */
.table-body::-webkit-scrollbar {
  width: 6px;
}

.table-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.table-body::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.table-body::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

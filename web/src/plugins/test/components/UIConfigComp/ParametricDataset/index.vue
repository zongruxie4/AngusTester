<script setup lang="ts">
import { ref, onMounted, defineAsyncComponent, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, Colon, Tooltip, Arrow } from '@xcan-angus/vue-ui';
import { Button, RadioGroup, Radio, Collapse, CollapsePanel, TabPane, Tabs, Popconfirm } from 'ant-design-vue';
import { EnumMessage, ActionOnEOF, SharingMode, enumUtils } from '@xcan-angus/infra';
import { DataMenuKey } from '@/views/data/menu';

import { DatasetItem } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
type Props = {
  projectId: string;         // Project identifier for filtering datasets
  datasets: DatasetItem[];   // Array of selected datasets
  actionOnEOF: ActionOnEOF;  // Behavior when dataset reaches end of file
  sharingMode: SharingMode;  // How datasets are shared between threads
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  datasets: () => [],
  actionOnEOF: ActionOnEOF.RECYCLE,        // Default: recycle (loop back to start)
  sharingMode: SharingMode.ALL_THREAD      // Default: share across all threads
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'change', value: DatasetItem[]): void;  // Emit dataset list changes
  (e: 'targetInfoChange', value: {            // Emit configuration changes
    actionOnEOF?: ActionOnEOF;
    sharingMode?: SharingMode;
  }): void;
}>();

/**
 * Lazy-loaded child components for better performance
 */
const DatasetModal = defineAsyncComponent(() => import('./DatasetModal/index.vue'));
const DatasetUseList = defineAsyncComponent(() => import('./DatasetUseList/index.vue'));
const PreviewData = defineAsyncComponent(() => import('./PreviewData/index.vue'));
const StaticParameters = defineAsyncComponent(() => import('./StaticParameters/index.vue'));
const ExtractParameters = defineAsyncComponent(() => import('./ExtractParameters/index.vue'));

/**
 * State Management
 */
const tableData = ref<DatasetItem[]>([]);        // Current dataset list
const modalVisible = ref(false);                 // Dataset selection modal visibility
const collapseActiveKeys = ref<string[]>([]);    // Expanded collapse panel keys

/**
 * Enum options for configuration
 */
const eofEnums = ref<EnumMessage<ActionOnEOF>[]>([]);         // Action on EOF options
const sharingModeEnums = ref<EnumMessage<SharingMode>[]>([]); // Sharing mode options

/**
 * Handle collapse panel expand/collapse
 * Updates the active keys list to control panel visibility
 *
 * @param open - Whether to expand or collapse
 * @param id - Dataset ID (panel key)
 */
const arrowChange = (open: boolean, id: string): void => {
  if (!open) {
    // Collapse: remove from active keys
    collapseActiveKeys.value = collapseActiveKeys.value.filter(item => item !== id);
    return;
  }

  // Expand: add to active keys
  collapseActiveKeys.value.push(id);
};

/**
 * Open dataset selection modal
 * Shows modal to add new datasets
 */
const toUse = (): void => {
  modalVisible.value = true;
};

/**
 * Handle action on EOF configuration change
 * Emits configuration update to parent component
 *
 * @param event - Radio group change event
 */
const datasetActionOnEOFChange = async (event: any): Promise<void> => {
  const value = event.target?.value;
  const params = { actionOnEOF: value };
  emit('targetInfoChange', params);
};

/**
 * Handle sharing mode configuration change
 * Emits configuration update to parent component
 *
 * @param event - Radio group change event
 */
const datasetSharingModeChange = async (event: any): Promise<void> => {
  const value = event.target?.value;
  const params = { sharingMode: value };
  emit('targetInfoChange', params);
};

/**
 * Handle dataset selection from modal
 * Adds selected datasets to the beginning of the list
 *
 * @param data - Array of selected dataset items
 */
const selectedVariablesOk = async (data: DatasetItem[]): Promise<void> => {
  if (!data?.length) {
    return;
  }

  // Add selected datasets to the beginning of the list
  tableData.value.unshift(...data);
  emit('change', tableData.value);
};

/**
 * Handle dataset deletion (unlink)
 * Removes dataset from the list
 *
 * @param data - Dataset item to delete
 */
const toDelete = async (data: DatasetItem): Promise<void> => {
  const id = data['x-id'];
  const index = tableData.value.findIndex((item) => item['x-id'] === id);
  tableData.value.splice(index, 1);
  emit('change', tableData.value);
};

/**
 * Load action on EOF enum options
 * Converts ActionOnEOF enum to display format
 */
const loadActionOnEOFEnums = (): void => {
  eofEnums.value = enumUtils.enumToMessages(ActionOnEOF);
};

/**
 * Load sharing mode enum options
 * Converts SharingMode enum to display format
 */
const loadSharingModeEnums = (): void => {
  sharingModeEnums.value = enumUtils.enumToMessages(SharingMode);
};

/**
 * Reset component state to initial values
 * Clears dataset list, modal state, and collapse keys
 */
const reset = (): void => {
  tableData.value = [];
  modalVisible.value = false;
  collapseActiveKeys.value = [];
};

/**
 * Component mount lifecycle hook
 * Loads enum options and watches for dataset changes
 */
onMounted(() => {
  // Load configuration enum options
  loadActionOnEOFEnums();
  loadSharingModeEnums();

  /**
   * Watch for datasets prop changes
   * Resets state and updates table data when datasets change
   */
  watch(() => props.datasets, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }
    tableData.value = newValue;
  }, { immediate: true });
});

/**
 * Computed array of selected dataset names
 * Used to disable already-selected datasets in the modal
 */
const selectedNames = computed(() => {
  return tableData.value?.map(item => item.name) || [];
});

/**
 * Hint text mapping for different extraction sources
 */
const hintTextMap = {
  FILE: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.hintTextMap.file'),
  JDBC: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.hintTextMap.jdbc')
};
</script>

<template>
  <!-- Main container -->
  <div class="text-3 leading-5">
    <!-- Section header with blue indicator bar -->
    <div class="flex items-center flex-nowrap mb-2.5">
      <!-- Blue indicator bar -->
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>

      <!-- Section title -->
      <div class="flex-shrink-0 text-theme-title mr-2.5">
        {{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.title') }}
      </div>

      <!-- Info icon -->
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />

      <!-- Description label -->
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">
        {{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.description') }}
      </div>
    </div>

    <!-- Configuration section: Action on EOF and Sharing Mode -->
    <div class="flex items-center space-x-15 mb-2">
      <!-- Action on EOF configuration -->
      <div class="flex-shrink-0 flex items-center">
        <!-- Label -->
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.actionOnEOF') }}</span>
          <Colon />
        </div>

        <!-- Radio group for EOF action options -->
        <RadioGroup
          :value="props.actionOnEOF"
          name="action"
          @change="datasetActionOnEOFChange">
          <Radio
            v-for="item in eofEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>

              <!-- Tooltip for RECYCLE option -->
              <Tooltip
                v-if="item.value === 'RECYCLE'"
                :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.recycleTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>

              <!-- Tooltip for STOP_THREAD option -->
              <Tooltip
                v-else-if="item.value === 'STOP_THREAD'"
                :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.stopThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>

      <!-- Sharing mode configuration -->
      <div class="flex-shrink-0 flex items-center">
        <!-- Label -->
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.sharingMode') }}</span>
          <Colon />
        </div>

        <!-- Radio group for sharing mode options -->
        <RadioGroup
          :value="props.sharingMode"
          name="share"
          @change="datasetSharingModeChange">
          <Radio
            v-for="item in sharingModeEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>

              <!-- Tooltip for ALL_THREAD option -->
              <Tooltip
                v-if="item.value === 'ALL_THREAD'"
                :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.allThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>

              <!-- Tooltip for CURRENT_THREAD option -->
              <Tooltip
                v-else-if="item.value === 'CURRENT_THREAD'"
                :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.currentThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>
    </div>

    <!-- Add dataset button -->
    <div class="mb-2">
      <Button
        type="link"
        size="small"
        class="flex items-center h-5 leading-5 p-0 space-x-1"
        @click="toUse">
        <Icon icon="icon-jia" class="text-3.5" />
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.addDataset') }}</span>
      </Button>
    </div>

    <!-- Empty state (when no datasets selected) -->
    <div
      v-if="tableData.length === 0"
      class="flex-1 flex flex-col items-center justify-center">
      <img style="width:100px;" src="./images/nodata.png">
      <div class="flex items-center text-theme-sub-content text-3">
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.noDatasetsDefined') }}</span>
      </div>
    </div>

    <!-- Dataset list (when datasets exist) -->
    <div v-else>
      <!-- Table header row -->
      <div class="flex items-center table-thead-tr">
        <div class="table-thead-th">{{ t('common.name') }}</div>
        <div class="table-thead-th">{{ t('common.creator') }}</div>
        <div class="table-thead-th">{{ t('common.actions') }}</div>
      </div>

      <!-- Collapsible dataset panels -->
      <Collapse
        v-model:activeKey="collapseActiveKeys"
        collapsible="disabled">
        <!-- Individual dataset panel -->
        <CollapsePanel
          v-for="item in tableData"
          :key="item['x-id']"
          :showArrow="false">
          <!-- Panel header: dataset row with name, creator, and actions -->
          <template #header>
            <div class="flex items-center table-tbody-tr">
              <!-- Name column with expand/collapse arrow -->
              <div class="table-tbody-td">
                <Arrow
                  :open="collapseActiveKeys.includes(item['x-id'])"
                  type="dashed"
                  style="font-size:12px;"
                  class="mr-1"
                  @change="arrowChange($event, item['x-id'])" />
                <div class="flex-1 truncate">
                  <span
                    :title="item.name"
                    class="truncate cursor-pointer"
                    @click="arrowChange(!collapseActiveKeys.includes(item['x-id']), item['x-id'])">
                    {{ item.name }}
                  </span>
                </div>
              </div>

              <!-- Creator column -->
              <div class="table-tbody-td" :title="item['x-creator']">
                <div class="flex-1 truncate">{{ item['x-creator'] }}</div>
              </div>

              <!-- Actions column -->
              <div class="table-tbody-td flex items-center space-x-2.5">
                <!-- Unlink/delete button with confirmation -->
                <Popconfirm
                  :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.confirmUnlinkDataset', { name: item.name })"
                  @confirm="toDelete(item)">
                  <Button
                    :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.unlinkDataset')"
                    type="text"
                    size="small"
                    class="flex items-center p-0 h-5 leading-5 space-x-1">
                    <Icon icon="icon-qingchu" class="text-3.5" />
                  </Button>
                </Popconfirm>

                <!-- View definition link (opens in new tab) -->
                <Button
                  :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.viewDefinition')"
                  type="text"
                  size="small"
                  class="p-0 h-5 leading-5">
                  <RouterLink
                    class="flex items-center space-x-1"
                    :to="`/data#${DataMenuKey.DATASET}?id=${item['x-id']}`"
                    target="_blank">
                    <Icon icon="icon-lianjie1" class="text-3.5" />
                  </RouterLink>
                </Button>
              </div>
            </div>
          </template>

          <!-- Panel content: tabs for different views -->
          <Tabs size="small" class="ant-tabs-nav-mb-2.5 normal-tabs">
            <!-- Static parameters tab (shown when no extraction configured) -->
            <template v-if="!item.extraction">
              <TabPane
                key="value"
                :tab="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.parameters')">
                <StaticParameters
                  :dataSource="item.parameters"
                  class="mb-5" />
              </TabPane>
            </template>

            <!-- Extracted parameters tab (shown when extraction configured) -->
            <template v-else>
              <TabPane
                key="value"
                :tab="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.extractParameters')">
                <ExtractParameters
                  :dataSource="item.parameters"
                  :columnIndex="+item.extraction.columnIndex"
                  :hintText="hintTextMap[item.extraction.source]"
                  class="mb-5" />
              </TabPane>
            </template>

            <!-- Preview data tab -->
            <TabPane
              key="preview"
              :tab="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.previewData')">
              <PreviewData :dataSource="item as any" />
            </TabPane>

            <!-- Dataset usage tab -->
            <TabPane
              key="use"
              :tab="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseList')">
              <DatasetUseList :id="item['x-id']" />
            </TabPane>
          </Tabs>
        </CollapsePanel>
      </Collapse>
    </div>

    <!-- Dataset selection modal (lazy loaded) -->
    <AsyncComponent :visible="modalVisible">
      <DatasetModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        :selectedNames="selectedNames"
        @ok="selectedVariablesOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.table-thead-tr {
  width: 100%;
  border-bottom: 1px solid var(--border-divider);
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
  background-color: var(--table-header-bg);
}

.table-thead-tr .table-thead-th {
  position: relative;
  height: 37px;
  padding: 2px 8px;
  overflow: hidden;
  line-height: 37px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-thead-tr .table-thead-th:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 50%;
  right: 0;
  width: 1px;
  height: 1.6em;
  transform: translateY(-50%);
  transition: background-color .3s;
  background-color: var(--border-divider);
}

.table-tbody-tr {
  width: 100%;
}


.table-tbody-tr:hover {
  background: var(--content-tabs-bg-hover);
}
.table-tbody-tr .table-tbody-td {
  display: flex;
  position: relative;
  flex-wrap: nowrap;
  align-items: center;
  height: 37px;
  padding: 2px 8px;
  overflow: hidden;
  line-height: 18px;
}


.table-thead-tr .table-thead-th:nth-child(1),
.table-tbody-tr .table-tbody-td:nth-child(1) {
  flex: 1 1 calc(85% - 180px);
}

.table-thead-tr .table-thead-th:nth-child(2),
.table-tbody-tr .table-tbody-td:nth-child(2) {
  width: 15%;
}


.table-thead-tr .table-thead-th:nth-child(3),
.table-tbody-tr .table-tbody-td:nth-child(3) {
  width: 80px;
}


.ant-collapse {
  border: none;
  background-color: #fff;
  font-size: 12px;
  line-height: 18px;
}


.ant-collapse :deep(.ant-collapse-header) {
  display: flex;
  align-items: center;
  padding: 0;
  line-height: 18px;
}

.ant-collapse>:deep(.ant-collapse-item).ant-collapse-no-arrow>.ant-collapse-header {
  padding-left: 0;
}

.ant-collapse :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 24px;
}

.ant-tabs.ant-tabs-top.ant-tabs-small.normal-tabs>:deep(.ant-tabs-nav) {
  margin: 0 0 10px;
}
</style>

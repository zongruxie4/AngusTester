<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { TableColumnType } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Icon, Table } from '@xcan-angus/vue-ui';

/**
 * Data unit type for bandwidth metrics
 */
type BandwidthUnit = 'KB' | 'MB';

/**
 * Component props interface
 */
interface Props {
  isSingleInterface: boolean;        // Whether testing single API or multiple
  columns: TableColumnType[];        // Table column definitions
  tableData: Record<string, any>[];  // Hierarchical table data
  brpsUnit: BandwidthUnit;           // Bytes received per second unit
  minBrpsUnit: BandwidthUnit;        // Min BRPS unit
  maxBrpsUnit: BandwidthUnit;        // Max BRPS unit
  meanBrpsUnit: BandwidthUnit;       // Mean BRPS unit
  bwpsUnit: BandwidthUnit;           // Bytes written per second unit
  minBwpsUnit: BandwidthUnit;        // Min BWPS unit
  maxBwpsUnit: BandwidthUnit;        // Max BWPS unit
  meanBwpsUnit: BandwidthUnit;       // Mean BWPS unit
  writeBytesUnit?: BandwidthUnit;    // Write bytes unit (optional)
  tabKey?: string;                   // Current tab identifier (optional)
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  columns: () => [],
  tableData: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  writeBytesUnit: 'KB',
  tabKey: ''
});

/**
 * Apply custom row class for Total row
 * Highlights the Total summary row with special background
 * 
 * @param record - Table row record
 * @returns Row configuration object
 */
const customRow = (record: any) => {
  if (record.name === 'Total') {
    return {
      class: 'last-row'
    };
  }
  return {};
};

/**
 * State Management
 */

// Array of expanded row IDs
const expandedRowKeys = ref<string[]>([]);

// Processed data source with unique IDs
const dataSource = ref<Record<string, any>[]>([]);

// Whether to preserve expansion state on data update
const isSaveExpand = ref(false);

// Map to maintain consistent IDs across updates
const idsMap = ref<Record<string, string>>({});

/**
 * Watch for table data changes
 * Processes data by adding unique IDs and managing expansion state
 */
watch(() => props.tableData, (newValue) => {
  if (newValue.length) {
    // Reset expansion state if not preserving
    if (!isSaveExpand.value) {
      expandedRowKeys.value = [];
    }
    
    // Process data: add IDs and handle expansion
    dataSource.value = newValue.map(item => {
      // Generate or reuse ID for this item
      if (!isSaveExpand.value) {
        if (!idsMap.value[item.name]) {
          idsMap.value[item.name] = utils.uuid();
        }
      }
      item.id = idsMap.value[item.name];
      
      // Handle child items (nested APIs)
      if (item.list && Array.isArray(item.list) && item.list.length > 0) {
        // Auto-expand rows with children on first load
        if (!isSaveExpand.value) {
          expandedRowKeys.value.push(idsMap.value[item.name]);
        }
        
        // Add unique IDs to child items
        item.list = item.list.map((item1: any) => {
          item1.id = utils.uuid();
          return item1;
        });
      }
      
      return item;
    });
  } else {
    // Reset state when no data
    isSaveExpand.value = false;
    idsMap.value = {};
  }
}, {
  immediate: true
});

/**
 * Handle row expand/collapse
 * Toggles row expansion and preserves state
 * 
 * @param record - Expand event record containing row data
 */
const handleExpand = (record: any): void => {
  const _id = record.record.id;
  
  if (record.expanded) {
    // Collapse: remove from expanded keys
    expandedRowKeys.value = expandedRowKeys.value.filter(item => item !== _id);
  } else {
    // Expand: add to expanded keys if not already present
    if (!expandedRowKeys.value.includes(_id)) {
      expandedRowKeys.value.push(_id);
    }
  }

  // Mark that we want to preserve expansion state
  isSaveExpand.value = true;
};

/**
 * Computed columns for child table
 * Adds left padding to name column for visual hierarchy
 */
const childTablepColumns = computed(() => {
  return props.columns.map(item => {
    if (item.dataIndex === 'name') {
      return {
        ...item,
        customCell: () => {
          return { style: 'padding-left: 20px' };
        }
      };
    } else {
      return item;
    }
  });
});

</script>

<template>
  <!-- Main hierarchical table -->
  <Table
    v-model:expandedRowKeys="expandedRowKeys"
    rowKey="id"
    size="small"
    defaultExpandAllRows
    :columns="props.columns as any"
    :dataSource="dataSource"
    :pagination="false"
    :rowExpandable="(record) => record?.list?.length"
    :customRow="customRow"
    noDataSize="small"
    noDataText="">
    
    <!-- Custom expand/collapse icon -->
    <template #expandIcon="record">
      <Icon
        v-if="record.record.list?.length"
        :icon="record.expanded ? 'icon-xiangxia' : 'icon-xiangshang'"
        class="text-3 text-text-sub-content cursor-pointer ml-1.5"
        @click="handleExpand(record)" />
    </template>
    
    <!-- Custom cell rendering for special columns -->
    <template #bodyCell="{ column, text, record }">
      <!-- Multi-OPS column: shows ops | min | max | mean -->
      <template v-if="column.dataIndex === 'multip-ops'">
        <div class="flex items-center">
          <div>{{ record.ops }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.minOps }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.maxOps }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.meanOps }}</div>
        </div>
      </template>

      <!-- Multi-TPS column: shows tps | min | max | mean -->
      <template v-else-if="column.dataIndex === 'multip-tps'">
        <div class="flex items-center">
          <div>{{ record.tps }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.minTps }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.maxTps }}</div>
          <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
          <div>{{ record.meanTps }}</div>
        </div>
      </template>

      <!-- Bytes received per second with unit -->
      <template v-else-if="column.dataIndex === 'brps'">
        <template v-if="props.tabKey === 'aggregate'">
          <!-- Single interface: show all statistics with units -->
          <template v-if="props.isSingleInterface">
            <div class="flex items-cneter space-x-3">
              <span>{{ text ? `${text}(${props.brpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.minBrps ? `${record.minBrps}(${props.minBrpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.maxBrps ? `${record.maxBrps}(${props.maxBrpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.meanBrps ? `${record.meanBrps}(${props.meanBrpsUnit})` : '--' }}</span>
            </div>
          </template>

          <!-- Multiple interfaces: show single value with unit -->
          <template v-else>
            <span>{{ text ? `${text}${props.brpsUnit}` : '--' }}</span>
          </template>
        </template>
      </template>
      
      <!-- Bytes written per second with unit -->
      <template v-else-if="column.dataIndex === 'bwps'">
        <template v-if="props.tabKey === 'aggregate'">
          <!-- Single interface: show all statistics with units -->
          <template v-if="props.isSingleInterface">
            <div class="flex items-cneter space-x-3">
              <span>{{ text ? `${text}(${props.bwpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.minBwps ? `${record.minBwps}(${props.minBwpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.maxBwps ? `${record.maxBwps}(${props.maxBwpsUnit})` : '--' }}</span>
              <span>|</span>
              <span>{{ record.meanBwps ? `${record.meanBwps}(${props.meanBwpsUnit})` : '--' }}</span>
            </div>
          </template>

          <!-- Multiple interfaces: show single value with unit -->
          <template v-else>
            <span>{{ text ? `${text}${props.bwpsUnit}` : '--' }}</span>
          </template>
        </template>
      </template>

      <!-- Write bytes with unit -->
      <template v-else-if="column.dataIndex === 'writeBytes'">
        {{ text === '--' ? text : text + props.writeBytesUnit }}
      </template>
      
      <!-- Error rate with percentage -->
      <template v-else-if="column.dataIndex === 'errorRate'">
        {{ text === '--' ? text : text + '%' }}
      </template>
    </template>
    
    <!-- Expanded row render: nested child table -->
    <template #expandedRowRender="{ record }">
      <template v-if="record.list?.length">
        <!-- Child table for nested APIs (no header, indented) -->
        <Table
          class="group-table"
          rowKey="name"
          :columns="childTablepColumns as any"
          :data-source="record.list"
          :rowExpandable="() => false"
          :pagination="false"
          :showHeader="false"
          noDataSize="small"
          noDataText="">
          <!-- Custom cell rendering for child table -->
          <template #bodyCell="{ column: c_column, text: c_text }">
            <!-- Write bytes with unit -->
            <template v-if="c_column.dataIndex === 'writeBytes'">
              {{ c_text === '--' ? c_text : c_text + props.writeBytesUnit }}
            </template>
            
            <!-- Error rate with percentage -->
            <template v-if="c_column.dataIndex === 'errorRate'">
              {{ c_text === '--' ? c_text : c_text + '%' }}
            </template>
          </template>
          
          <!-- Empty expanded row render (disable further expansion) -->
          <template #expandedRowRender />
        </Table>
      </template>
    </template>
  </Table>
</template>

<style scoped>
/**
 * Nested table styles for child rows
 */
.group-table :deep(.ant-table) {
  margin: 3.5px -8px !important;
  font-size: 12px;
}

/**
 * Total row background color
 */
:deep(.ant-table .last-row > td) {
  background-color: #FAFCFC;
}
</style>

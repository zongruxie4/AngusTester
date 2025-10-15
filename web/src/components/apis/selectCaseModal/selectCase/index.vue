<script lang="ts" setup>
// Vue core imports
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { DropdownGroup, HttpMethodText, Icon, IconText, Input, Select, Table } from '@xcan-angus/vue-ui';
import { Button, Tree } from 'ant-design-vue';

// Infrastructure imports
import { TESTER } from '@xcan-angus/infra';

// Utility imports
import { debounce } from 'throttle-debounce';

// API imports
import { services, apis } from '@/api/tester';

const { t } = useI18n();

/**
 * Component props interface for select case component
 */
interface Props {
  visible: boolean;
  projectId: string;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  visible: true,
  projectId: undefined
});

// Component events
const emits = defineEmits<{(e: 'change', ids: string[], rows: any[]): void}>();

// Component state
const apiSearchKeywords = ref<string>();
const caseSearchKeywords = ref<string>();

/**
 * Group data by specified key
 * @param data - Array of data to group
 * @param key - Key to group by
 * @returns Grouped data object
 */
const groupDataByKey = (data: Array<Record<string, any>>, key: string) => {
  const groupedResult: Record<string, Array<any>> = {};
  data.forEach((item) => {
    const keyValue = item[key];
    if (!groupedResult[keyValue]) {
      groupedResult[keyValue] = [item];
    } else {
      groupedResult[keyValue].push(item);
    }
  });
  return groupedResult;
};

/**
 * Emit selected case data to parent
 */
const emitSelectedCaseData = () => {
  if (currentGroupingKey.value !== '') {
    const groupIds = apiListData.value.map(item => item.id);
    const selectedIds = tableRowSelection.value.selectedRowKeys.filter(id => !groupIds.includes(id));
    const selectedRows = allApiData.filter(row => selectedIds.includes(row.id));
    emits('change', selectedIds, selectedRows);
  } else {
    const selectedRows = allApiData.filter(row => tableRowSelection.value.selectedRowKeys.includes(row.id));
    emits('change', tableRowSelection.value.selectedRowKeys, selectedRows);
  }
};

// Service selection state
const selectedServiceId = ref<string>();

/**
 * Handle service selection
 * @param serviceId - Selected service ID
 */
const handleServiceSelection = (serviceId: string) => {
  selectedServiceId.value = serviceId;
  apiListData.value = [];
  allApiData = [];
  currentSelectedApiId.value = [];
  tableRowSelection.value.selectedRowKeys = [];
  emitSelectedCaseData();
  loadApiList();
};

// API data storage
let allApiData: any[] = [];
const currentSelectedApiId = ref<string[]>([]);

/**
 * Load API list from service
 */
const loadApiList = async () => {
  const [error, { data = {} }] = await services.loadApisByServicesId(selectedServiceId.value, {
    pageNo: 1,
    pageSize: 2000,
    projectId: props.projectId,
    filters: apiSearchKeywords.value ? [{ value: apiSearchKeywords.value, op: 'MATCH', key: 'summary' }] : []
  });
  if (error) {
    allApiData = [];
    apiListData.value = [];
    allApiCaseData.value = [];
    currentSelectedApiId.value = [];
    emitSelectedCaseData();
    return;
  }

  allApiData = data?.list || [];
  processGroupedData(allApiData);
  if (apiListData.value.length && !currentSelectedApiId.value.length) {
    if (currentGroupingKey.value !== '') {
      currentSelectedApiId.value = apiListData.value?.[0]?.children?.[0]?.id ? [apiListData.value?.[0]?.children?.[0]?.id] : [];
    } else {
      currentSelectedApiId.value = apiListData.value?.[0]?.id ? [apiListData.value?.[0]?.id] : [];
    }
  } else {
    currentSelectedApiId.value = [];
  }
};

// Table loading state
const isTableLoading = ref(false);
const allApiCaseData = ref<any[]>([]);

/**
 * Load API cases for selected API
 * @param apiId - API ID to load cases for
 */
const loadApiCases = async (apiId: string) => {
  allApiCaseData.value = [];
  if (!apiId) {
    return;
  }
  isTableLoading.value = true;
  const [error, { data }] = await apis.loadApiCases({
    pageNo: 1,
    pageSize: 2000,
    apisId: apiId,
    filters: caseSearchKeywords.value ? [{ value: caseSearchKeywords.value, op: 'MATCH', key: 'name' }] : []
  });
  isTableLoading.value = false;
  tableRowSelection.value.selectedRowKeys = [];
  emitSelectedCaseData();
  if (error) {
    return;
  }
  allApiCaseData.value = data?.list || [];
};

// Grouping configuration
const currentGroupingKey = ref('');

const groupingOptions = [
  {
    key: '',
    name: t('actions.noGroup')
  },
  {
    key: 'createdBy',
    name: t('commonComp.apis.selectCaseModal.groupByCreator')
  },
  {
    key: 'method',
    name: t('commonComp.apis.selectCaseModal.groupByMethod')
  },
  {
    key: 'ownerId',
    name: t('commonComp.apis.selectCaseModal.groupByOwner')
  },
  {
    key: 'tag',
    name: t('commonComp.apis.selectCaseModal.groupByTag')
  }
];

// Table column configuration
const tableColumns = [
  {
    title: t('commonComp.apis.selectCaseModal.caseName'),
    dataIndex: 'name'
  },
  {
    title: t('protocol.http.method'),
    dataIndex: 'method',
    width: 100,
    customRender: ({ text }) => text?.message
  },
  {
    title: t('common.path'),
    dataIndex: 'endpoint'
  },
  {
    title: t('commonComp.apis.selectCaseModal.caseType'),
    dataIndex: 'type',
    width: 100,
    customRender: ({ text }) => text?.message
  }
];

// API list data
const apiListData = ref<{id: string; type?: string; children?: Record<string, any>[], name: string, key?: string}[]>([]);

// Table row selection configuration
const tableRowSelection = ref({
  selectedRowKeys: [] as string[],
  checkStrictly: false,
  onChange: (selectedRowKeys: string[]) => {
    tableRowSelection.value.selectedRowKeys = selectedRowKeys;
    emitSelectedCaseData();
  }
});

/**
 * Process and group API data based on current grouping key
 * @param dataList - List of API data to process
 */
const processGroupedData = (dataList: any[] = []) => {
  apiListData.value = [];
  if (currentGroupingKey.value !== '') {
    if (currentGroupingKey.value !== 'tag') {
      const groupedData = groupDataByKey(dataList, currentGroupingKey.value);
      Object.keys(groupedData).forEach(key => {
        apiListData.value.push({
          type: 'group',
          summary: currentGroupingKey.value === 'createdBy'
            ? groupedData[key][0].createdByName
            : currentGroupingKey.value === 'method'
              ? groupedData[key][0].method
              : currentGroupingKey.value === 'ownerId'
                ? groupedData[key][0].ownerName
                : key,
          id: key,
          key,
          selectable: false,
          children: [...groupedData[key]]
        });
      });
    } else {
      const tagGroupedResult: Record<string, any[]> = {};
      dataList.forEach((item) => {
        if (item.tags && item.tags.length) {
          item.tags.forEach(tag => {
            if (!tagGroupedResult[tag]) {
              tagGroupedResult[tag] = [item];
            } else {
              tagGroupedResult[tag].push(item);
            }
          });
        } else {
          if (!tagGroupedResult.null_tag) {
            tagGroupedResult.null_tag = [item];
          } else {
            tagGroupedResult.null_tag.push(item);
          }
        }
      });
      Object.entries(tagGroupedResult).forEach(([key, list]) => {
        apiListData.value.push({
          type: 'group',
          summary: key,
          id: key,
          key,
          selectable: false,
          children: [...list]
        });
      });
    }
  } else {
    apiListData.value = dataList;
  }
};

/**
 * Handle grouping key change
 * @param key - New grouping key
 */
const handleGroupingKeyChange = (key: string) => {
  currentGroupingKey.value = key;
  tableRowSelection.value.selectedRowKeys = [];
  emitSelectedCaseData();
  processGroupedData(allApiData);
  if (apiListData.value.length && !currentSelectedApiId.value) {
    if (currentGroupingKey.value !== '') {
      currentSelectedApiId.value = apiListData.value?.[0]?.children?.[0]?.id ? [apiListData.value?.[0]?.children?.[0]?.id] : [];
    } else {
      currentSelectedApiId.value = apiListData.value?.[0]?.id ? [apiListData.value?.[0]?.id] : [];
    }
  } else {
    currentSelectedApiId.value = [];
  }
};

/**
 * Handle API keywords search change with debounce
 */
const handleApiKeywordsSearchChange = debounce(300, () => {
  loadApiList();
});

/**
 * Handle case keywords search change with debounce
 */
const handleCaseKeywordsSearchChange = debounce(300, () => {
  loadApiCases(currentSelectedApiId.value[0]);
});

// Watch for API selection changes
onMounted(() => {
  watch(() => currentSelectedApiId.value, (newValue: string[]) => {
    loadApiCases(newValue[0]);
  });
});

// Expose component methods
defineExpose({
  reset: () => {
    apiListData.value = [];
    allApiData = [];
    allApiCaseData.value = [];
    tableRowSelection.value.selectedRowKeys = [];
    emitSelectedCaseData();
    if (selectedServiceId.value) {
      handleServiceSelection(selectedServiceId.value);
    }
  }
});

</script>
<template>
  <div class="flex space-x-2">
    <div class="w-80 space-y-1">
      <div class="flex space-x-2 justify-between">
        <Select
          v-if="props.visible && props.projectId"
          :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
          :allowClear="false"
          :fieldNames="{ label: 'name', value: 'id'}"
          :defaultActiveFirstOption="true"
          :lazy="false"
          :placeholder="t('commonComp.apis.selectCaseModal.selectService')"
          showSearch
          class="flex-1"
          @change="selectService">
          <template #option="{ name }">
            <div class="flex items-center leading-6.5 h-6.5 space-x-1.5">
              <IconText
                style="width: 16px;height: 16px;background-color: rgb(162, 222, 236);"
                text="S"
                class="flex-shrink-0" />
              <div :title="name" class="flex-1 truncate">{{ name }}</div>
            </div>
          </template>
        </Select>
        <Input
          v-model:value="apiSearchKeywords"
          class="flex-1/3"
          allowClear
          :placeholder="t('commonComp.apis.selectCaseModal.searchApi')"
          @change="handleApiKeywordsSearchChange" />
        <DropdownGroup
          v-model:activeKey="currentGroupingKey"
          :menuItems="groupingOptions"
          @click="handleGroupingKeyChange">
          <Button
            size="small">
            <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
              <Icon icon="icon-fenzu" />
              <span>{{ t('organization.group') }}</span>
            </div>
          </Button>
        </DropdownGroup>
      </div>
      <div class="h-100">
        <Tree
          v-model:selectedKeys="currentSelectedApiId"
          defaultExpandAll
          :treeData="apiListData"
          blockNode
          :showIcon="currentGroupingKey!==''"
          :height="400"
          :fieldNames="{children:'children', title:'title', key:'id'}"
          class="text-3 select-case-tree w-full">
          <template #switcherIcon="{expanded}">
            <Button size="small" class="px-0.5 h-4 mt-1.5">
              <Icon :icon="expanded ? 'icon-jian' : 'icon-jia'" />
            </Button>
          </template>
          <template #title="{summary, type, method, endpoint}">
            <div v-if="type==='group'" class="h-8 leading-8">{{ summary }}</div>
            <div v-else class="w-full">
              <div class="leading-6 truncate" :title="summary">{{ summary }}</div>
              <div class="flex-shrink-0 flex space-x-2 whitespace-nowrap h-6 leading-5 items-center">
                <HttpMethodText :value="method" />
                <span :title="endpoint" class="truncate flex-1 minw-0">{{ endpoint }}</span>
              </div>
            </div>
          </template>
        </Tree>
      </div>
    </div>
    <div class="flex-1 min-w-50">
      <div class="flex mb-1 justify-between">
        <Input
          v-model:value="caseSearchKeywords"
          :placeholder="t('actions.search')"
          allowClear
          class="w-50"
          @change="handleCaseKeywordsSearchChange" />
      </div>
      <Table
        :loading="isTableLoading"
        :rowSelection="tableRowSelection"
        :dataSource="allApiCaseData"
        :columns="tableColumns"
        :scroll="{y: 400}"
        noDataSize="small"
        rowKey="id"
        :pagination="false">
      </Table>
    </div>
  </div>
</template>
<style scoped>
:deep(.select-case-tree .ant-tree-list) .ant-tree-switcher.ant-tree-switcher-noop {
  display: none;
}

:deep(.select-case-tree .ant-tree-list) .ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal {
  @apply flex-1 min-w-0;
}
</style>

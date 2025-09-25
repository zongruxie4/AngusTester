<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { DropdownGroup, HttpMethodText, Icon, IconText, Input, Select, Table } from '@xcan-angus/vue-ui';
import { Button, Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { services, apis } from '@/api/tester';

const { t } = useI18n();

interface Props {
  visible: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  projectId: undefined
});
const emits = defineEmits<{(e: 'change', ids: string[], rows: any[]):void}>();

const apisKeywords = ref();
const caseKeywords = ref();

// 根据关键词分组
const group = (data:Array<Record<string, any>>, key: string) => {
  const res:Record<string, Array<any>> = {};
  data.forEach((item) => {
    const keyValue = item[key];
    if (!res[keyValue]) {
      res[keyValue] = [item];
    } else {
      res[keyValue].push(item);
    }
  });
  return res;
};

const ok = () => {
  if (groupedKey.value !== '') {
    const groupIds = apisList.value.map(item => item.id);
    const ids = rowSelection.value.selectedRowKeys.filter(id => !groupIds.includes(id));
    const rows = allApis.filter(row => ids.includes(row.id));
    emits('change', ids, rows);
  } else {
    const rows = allApis.filter(row => rowSelection.value.selectedRowKeys.includes(row.id));
    emits('change', rowSelection.value.selectedRowKeys, rows);
  }
};
const serviceId = ref();
const selectService = (id: string) => {
  serviceId.value = id;
  apisList.value = [];
  allApis = [];
  currentApiId.value = [];
  rowSelection.value.selectedRowKeys = [];
  ok();
  getApiList();
};

let allApis = [];
const currentApiId = ref<string[]>([]);
const getApiList = async () => {
  const [error, { data = {} }] = await services.loadApisByServicesId(serviceId.value, {
    pageNo: 1,
    pageSize: 2000,
    projectId: props.projectId,
    filters: apisKeywords.value ? [{ value: apisKeywords.value, op: 'MATCH', key: 'summary' }] : []
  });
  if (error) {
    allApis = [];
    apisList.value = [];
    allApiCases.value = [];
    currentApiId.value = [];
    ok();
    return;
  }

  allApis = data?.list || [];
  grouped(allApis);
  if (apisList.value.length && !currentApiId.value.length) {
    if (groupedKey.value !== '') {
      currentApiId.value = apisList.value?.[0]?.children?.[0]?.id ? [apisList.value?.[0]?.children?.[0]?.id] : [];
    } else {
      currentApiId.value = apisList.value?.[0]?.id ? [apisList.value?.[0]?.id] : [];
    }
  } else {
    currentApiId.value = [];
  }
};

const tableLoading = ref(false);
const allApiCases = ref([]);
const apiListSelect = async (apisId) => {
  allApiCases.value = [];
  if (!apisId) {
    return;
  }
  tableLoading.value = true;
  const [error, { data }] = await apis.loadApiCases({
    pageNo: 1,
    pageSize: 2000,
    apisId,
    filters: caseKeywords.value ? [{ value: caseKeywords.value, op: 'MATCH', key: 'name' }] : []
  });
  tableLoading.value = false;
  rowSelection.value.selectedRowKeys = [];
  ok();
  if (error) {
    return;
  }
  allApiCases.value = data?.list || [];
};

const groupedKey = ref('');

const groups = [
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

const cloumns = [
  {
    title: t('commonComp.apis.selectCaseModal.caseName'),
    dataIndex: 'name'
  },
  {
    title: t('common.method'),
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

const apisList = ref<{id: string; type?: string; children?: Record<string, any>[], name: string, key?: string}[]>([]);
const rowSelection = ref({
  selectedRowKeys: [],
  checkStrictly: false,
  onChange: (selectedRowKeys) => {
    rowSelection.value.selectedRowKeys = selectedRowKeys;
    ok();
  }
});

const grouped = (datalist = []) => {
  apisList.value = [];
  if (groupedKey.value !== '') {
    if (groupedKey.value !== 'tag') {
      const objData = group(datalist, groupedKey.value);
      Object.keys(objData).forEach(key => {
        apisList.value.push({
          type: 'group',
          summary: groupedKey.value === 'createdBy'
            ? objData[key][0].createdByName
            : groupedKey.value === 'method'
              ? objData[key][0].method
              : groupedKey.value === 'ownerId'
                ? objData[key][0].ownerName
                : key,
          id: key,
          key,
          selectable: false,
          children: [...objData[key]]
        });
      });
    } else {
      const resultObj: Record<string, any[]> = {};
      datalist.forEach((item) => {
        if (item.tags && item.tags.length) {
          item.tags.forEach(tag => {
            if (!resultObj[tag]) {
              resultObj[tag] = [item];
            } else {
              resultObj[tag].push(item);
            }
          });
        } else {
          if (!resultObj.null_tag) {
            resultObj.null_tag = [item];
          } else {
            resultObj.null_tag.push(item);
          }
        }
      });
      Object.entries(resultObj).forEach(([key, list]) => {
        apisList.value.push({
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
    apisList.value = datalist;
  }
};

const setGroupKey = (key) => {
  groupedKey.value = key;
  rowSelection.value.selectedRowKeys = [];
  ok();
  grouped(allApis);
  if (apisList.value.length && !currentApiId.value) {
    if (groupedKey.value !== '') {
      currentApiId.value = apisList.value?.[0]?.children?.[0]?.id ? [apisList.value?.[0]?.children?.[0]?.id] : [];
    } else {
      currentApiId.value = apisList.value?.[0]?.id ? [apisList.value?.[0]?.id] : [];
    }
  } else {
    currentApiId.value = [];
  }
};

const handleApisKeywordsChange = debounce(300, () => {
  getApiList();
});

const handleChangeCaseKeywordChange = debounce(300, () => {
  apiListSelect(currentApiId.value[0]);
});

onMounted(() => {
  watch(() => currentApiId.value, newValue => {
    apiListSelect(newValue[0]);
  });
});

defineExpose({
  reset: () => {
    apisList.value = [];
    allApis = [];
    allApiCases.value = [];
    rowSelection.value.selectedRowKeys = [];
    ok();
    if (serviceId.value) {
      selectService(serviceId.value);
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
          v-model:value="apisKeywords"
          class="flex-1/3"
          allowClear
          :placeholder="t('commonComp.apis.selectCaseModal.searchApi')"
          @change="handleApisKeywordsChange" />
        <DropdownGroup
          v-model:activeKey="groupedKey"
          :menuItems="groups"
          @click="setGroupKey">
          <Button
            size="small">
            <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
              <Icon icon="icon-fenzu" />
              <span>{{ t('commonComp.apis.selectCaseModal.group') }}</span>
            </div>
          </Button>
        </DropdownGroup>
      </div>
      <div class="h-100">
        <Tree
          v-model:selectedKeys="currentApiId"
          defaultExpandAll
          :treeData="apisList"
          blockNode
          :showIcon="groupedKey!==''"
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
          v-model:value="caseKeywords"
          :placeholder="t('actions.search')"
          allowClear
          class="w-50"
          @change="handleChangeCaseKeywordChange" />
      </div>
      <Table
        :loading="tableLoading"
        :rowSelection="rowSelection"
        :dataSource="allApiCases"
        :columns="cloumns"
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

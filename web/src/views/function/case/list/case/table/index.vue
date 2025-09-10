<script setup lang="ts">
import { computed, ref } from 'vue';
import { Badge } from 'ant-design-vue';
import { Dropdown, Icon, ReviewStatus, Table, TaskPriority, TestResult } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { CaseActionAuth, CaseListObj, EnabledGroup, GroupCaseListObj } from '../types';

type FilterOp = 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN';
type Filters = { key: string, value: string | boolean | string[], op: FilterOp }

interface Props {
  params: {
    pageNo: number;
    pageSize: number;
    filters?: Filters[];
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    [key: string]: any;
  };
  total: number;
  loading: boolean;
  enabledGroup: EnabledGroup;
  caseAcitonAuth: Record<string, string[]>;
  actionMenus: Record<string, any[]>;
  caseList: CaseListObj[];
  groupCaseList: GroupCaseListObj[];
  selectedRowKeys: string[];
}

const props = withDefaults(defineProps<Props>(), {
  params: undefined,
  total: 0,
  loading: false,
  enabledGroup: false,
  caseAcitonAuth: () => ({}),
  actionMenus: () => ({}),
  caseList: () => [],
  groupCaseList: () => [],
  selectedRowKeys: () => []
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:selectedRowKeys', value: string[]): void;
  (e: 'onClick', type:CaseActionAuth, value:CaseListObj):void;
  (e: 'openInfo', value:CaseListObj):void;
  (e: 'change', value:{pagination, sorter}):void;
}>();

const { t } = useI18n();

const pagination = computed(() => {
  return {
    current: props.params.pageNo,
    pageSize: props.params.pageSize,
    total: +props.total,
    showTotal: (_total: number) => { return props.enabledGroup ? t('functionCase.tableView.totalGroups', { total: _total }) : t('functionCase.tableView.totalItems', { total: _total }); }
  };
});

const tableChange = (pagination, _filters, sorter) => {
  emits('change', { pagination: pagination, sorter: sorter });
};

const tableColumns = computed(() => [
  props.enabledGroup && {
    title: '',
    width: 30,
    dataIndex: 'expend'
  },
  {
    title: t('functionCase.tableView.code'),
    dataIndex: 'code',
    width: '11%',
    customCell: () => {
      return { style: 'white-space:nowrap;position: relative;' };
    }
  },
  {
    title: t('functionCase.tableView.caseName'),
    dataIndex: 'name',
    width: '23%',
    ellipsis: true,
    customCell: () => {
      return { style: 'white-space: nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.priority'),
    dataIndex: 'priority',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.reviewStatus'),
    dataIndex: 'reviewStatus',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.testResult'),
    dataIndex: 'testResult',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.creator'),
    dataIndex: 'createdByName',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.tester'),
    dataIndex: 'testerName',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.lastUpdateTime'),
    dataIndex: 'lastModifiedDate',
    sorter: true,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('functionCase.tableView.actions'),
    dataIndex: 'action',
    width: 140
  }
].filter(Boolean));
// 表格视图下 所有所选中的用例的Ids，table功能
const selectedRowKeys = ref<string[]>([]);

// 父表格SelectChange
const onSelectChange = (_selectedRowKeys) => {
  selectedRowKeys.value = _selectedRowKeys;
  emits('update:selectedRowKeys', _selectedRowKeys.filter((id) => {
    if (props.enabledGroup) {
      return !props.groupCaseList.map(group => group.id).includes(id);
    }
    return true;
  }));
};

const handleClick = async (type: CaseActionAuth, value: CaseListObj) => {
  emits('onClick', type, value);
};

const handleViewInfo = (value:CaseListObj) => {
  emits('openInfo', value);
};

defineExpose({
  selectedRowKeys,
  onSelectChange
});
</script>
<template>
  <Table
    :dataSource="props.enabledGroup?props.groupCaseList:props.caseList"
    :loading="props.loading"
    :columns="tableColumns"
    :pagination="pagination"
    :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange, checkStrictly: false }"
    rowKey="id"
    size="small"
    @change="tableChange">
    <template #bodyCell="{ column, text, record }">
      <template v-if="column.dataIndex === 'code'">
        <div
          v-if="record.groupName "
          class="absolute flex items-center top-0"
          style="max-width: 500px; line-height: 38px;">
          <Icon
            icon="icon-mokuai"
            class="mr-1 text-4"
            style="height: 38px;" />
          <div class="truncate" style="z-index: 999;max-width: 500px;">{{ record.groupName }}</div>
        </div>
        <div v-else>{{ text }}</div>
      </template>
      <template v-if="column.dataIndex === 'name'">
        <span v-if="!record.groupName" class="flex items-center">
          <Icon
            icon="icon-gongnengyongli"
            class="mr-1.5 text-4 -mt-0.5" />
          <a
            class="text-theme-special"
            :title="record.name"
            @click="handleViewInfo(record)">
            {{ record.name }}
          </a>
        </span>
        <span v-else></span>
      </template>
      <template v-if="column.dataIndex === 'priority'">
        <TaskPriority v-if="!record.groupName" :value="record?.priority" />
        <span v-else></span>
      </template>
      <template v-if="column.dataIndex === 'reviewStatus'">
        <span v-if="record.groupName"></span>
        <template v-else-if="text">
          <ReviewStatus :value="text" />
        </template>
        <template v-else>
          --
        </template>
      </template>
      <template v-if="column.dataIndex === 'testResult'">
        <TestResult :value="text" />
      </template>
      <template v-if="column.dataIndex === 'source'">
        {{ text?.message }}
      </template>
      <template v-if="column.dataIndex === 'createdByName'">
        <span v-if="record.groupName"></span>
        <template v-else>{{ text || '--' }}</template>
      </template>
      <template v-if="column.dataIndex === 'testerName'">
        <span v-if="record.groupName"></span>
        <template v-else>{{ text || '--' }}</template>
      </template>
      <template v-if="column.dataIndex === 'enabled'">
        <Badge
          v-if="record.enabled"
          status="success"
          :text="t('enable')" />
        <Badge
          v-else
          status="error"
          :text="t('disable')" />
      </template>

      <div v-else-if="column.dataIndex === 'action' && !record.groupName" class="flex items-center">
        <Button
          :disabled="!(caseAcitonAuth[record.id] || []).includes('edit')"
          type="text"
          size="small"
          class="flex items-center px-0 mr-2.5"
          @click="handleClick('edit',record)">
          <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
          <span>{{ t('actions.edit') }}</span>
        </Button>

        <Button
          :disabled="!(caseAcitonAuth[record.id] || [])?.includes('delete')"
          type="text"
          size="small"
          class="flex items-center px-0 mr-2.5"
          @click="handleClick('delete',record)">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Dropdown
          :menuItems="props.actionMenus[record.id]"
          :permissions="caseAcitonAuth[record.id] || []"
          @click="handleClick($event.key, record)">
          <Button
            type="text"
            size="small"
            class="flex items-center px-0">
            <Icon icon="icon-gengduo" />
          </Button>
        </Dropdown>
      </div>
    </template>
  </Table>
</template>
<style scoped>
.top-count-element {
  height: 0;
  transition: all 0.5s ease-in-out;
}

.top-count-element-expanded {
  height: auto;
  transition: height 0.5s ease-in-out; /* 使用新的过渡动画属性 */
}

.fade-enter-from {
  height: 0;
  opacity: 0;
}

.fade-enter-to {
  height: 28px;
  opacity: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease-in-out;
}

.fade-leave-from {
  height: 28px;
  opacity: 1;
}

.fade-leave-to {
  height: 0;
  opacity: 0;
}

</style>

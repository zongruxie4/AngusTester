<script setup lang="ts">
import { computed, ref } from 'vue';
import { Badge } from 'ant-design-vue';
import { Dropdown, Icon, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { PageQuery } from '@xcan-angus/infra';

import { CaseActionAuth, CaseDetailChecked, EnabledGroup, GroupCaseList } from '../types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

interface Props {
  params: PageQuery;
  total: number;
  loading: boolean;
  enabledGroup: EnabledGroup;
  caseActionAuth: Record<string, string[]>;
  actionMenus: Record<string, any[]>;
  caseList: CaseDetailChecked[];
  groupCaseList: GroupCaseList[];
  selectedRowKeys: string[];
}

const props = withDefaults(defineProps<Props>(), {
  params: undefined,
  total: 0,
  loading: false,
  enabledGroup: false,
  caseActionAuth: () => ({}),
  actionMenus: () => ({}),
  caseList: () => [],
  groupCaseList: () => [],
  selectedRowKeys: () => []
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:selectedRowKeys', value: string[]): void;
  (e: 'onClick', type:CaseActionAuth, value:CaseDetailChecked):void;
  (e: 'openInfo', value:CaseDetailChecked):void;
  (e: 'change', value:{pagination, sorter}):void;
}>();

const { t } = useI18n();

const pagination = computed(() => {
  return {
    current: props.params.pageNo,
    pageSize: props.params.pageSize,
    total: +props.total,
    showTotal: (_total: number) => {
      return props.enabledGroup
        ? t('functionCase.tableView.totalGroups', { total: _total })
        : t('functionCase.tableView.totalItems', { total: _total });
    }
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
    title: t('common.code'),
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
    title: t('common.priority'),
    dataIndex: 'priority',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.reviewStatus'),
    dataIndex: 'reviewStatus',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.testResult'),
    dataIndex: 'testResult',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.tester'),
    dataIndex: 'testerName',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    sorter: true,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('common.actions'),
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

const handleClick = async (type: CaseActionAuth, value: CaseDetailChecked) => {
  emits('onClick', type, value);
};

const handleViewInfo = (value:CaseDetailChecked) => {
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
          :disabled="!(caseActionAuth[record.id] || []).includes('edit')"
          type="text"
          size="small"
          class="flex items-center px-0 mr-2.5"
          @click="handleClick('edit',record)">
          <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
          <span>{{ t('common.edit') }}</span>
        </Button>

        <Button
          :disabled="!(caseActionAuth[record.id] || [])?.includes('delete')"
          type="text"
          size="small"
          class="flex items-center px-0 mr-2.5"
          @click="handleClick('delete',record)">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Dropdown
          :menuItems="props.actionMenus[record.id]"
          :permissions="caseActionAuth[record.id] || []"
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

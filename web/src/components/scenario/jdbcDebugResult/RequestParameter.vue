<script setup lang="ts">
import { computed, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, NoData, Table, Icon } from '@xcan-angus/vue-ui';

import { useI18n } from 'vue-i18n';
import { ExecContent, ExecInfo } from './PropsType';
const { t } = useI18n();

export interface Props {
  type: ExecInfo['task']['pipelines'][number]['type']['value'];
  value: ExecContent;
}

const props = withDefaults(defineProps<Props>(), {
  type: undefined,
  value: undefined
});

const panels:{id:string;name:string;key:'SQL'|'parameters'|'result'}[] = [
  {
    id: utils.uuid(),
    name: 'SQL',
    key: 'SQL'
  },
  {
    id: utils.uuid(),
    name: t('xcan_debugResRequestParameter.parameters'),
    key: 'parameters'
  },
  {
    id: utils.uuid(),
    name: t('xcan_debugResRequestParameter.result'),
    key: 'result'
  }
];

const activeKeys = ref<string[]>([panels[0].id, panels[1].id, panels[2].id]);

const arrowChange = (id: string) => {
  if (activeKeys.value.includes(id)) {
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    activeKeys.value.push(id);
  }
};

const sql = computed(() => {
  return props.value?.content?.request0?.sql;
});

const updateCount = computed(() => {
  return props.value?.content?.response?.updateCount || 0;
});

const parameters = computed(() => {
  return props.value?.content?.request0?.arguments?.map(item => {
    return {
      ...item,
      inout: item.inout?.value
    };
  }) || [];
});

const parameterColumns = [
  {
    title: 'type',
    dataIndex: 'type',
    key: 'type'
  },
  {
    title: 'inout',
    dataIndex: 'inout',
    key: 'inout'
  },
  {
    title: 'value',
    dataIndex: 'actualValue',
    key: 'actualValue'
  }
];

const rows = computed(() => {
  return props.value?.content?.response?.rows || [];
});

const columns = computed(() => {
  return props.value?.content?.response?.columnLabels?.map((item) => {
    return {
      title: item,
      dataIndex: item,
      key: item,
      ellipsis: true,
      customRender: ({ text }) => {
        if (typeof text === 'object') {
          return JSON.stringify(text);
        }

        return text;
      }
    };
  });
});
</script>
<template>
  <Collapse
    :activeKey="activeKeys"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="tabs-collapse">
    <CollapsePanel
      v-for="item in panels"
      :key="item.id"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center">
          <Arrow :open="activeKeys.includes(item.id)" @change="arrowChange(item.id)" />
          <div class="ml-1 font-bold">{{ item.name }}</div>
        </div>
      </template>
      <template v-if="item.key==='SQL'">
        <div v-if="sql" class="pb-2 pl-2 pt-1 space-y-1 whitespace-pre-line break-all">
          {{ sql }}
        </div>
        <NoData v-else size="small" />
      </template>
      <template v-else-if="item.key==='parameters'">
        <Table
          v-if="parameters?.length"
          :dataSource="parameters"
          :columns="parameterColumns"
          :pagination="false"
          class="pb-5 pl-2 pt-2 space-y-1 w-1/2 min-w-100 max-w-200" />
        <NoData v-else size="small" />
      </template>
      <template v-else-if="['SELECT','PREPARED_SELECT'].includes(props.type)&&item.key==='result'">
        <div v-if="rows?.length" class="pl-2 pt-1 space-y-1 min-w-100">
          <div class="flex items-center mb-1.5 text-theme-sub-content">
            <Icon
              icon="icon-tishi1"
              class="text-3.5"
              style="color:#a6ceff;" />
            <span class="ml-1">{{ t('xcan_debugResRequestParameter.maxDisplayRecords') }}</span>
          </div>
          <Table :dataSource="rows" :columns="columns" />
        </div>
        <NoData v-else size="small" />
      </template>
      <template v-else-if="['UPDATE','PREPARED_UPDATE'].includes(props.type)&&item.key==='result'">
        <div class="pl-2 pt-1 pb-2 space-y-1">
          {{ t('xcan_debugResRequestParameter.affectedRows') }}<span class="mx-0.5">{{ updateCount }}</span>{{ t('xcan_debugResRequestParameter.rows') }}
        </div>
      </template>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 6px 0;
}

:deep(.ant-table.ant-table-small) {
  overflow-x: auto;
}
</style>

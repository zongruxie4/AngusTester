<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { IconTask, Input, Modal, Table } from '@xcan-angus/vue-ui';
import { http, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

interface Props {
  // planId: string;
  visible: boolean;
  // baselineId?: string;
  projectId: string;
  action: string;
  title: string;
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined,
  action: '',
  title: '选择任务'
});
const emit = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok', value: string[], rowValue: ReviewCaseInfo[]):void}>();
const ModuleTree = defineAsyncComponent(() => import('@/components/module/treeSelector/index.vue'));

const selectedIds = ref<string[]>([]);
const selectRows = ref([]);
const data = ref([]);
const showData = ref([]);
const loading = ref(false);
const moduleId = ref('');
const keywords = ref();

const rowSelection = {
  onChange: (selectedRowKeys, selectedRows) => {
    selectedIds.value = selectedRowKeys;
    selectRows.value = selectedRows;
  }
};

const cancel = () => {
  emit('update:visible', false);
};

const ok = () => {
  if (selectedIds.value.length) {
    emit('ok', selectedIds.value, selectRows.value);
  } else {
    cancel();
  }
};

const loadCases = async () => {
  if (!props.projectId) {
    return;
  }
  loading.value = true;
  const [error, resp] = await http.get(props.action, {
    moduleId: moduleId.value
  });
  loading.value = false;
  if (error) {
    return;
  }
  data.value = resp?.data || [];
  showData.value = data.value.filter(item => (item.name || '').includes(keywords.value || '') || (item.code || '').includes(keywords.value || ''));
};

watch(() => props.visible, (newValue) => {
  if (newValue) {
    selectedIds.value = [];
    loadCases();
  }
}, {
  immediate: true
});

watch(() => moduleId.value, () => {
  selectedIds.value = [];
  loadCases();
});

const handleFilter = debounce(duration.search, () => {
  selectedIds.value = [];
  showData.value = data.value.filter(item => (item.name).includes(keywords.value || '') || (item.code || '').includes(keywords.value || ''));
});

const columns = [
  {
    title: '编号',
    dataIndex: 'code',
    width: '20%'
  },
  {
    title: '名称',
    dataIndex: 'name'

  },
  {
    title: '状态',
    dataIndex: 'status',
    width: '10%'
  },
  {
    title: '类型',
    dataIndex: 'taskType',
    width: '15%'
  }
];

</script>
<template>
  <Modal
    :title="props.title"
    :visible="props.visible"
    :width="1000"
    :loading="loading"
    @cancel="cancel"
    @ok="ok">
    <div v-if="props.visible" class="flex">
      <div class="w-50 h-144.5 overflow-y-auto">
        <ModuleTree
          v-model:moduleId="moduleId"
          title="任务"
          :projectId="props.projectId" />
      </div>
      <div class="flex-1 ml-2">
        <Input
          v-model:value="keywords"
          placeholder="查询名称、编码"
          class="w-100"
          @change="handleFilter" />
        <Table
          :columns="columns"
          :dataSource="showData"
          :rowSelection="rowSelection"
          :pagination="false"
          :scroll="{y: 500}"
          class="mt-2"
          rowKey="id"
          noDataSize="small">
          <template #bodyCell="{record, column}">
            <!-- <template v-if="column.dataIndex === 'reviewStatus'">
            <ReviewStatus :value="record.reviewStatus" />
          </template> -->
            <template v-if="column.dataIndex === 'status'">
              <!-- <testResult :value="record.testResult" /> -->
              {{ record.status?.message }}
            </template>
            <template v-if="column.dataIndex === 'taskType'">
              <!-- <testResult :value="record.testResult" /> -->
              <IconTask :value="record.taskType?.value" />
              {{ record.taskType?.message }}
            </template>
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>

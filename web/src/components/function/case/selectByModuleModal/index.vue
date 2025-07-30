<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { Input, Modal, ReviewStatus, Table, TestResult } from '@xcan-angus/vue-ui';
import { http, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { ReviewCaseInfo } from '@/views/function/review/edit/PropsType';

interface Props {
  // planId: string;
  visible: boolean;
  // baselineId?: string;
  projectId: string;
  action: string;
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined,
  action: ''
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
    dataIndex: 'code'
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: '40%'
  },
  {
    title: '评审状态',
    dataIndex: 'reviewStatus'
  },
  {
    title: '测试结果',
    dataIndex: 'testResult'
  },
  {
    title: '测试人',
    dataIndex: 'testerName'
  }
];

const rowSelection = ref({
  onChange: (selectedRowKeys, selectedRows) => {
    selectedIds.value = selectedRowKeys;
    selectRows.value = selectedRows;
  }
});

</script>
<template>
  <Modal
    title="选择用例"
    :visible="props.visible"
    :width="1000"
    :loading="loading"
    @cancel="cancel"
    @ok="ok">
    <div class="flex">
      <div class="w-50 h-144.5 overflow-y-auto">
        <ModuleTree
          v-model:moduleId="moduleId"
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
            <template v-if="column.dataIndex === 'reviewStatus'">
              <ReviewStatus :value="record.reviewStatus" />
            </template>
            <template v-if="column.dataIndex === 'testResult'">
              <TestResult :value="record.testResult" />
            </template>
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>

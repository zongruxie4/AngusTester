<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { funcPlan } from '@/api/tester';

import { BaselineCaseInfo } from '@/views/function/baseline/types';

import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

const ModuleTree = defineAsyncComponent(() => import('@/views/function/baseline/case/list/ModuleTree.vue'));

interface Props {
  planId: string;
  visible: boolean;
  baselineId?: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok', value: string[], rowValue: BaselineCaseInfo[]):void}>();

const selectedIds = ref<string[]>([]);
const selectRows = ref<BaselineCaseInfo[]>([]);
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
  if (!props.planId) {
    return;
  }
  loading.value = true;
  const [error, resp] = await funcPlan.getCaseNotEstablishedBaseline(props.planId, {
    baselineId: props.baselineId,
    moduleId: moduleId.value
  });
  loading.value = false;
  if (error) {
    return;
  }
  data.value = resp?.data || [];
  showData.value = data.value.filter(item => (item.name || '').includes(keywords.value || '') || (item.code || '')
    .includes(keywords.value || ''));
};

const handleFilter = debounce(duration.search, () => {
  selectedIds.value = [];
  showData.value = data.value.filter(item => (item.name).includes(keywords.value || '') || (item.code || '').includes(keywords.value || ''));
});

const columns = [
  {
    title: t('functionBaseline.editForm.code'),
    dataIndex: 'code'
  },
  {
    title: t('functionBaseline.editForm.caseName'),
    dataIndex: 'name',
    width: '40%'
  },
  {
    title: t('functionBaseline.editForm.reviewStatus'),
    dataIndex: 'reviewStatus'
  },
  {
    title: t('functionBaseline.editForm.testResult'),
    dataIndex: 'testResult'
  },
  {
    title: t('functionBaseline.editForm.tester'),
    dataIndex: 'testerName'
  }
];

const rowSelection = ref({
  onChange: (selectedRowKeys, selectedRows) => {
    selectedIds.value = selectedRowKeys;
    selectRows.value = selectedRows;
  }
});

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
</script>
<template>
  <Modal
    :title="t('functionBaseline.editForm.selectCase')"
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
          :placeholder="t('functionBaseline.editForm.queryNameCode')"
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

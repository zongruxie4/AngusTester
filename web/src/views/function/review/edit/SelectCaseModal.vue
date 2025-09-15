<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { Input, Modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { funcPlan } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ReviewCaseInfo } from '@/views/function/review/types';

interface Props {
  planId: string;
  visible: boolean;
  reviewId?: string;
  projectId: string;
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  reviewId: undefined,
  projectId: undefined
});

const { t } = useI18n();
const emit = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok', value: string[], rowValue: ReviewCaseInfo[]):void}>();

const ModuleTree = defineAsyncComponent(() => import('@/views/function/review/edit/ModuleTree.vue'));
const selectedIds = ref<string[]>([]);
const selectRows = ref<ReviewCaseInfo[]>([]);
const data = ref([]);
const showData = ref([]);
const loading = ref(false);
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

const moduleId = ref('');

const loadCases = async () => {
  if (!props.planId) {
    return;
  }
  loading.value = true;
  const [error, resp] = await funcPlan.getNotReviewedPlan(props.planId, {
    reviewId: props.reviewId,
    moduleId: moduleId.value
  });
  loading.value = false;
  if (error) {
    return;
  }
  data.value = resp.data || [];
  showData.value = data.value.filter(item => (item.name).includes(keywords.value || '') || (item.code || '').includes(keywords.value || ''));
};

watch(() => moduleId.value, () => {
  selectedIds.value = [];
  loadCases();
});

watch(() => props.visible, (newValue) => {
  if (newValue) {
    selectedIds.value = [];
    loadCases();
  }
}, {
  immediate: true
});

const handleFilter = debounce(duration.search, () => {
  selectedIds.value = [];
  showData.value = data.value.filter(item => (item.name).includes(keywords.value || '') || (item.code || '').includes(keywords.value || ''));
});

const columns = [
  {
    title: t('caseReview.editForm.code'),
    dataIndex: 'code'
  },
  {
    title: t('caseReview.editForm.name'),
    dataIndex: 'name',
    width: '40%'
  },
  {
    title: t('caseReview.editForm.tester'),
    dataIndex: 'testerName'
  },
  {
    title: t('caseReview.editForm.developer'),
    dataIndex: 'developerName'
  },
  {
    title: t('caseReview.editForm.reviewStatus'),
    dataIndex: 'reviewStatus'
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
    :title="t('caseReview.editForm.selectCase')"
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
          :placeholder="t('caseReview.editForm.queryNameCode')"
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
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>

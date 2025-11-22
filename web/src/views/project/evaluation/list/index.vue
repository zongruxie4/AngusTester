<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { AsyncComponent, notification, Spin, modal } from '@xcan-angus/vue-ui';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { evaluation } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { EvaluationDetail } from '../types';

const { t } = useI18n();

// Async components
const Introduce = defineAsyncComponent(() => import('@/views/project/evaluation/list/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/project/evaluation/list/SearchPanel.vue'));
const List = defineAsyncComponent(() => import('@/views/project/evaluation/list/List.vue'));

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const addTabPane = inject<(data: any) => void>('addTabPane', () => ({}));

// Reactive data
const isDataLoaded = ref(false);
const isLoading = ref(false);
const searchedFlag = ref(false);

// Search and pagination state
const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pageNo = ref(1);
const pageSize = ref(4);
const pageSizeOptions = ['4', '10', '15', '20', '30'];

// Data state
const total = ref(0);
const dataList = ref<EvaluationDetail[]>([]);

/**
 * Refreshes the data list and resets pagination
 */
const refresh = () => {
  pageNo.value = 1;
  loadData();
};

/**
 * Handles search panel parameter changes and reloads data
 * @param data - Search parameters from search panel
 */
const handleSearchChange = (data) => {
  searchPanelParams.value = data;
  pageNo.value = 1;
  loadData();
};

/**
 * Handles pagination changes and reloads data
 * @param _pageNo - New page number
 * @param _pageSize - New page size
 */
const handlePaginationChange = (_pageNo: number, _pageSize: number) => {
  pageNo.value = _pageNo;
  pageSize.value = _pageSize;
  loadData();
};

/**
 * Loads evaluation list data from API with search and pagination parameters
 */
const loadData = async () => {
  isLoading.value = true;
  const params: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    ...searchPanelParams.value
  };

  const [error, res] = await evaluation.getEvaluationList(params);
  isDataLoaded.value = true;
  isLoading.value = false;

  searchedFlag.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    dataList.value = [];
    return;
  }

  const data = res?.data;
  if (data) {
    total.value = +data.total;
    dataList.value = (data.list || []) as EvaluationDetail[];
  }
};

/**
 * Deletes an evaluation with confirmation
 * @param evaluationData - The evaluation data to delete
 */
const handleDeleteEvaluation = async (evaluationData: EvaluationDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: evaluationData.name }),
    async onOk () {
      const id = evaluationData.id;
      const [error] = await evaluation.deleteEvaluation(id);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      refresh();
      deleteTabPane([String(id)]);
    }
  });
};

/**
 * Navigates to evaluation detail page
 * @param evaluationData - The evaluation data
 */
const handleViewDetail = (evaluationData: EvaluationDetail) => {
  addTabPane({
    _id: String(evaluationData.id) + '-detail',
    value: 'evaluationDetail',
    data: { _id: String(evaluationData.id), id: String(evaluationData.id) }
  });
};

/**
 * Navigates to evaluation edit page
 * @param evaluationData - The evaluation data
 */
const handleEditEvaluation = (evaluationData: EvaluationDetail) => {
  addTabPane({
    _id: String(evaluationData.id),
    value: 'evaluationEdit',
    noCache: true,
    data: { _id: String(evaluationData.id), id: String(evaluationData.id) }
  });
};

/**
 * Generates evaluation result
 * @param evaluationData - The evaluation data
 */
const handleGenerateResult = async (evaluationData: EvaluationDetail) => {
  const id = evaluationData.id;
  isLoading.value = true;
  const [error] = await evaluation.generateResult(id);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('evaluation.actions.generateResultSuccess'));
  refresh();
};

/**
 * Resets pagination and clears data list
 */
const resetData = () => {
  pageNo.value = 1;
  dataList.value = [];
};

// Lifecycle hooks
onMounted(() => {
  watch(() => props.projectId, () => {
    resetData();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }
    loadData();
  }, { immediate: false });
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('evaluation.addedEvaluations') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('evaluation.noEvaluations') }}</span>
            <a
              class="router-link flex-1 truncate cursor-pointer"
              @click="addTabPane({
                _id: 'new-evaluation',
                name: t('evaluation.actions.addEvaluation'),
                value: 'evaluationEdit',
                noCache: true,
                data: { _id: 'new-evaluation' }
              })">
              {{ t('evaluation.actions.addEvaluation') }}
            </a>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            :userId="props.userInfo?.id"
            @change="handleSearchChange"
            @refresh="refresh" />

          <List
            :dataList="dataList"
            :loading="isLoading"
            :total="total"
            :pageNo="pageNo"
            :pageSize="pageSize"
            :pageSizeOptions="pageSizeOptions"
            @paginationChange="handlePaginationChange"
            @viewDetail="handleViewDetail"
            @editEvaluation="handleEditEvaluation"
            @deleteEvaluation="handleDeleteEvaluation"
            @generateResult="handleGenerateResult" />
        </template>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.router-link {
  color: #1890ff;
  cursor: pointer;
}

.router-link:hover {
  text-decoration: underline;
}
</style>

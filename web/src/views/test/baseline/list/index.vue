<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal, notification, Spin } from '@xcan-angus/vue-ui';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { test } from '@/api/tester';
import { TestMenuKey } from '@/views/test/menu';

import { BaselineDetail } from '@/views/test/baseline/types';
import { BasicProps } from '@/types/types';

// Async Components
const SearchPanel = defineAsyncComponent(() => import('@/views/test/baseline/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/test/baseline/list/Introduce.vue'));
const List = defineAsyncComponent(() => import('@/views/test/baseline/list/List.vue'));

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// Injected Dependencies
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// Reactive Data
const isDataLoaded = ref(false);
const isLoading = ref(false);
const hasSearchFilters = ref(false);
const searchParameters = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});

const currentPageNo = ref(1);
const currentPageSize = ref(4);
const totalCount = ref(0);
const baselineList = ref<BaselineDetail[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

// Pagination Configuration
const pageSizeOptions = ['4', '10', '15', '20', '30'];

/**
 * Handle refresh button click
 */
const handleRefreshClick = () => {
  currentPageNo.value = 1;
  permissionsMap.value.clear();
  loadBaselineData();
};

/**
 * Handle search parameters change
 * @param data - Search parameters
 */
const handleSearchParametersChange = (data) => {
  currentPageNo.value = 1;
  searchParameters.value = data;
  loadBaselineData();
};

/**
 * Handle baseline establishment
 * @param data - Baseline detail data
 */
const handleBaselineEstablishment = async (data: BaselineDetail) => {
  isLoading.value = true;
  const id = data.id;
  const [error] = await test.establishBaseline(id);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('testCaseBaseline.messages.baselineEstablished'));
  await loadBaselineData();
};

/**
 * Handle baseline deletion
 * @param data - Baseline detail data
 */
const handleBaselineDeletion = async (data: BaselineDetail) => {
  modal.confirm({
    content: t('testCaseBaseline.messages.confirmDeleteBaseline', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await test.deleteBaseline([id]);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      await loadBaselineData();

      deleteTabPane([id]);
    }
  });
};

/**
 * Handle pagination change
 * @param pageNo - Page number
 * @param pageSize - Page size
 */
const handlePaginationChange = (pageNo: number, pageSize: number) => {
  currentPageNo.value = pageNo;
  currentPageSize.value = pageSize;
  loadBaselineData();
};

/**
 * Load baseline list data
 */
const loadBaselineData = async () => {
  isLoading.value = true;
  const params: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: currentPageNo.value,
    pageSize: currentPageSize.value,
    ...searchParameters.value
  };

  const [error, res] = await test.getBaselineList(params);
  isDataLoaded.value = true;
  isLoading.value = false;

  hasSearchFilters.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    baselineList.value = [];
    return;
  }

  const data = res?.data;
  if (data) {
    totalCount.value = +data.total;
    baselineList.value = (data.list || [] as BaselineDetail[]);
  }
};

/**
 * Reset component state
 */
const resetComponentState = () => {
  currentPageNo.value = 1;
  baselineList.value = [];
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  watch(() => props.projectId, () => {
    resetComponentState();
    loadBaselineData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadBaselineData();
  }, { immediate: false });
});
</script>
<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />
    <div class="text-3.5 font-semibold mb-1">{{ t('testCaseBaseline.addedBaselines') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!hasSearchFilters && baselineList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('testCaseBaseline.noBaselinesAdded') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/test#${TestMenuKey.BASELINE}?type=ADD`">
              {{ t('testCaseBaseline.actions.addBaseline') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            @refresh="handleRefreshClick"
            @change="handleSearchParametersChange" />

          <List
            :baselineList="baselineList"
            :loading="isLoading"
            :total="totalCount"
            :currentPageNo="currentPageNo"
            :currentPageSize="currentPageSize"
            :pageSizeOptions="pageSizeOptions"
            :isAdmin="true"
            @paginationChange="handlePaginationChange"
            @establishBaseline="handleBaselineEstablishment"
            @deleteBaseline="handleBaselineDeletion"
            @refresh="handleRefreshClick" />
        </template>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

:deep(.ant-progress-text) {
  margin-left: 8px;
}

:deep(.ant-progress-inner) {
  background-color: #d5d5d5;
}

.router-link {
  color: #1890ff;
  cursor: pointer;
}

.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-popover-inner-content) {
  padding: 8px 4px;
}
</style>

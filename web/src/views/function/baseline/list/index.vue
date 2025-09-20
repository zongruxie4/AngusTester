<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Pagination } from 'ant-design-vue';
import { Colon, Icon, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { func } from '@/api/tester';

import { BaselineDetail } from '@/views/function/baseline/types';
import { BasicProps } from '@/types/types';

import SearchPanel from '@/views/function/baseline/list/SearchPanel.vue';
import RichText from '@/components/richEditor/textContent/index.vue';

// Async Components
const Introduce = defineAsyncComponent(() => import('@/views/function/baseline/list/Introduce.vue'));

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
const currentPageNo = ref(1);
const currentPageSize = ref(5);
const searchParameters = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const totalCount = ref(0);
const baselineList = ref<BaselineDetail[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

// Pagination Configuration
const pageSizeOptions = ['5', '10', '15', '20', '30'];

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
  const [error] = await func.establishBaseline(id);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('functionBaseline.list.baselineEstablished'));
  await loadBaselineData();
};

/**
 * Handle baseline deletion
 * @param data - Baseline detail data
 */
const handleBaselineDeletion = async (data: BaselineDetail) => {
  modal.confirm({
    content: t('functionBaseline.list.confirmDeleteBaseline', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await func.deleteBaseline([id]);
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.list.baselineDeletedSuccess'));
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

  const [error, res] = await func.getBaselineList(params);
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
    <div class="text-3.5 font-semibold mb-1">{{ t('functionBaseline.list.createdBaselines') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!hasSearchFilters && baselineList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('functionBaseline.list.noBaselinesAdded') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/function#baseline?type=ADD`">
              {{ t('functionBaseline.list.addBaseline') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            @refresh="handleRefreshClick"
            @change="handleSearchParametersChange" />

          <NoData v-if="baselineList.length === 0" class="flex-1" />

          <template v-else>
            <div
              v-for="(item, index) in baselineList"
              :key="item.id"
              class="mb-3.5 border border-theme-text-box rounded">
              <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
                <div class="truncate" style="width:35%;max-width: 360px;">
                  <RouterLink
                    class="router-link flex-1 truncate"
                    :title="item.name"
                    :to="`/function#baseline?id=${item.id}`">
                    {{ item.name }}
                  </RouterLink>
                </div>

                <div class="flex">
                  <div
                    class="text-theme-sub-content text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
                    <Icon
                      v-if="item.established"
                      icon="icon-duihao-copy"
                      class="mr-1" />
                    <div>{{ item.established ? t('functionBaseline.list.established') : t('functionBaseline.list.notEstablished') }}</div>
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap space-x-8">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>ID</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.id || "--" }}</div>
                  </div>

                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('functionBaseline.list.testPlan') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.planName || "--" }}</div>
                  </div>
                </div>

                <div class="flex ml-8 mt-3">
                  <div
                    class="truncate text-theme-content"
                    style="max-width: 100px;"
                    :title="item.lastModifiedByName">
                    {{ item.lastModifiedByName }}
                  </div>
                  <div class="mx-2 whitespace-nowrap">{{ t('functionBaseline.list.modifiedBy') }}</div>
                  <div class="whitespace-nowrap text-theme-content">
                    {{ item.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex justify-between items-end text-3 my-2.5 relative">
                <div class="truncate mr-8" :title="item.descriptionText || ''">
                  <template v-if="item.description">
                    <RichText
                      v-model:textValue="item.descriptionText"
                      :value="item.description"
                      :emptyText="t('functionBaseline.list.noDescription')" />
                  </template>
                  <span v-else class="text-text-sub-content">
                    {{ t('functionBaseline.list.noDescription') }}
                  </span>
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/function#baseline?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.edit') }}</span>
                  </RouterLink>

                  <Button
                    v-if="!item.established"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="handleBaselineEstablishment(item)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.establishBaseline') }}</span>
                  </Button>

                  <Button
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="handleBaselineDeletion(item)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.delete') }}</span>
                  </Button>
                </div>
              </div>
            </div>

            <Pagination
              v-if="total > 5"
              :current="currentPageNo"
              :pageSize="currentPageSize"
              :pageSizeOptions="pageSizeOptions"
              :total="totalCount"
              :hideOnSinglePage="false"
              showSizeChanger
              size="default"
              class="text-right"
              @change="handlePaginationChange" />
          </template>
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

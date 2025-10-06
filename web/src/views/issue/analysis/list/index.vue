<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, notification } from '@xcan-angus/vue-ui';
import { TemplateIconConfig } from './types';
import { Button, Tag } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import { analysis } from '@/api/tester';
import { EnumMessage } from '@xcan-angus/infra';
import { AnalysisDataSource, AnalysisTaskTemplate, AnalysisTaskTemplateDesc } from '@/enums/enums';
import { BasicProps } from '@/types/types';
import { AnalysisInfo } from '@/views/issue/analysis/types';

// Lazy load template selection component
const Introduce = defineAsyncComponent(() => import('@/views/issue/analysis/list/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/issue/analysis/list/SearchPanel.vue'));
const TemplateSelectList = defineAsyncComponent(() => import('@/views/issue/analysis/list/TemplateSelect.vue'));

// Props and Basic Setup
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: () => ({ id: '' }),
  refreshNotify: 0,
  onShow: false
});

const { t } = useI18n();
const dataListContainerRef = ref();

const addTabPane = inject('addTabPane', (value) => value);

/**
 * Pagination configuration for analysis list
 */
const paginationConfig = { currentPage: 1, pageSize: 20, total: 0 };

/**
 * Template descriptions for display
 */
const templateDescriptions = ref<EnumMessage<AnalysisTaskTemplateDesc>[]>([]);

/**
 * Available analysis templates
 */
const availableTemplates = ref<EnumMessage<AnalysisTaskTemplate>[]>([]);

/**
 * Search filters applied to the analysis list
 */
const searchFilters = ref<{ op: string; key: string; value: any }[]>([]);

/**
 * Sorting configuration for the analysis list
 */
const sortingConfig = ref({
  orderBy: undefined,
  orderSort: undefined
});

/**
 * Currently selected template filter
 */
const selectedTemplate = ref('');

/**
 * List of analysis items to display
 */
const analysisList = ref<Array<AnalysisInfo>>([]);

/**
 * Loading state for API calls
 */
const isLoading = ref(false);

/**
 * Get the display name of the currently selected template
 * @returns {string} Template display name
 */
const getCurrentTemplateName = () => {
  return availableTemplates.value.find(item => item.value === selectedTemplate.value)?.message;
};

/**
 * Create a mapping of template values to their descriptions
 * @returns {Record<string, string>} Template description mapping
 */
const templateDescriptionMap = computed(() => {
  const descriptionMap = {};
  templateDescriptions.value.forEach(item => {
    descriptionMap[item.value] = item.message;
  });
  return descriptionMap;
});

/**
 * Build query parameters for API calls
 * @returns {object} API query parameters
 */
const buildQueryParams = () => {
  const { currentPage, pageSize } = paginationConfig;
  return {
    pageNo: currentPage,
    pageSize,
    filters: searchFilters.value,
    template: selectedTemplate.value || undefined,
    projectId: props.projectId,
    resource: 'TASK',
    ...sortingConfig.value
  };
};

/**
 * Load analysis list from API with pagination support
 */
const loadAnalysisList = async () => {
  const queryParams = buildQueryParams();

  isLoading.value = true;
  const [error, { data }] = await analysis.getAnalysisList({ ...queryParams });
  isLoading.value = false;

  if (error) {
    return;
  }

  // Handle pagination - either replace or append data
  if (paginationConfig.currentPage === 1) {
    analysisList.value = data.list || [];
    paginationConfig.total = +data.total;
  } else {
    analysisList.value.push(...(data.list || []));
    paginationConfig.total = +data.total;
  }
};

/**
 * Handle search filter changes
 * @param {Array} filterData - New filter data
 */
const handleSearchFiltersChange = (filterData) => {
  paginationConfig.currentPage = 1;
  searchFilters.value = filterData;
  loadAnalysisList();
};

/**
 * Open analysis creation dialog
 * @param {string} templateType - Optional template type to pre-select
 */
const openCreateAnalysisDialog = (templateType = '') => {
  if (templateType) {
    addTabPane({
      _id: 'analysisEdit',
      value: 'analysisEdit',
      name: t('issueAnalysis.actions.addAnalysis'),
      data: { template: templateType }
    });
  } else {
    addTabPane({
      _id: 'analysisEdit',
      value: 'analysisEdit',
      name: t('issueAnalysis.actions.addAnalysis')
    });
  }
};

/**
 * Open analysis editing dialog
 * @param {object} analysisData - Analysis data to edit
 */
const openEditAnalysisDialog = (analysisData) => {
  addTabPane({
    _id: `analysisEdit_${analysisData.id}`,
    value: 'analysisEdit',
    name: analysisData.name,
    data: analysisData
  });
};

/**
 * Open analysis detail view
 * @param {object} analysisData - Analysis data to view
 */
const openAnalysisDetailView = (analysisData) => {
  addTabPane({
    _id: `analysisDetail_${analysisData.id}`,
    value: 'analysisDetail',
    name: analysisData.name,
    data: analysisData
  });
};

/**
 * Delete analysis with confirmation
 * @param {object} analysisData - Analysis data to delete
 */
const deleteAnalysis = (analysisData) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: analysisData.name }),
    onOk () {
      return analysis.deleteAnalysis([analysisData.id])
        .then(() => {
          notification.success(t('actions.tips.deleteSuccess'));
          paginationConfig.currentPage = 1;
          loadAnalysisList();
        });
    }
  });
};

/**
 * Update snapshot data for analysis
 * @param {object} analysisData - Analysis data to update
 */
const updateAnalysisSnapshot = (analysisData) => {
  modal.confirm({
    content: t('issueAnalysis.messages.confirmUpdateSnapshot', { name: analysisData.name }),
    onOk () {
      return analysis.refreshAnalysis(analysisData.id)
        .then(([error]) => {
          if (error) {
            return;
          }
          notification.success(t('actions.tips.updateSuccess'));
        });
    }
  });
};

/**
 * Handle infinite scroll for loading more analysis items
 * @param {Event} event - Scroll event
 */
const handleInfiniteScroll = throttle(500, (event) => {
  // Prevent loading if already at end or currently loading
  if (analysisList.value.length === paginationConfig.total || isLoading.value) {
    return;
  }

  const container = event.currentTarget;
  const clientHeight = container.clientHeight;
  const scrollHeight = container.scrollHeight;
  const scrollTop = container.scrollTop;

  // Load more when near bottom (100px threshold)
  if (clientHeight + scrollTop + 100 > scrollHeight) {
    paginationConfig.currentPage += 1;
    loadAnalysisList();
  }
});

/**
 * Track container height for responsive pagination
 */
let previousContainerHeight = 0;

/**
 * Handle window resize to adjust pagination based on visible area
 */
const handleWindowResize = debounce(300, () => {
  if (!props.onShow) {
    return;
  }

  const currentHeight = dataListContainerRef.value?.clientHeight || 0;
  if (currentHeight <= previousContainerHeight) {
    return;
  }
  previousContainerHeight = currentHeight;

  if (!analysisList.value.length) {
    return;
  }

  // Calculate optimal page size based on container height
  // Each item is approximately 120px tall
  if ((currentHeight / 120) > 4) {
    const visibleRows = Math.floor(currentHeight / 120) + 2;
    paginationConfig.pageSize = visibleRows * 5;
  } else {
    paginationConfig.pageSize = 20;
  }

  // Reload if current data is less than new page size
  if (analysisList.value.length < paginationConfig.pageSize) {
    paginationConfig.currentPage = 1;
    loadAnalysisList();
  }
});

// Lifecycle Hooks
onMounted(() => {
  // Watch for template changes and reload data
  watch(() => selectedTemplate.value, () => {
    paginationConfig.currentPage = 1;
    loadAnalysisList();
  });

  // Watch for refresh notifications and reload data
  watch(() => props.refreshNotify, () => {
    paginationConfig.currentPage = 1;
    loadAnalysisList();
  });

  // Watch for sorting changes and reload data
  watch(() => sortingConfig.value, () => {
    paginationConfig.currentPage = 1;
    loadAnalysisList();
  }, {
    deep: true
  });

  // Add window resize listener
  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  // Clean up window resize listener
  window.removeEventListener('resize', handleWindowResize);
});

</script>
<template>
  <div class="p-5 h-full flex flex-col overflow-x-hidden">
    <Introduce />
    <div class="text-3.5 font-semibold mb-2.5">{{ t('issueAnalysis.addedAnalysis') }}</div>
    <SearchPanel
      v-model:orderBy="sortingConfig.orderBy"
      v-model:orderSort="sortingConfig.orderSort"
      :userInfo="props.userInfo"
      :projectId="props.projectId"
      @change="handleSearchFiltersChange"
      @add="openCreateAnalysisDialog" />

    <div class="flex-1 min-h-50 flex mt-2.5">
      <TemplateSelectList
        v-model:template="selectedTemplate"
        v-model:templateData="availableTemplates"
        v-model:templateDesc="templateDescriptions"
        class="w-55 h-full overflow-y-auto bg-gray-1" />

      <div
        v-show="analysisList.length"
        ref="dataListContainerRef"
        style="height: fit-content"
        class="flex-1 max-h-full pb-2.5 pl-2.5 flex flex-wrap min-w-0 overflow-y-auto"
        @scroll="handleInfiniteScroll">
        <div
          v-for="item in analysisList"
          :key="item.id"
          class="pr-2 flex-1/5 flex-grow-0 mb-2.5 min-w-0">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div class="flex space-x-2 ">
              <Icon :icon="TemplateIconConfig[item.template]" class="text-8 mt-6" />
              <div class="flex-1 min-w-0 mt-1">
                <div class="flex justify-between">
                  <div
                    :title="item.name"
                    class="flex-1 min-w-0 text-3.5 font-semibold mb-1 text-theme-special cursor-pointer truncate"
                    @click="openAnalysisDetailView(item)">
                    {{ item.name }}
                  </div>
                  <Tag class="relative -top-1 mr-0 px-0.5 h-4" color="geekblue">
                    {{
                      item.datasource?.value === AnalysisDataSource.REAL_TIME_DATA ? t('issueAnalysis.status.realTime') : t('issueAnalysis.status.snapshot')
                    }}
                  </Tag>
                </div>

                <div class="project-description-row">
                  <div
                    v-if="item.description"
                    class="project-description-text">
                    <div class="mt-2">{{ item.description }}</div>
                  </div>
                  <p v-else class="project-no-description-text">{{ t('common.noDescription') }}</p>
                </div>
              </div>
            </div>
            <div class="mt-1 text-right">
              <span class="font-semibold mr-1">{{ item.createdByName }}</span>
              {{ t('status.createdAt') }}&nbsp;{{ item.createdDate }}
            </div>
            <div class="flex justify-end">
              <Button
                v-show="item.datasource?.value === AnalysisDataSource.SNAPSHOT_DATA"
                size="small"
                type="text"
                @click="updateAnalysisSnapshot(item)">
                <Icon icon="icon-shuaxin" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="openEditAnalysisDialog(item)">
                <Icon icon="icon-bianji" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="deleteAnalysis(item)">
                <Icon icon="icon-qingchu" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-show="!analysisList.length && !selectedTemplate"
        style="height: fit-content"
        class="flex-1 max-h-full pb-2.5 pl-2.5 flex flex-wrap min-w-0 overflow-y-auto">
        <div
          v-for="item in availableTemplates"
          :key="item.value"
          class="pr-2 flex-1/5 flex-grow-0 mb-2.5">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div>
              <div class="flex space-x-2 ">
                <Icon :icon="TemplateIconConfig[item.value]" class="text-8 mt-6" />
                <div>
                  <div
                    class="text-3.5 font-semibold mb-1 text-theme-special cursor-pointer flex items-center"
                    @click="openCreateAnalysisDialog(item.value)">
                    <Icon icon="icon-jia" />
                    {{ item.message }}
                  </div>
                  <p class="">{{ templateDescriptionMap[item.value] }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-show="!analysisList.length && !!selectedTemplate"
        class="flex-1 h-full p-2.5 min-w-0 text-center flex flex-col items-center space-y-3">
        <Icon :icon="TemplateIconConfig[selectedTemplate]" class="text-20 mt-20" />
        <div>{{ templateDescriptionMap[selectedTemplate] }}</div>
        <div>
          {{ t('issueAnalysis.notCreatedYet') }}
          <Button
            type="link"
            size="small"
            @click="openCreateAnalysisDialog(selectedTemplate)">
            {{ getCurrentTemplateName() }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

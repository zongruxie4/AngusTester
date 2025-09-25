<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, notification } from '@xcan-angus/vue-ui';
import { TemplateIconConfig } from './types';
import { Button, Tag } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import { analysis } from '@/api/tester';
import { BasicProps } from '@/types/types';
import { AnalysisDataSource } from '@/enums/enums';
import { AnalysisInfo } from '@/views/function/analysis/types';
import Introduce from '@/views/function/analysis/list/Introduce.vue';
import SearchPanel from '@/views/function/analysis/list/SearchPanel.vue';

// Async components
const TemplateSelectList = defineAsyncComponent(() => import('@/views/function/analysis/list/TemplateSelect.vue'));

// Component setup
const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: () => ({ id: '' }),
  refreshNotify: 0,
  onShow: false
});

// Injected dependencies
const addTabPane = inject('addTabPane', (value) => value);

// Pagination state
const paginationConfig = {
  pageNo: 1,
  pageSize: 20,
  total: 0
};

// Reactive state
const dataListContainerRef = ref();
const templateDescriptions = ref<{ value: string; message: string }[]>([]);
const templateOptions = ref<{ value: string; message: string }[]>([]);
const searchFilters = ref<{ op: string; key: string; value: any }[]>([]);
const sortConfig = ref({ orderBy: undefined, orderSort: undefined });
const selectedTemplate = ref('');
const analysisList = ref<AnalysisInfo[]>([]);
const isLoading = ref(false);

/**
 * Get template name by selected template value.
 * @returns Template name string
 */
const getSelectedTemplateName = () => {
  return templateOptions.value.find(item => item.value === selectedTemplate.value)?.message;
};

/**
 * Convert template descriptions to configuration object.
 */
const templateDescriptionConfig = computed(() => {
  const config = {} as any;
  templateDescriptions.value.forEach(item => {
    config[item.value] = item.message;
  });
  return config;
});

/**
 * Build API request parameters for analysis list.
 * @returns Formatted parameters object
 */
const buildRequestParameters = () => {
  const { pageNo, pageSize } = paginationConfig;
  return {
    pageNo,
    pageSize,
    filters: searchFilters.value,
    template: selectedTemplate.value || undefined,
    projectId: props.projectId,
    resource: 'CASE',
    ...sortConfig.value
  };
};

/**
 * Handle search form submission.
 * @param searchData - Search form data
 */
const handleSearchSubmit = (searchData) => {
  paginationConfig.pageNo = 1;
  searchFilters.value = searchData;
  loadAnalysisList();
};

/**
 * Navigate to add analysis tab.
 * @param templateValue - Optional template value for pre-selection
 */
const navigateToAddAnalysis = (templateValue = '') => {
  if (templateValue) {
    addTabPane({
      _id: 'analysisEdit',
      value: 'analysisEdit',
      name: t('functionAnalysis.list.addAnalysis'),
      data: { template: templateValue }
    });
  } else {
    addTabPane({
      _id: 'analysisEdit',
      value: 'analysisEdit',
      name: t('functionAnalysis.list.addAnalysis')
    });
  }
};

/**
 * Navigate to edit analysis tab.
 * @param analysisData - Analysis data to edit
 */
const navigateToEditAnalysis = (analysisData) => {
  addTabPane({
    _id: `analysisEdit_${analysisData.id}`,
    value: 'analysisEdit',
    name: analysisData.name,
    data: analysisData
  });
};

/**
 * Navigate to analysis detail tab.
 * @param analysisData - Analysis data to view
 */
const navigateToAnalysisDetail = (analysisData) => {
  addTabPane({
    _id: `analysisDetail_${analysisData.id}`,
    value: 'analysisDetail',
    name: analysisData.name,
    data: analysisData
  });
};

/**
 * Load analysis list from API with pagination support.
 */
const loadAnalysisList = async () => {
  const parameters = buildRequestParameters();

  isLoading.value = true;
  const [error, { data }] = await analysis.getAnalysisList({
    ...parameters
  });
  isLoading.value = false;
  if (error) {
    return;
  }

  // Handle pagination data
  if (paginationConfig.pageNo === 1) {
    analysisList.value = data.list || [];
    paginationConfig.total = +data.total;
  } else {
    analysisList.value.push(...(data.list || []));
    paginationConfig.total = +data.total;
  }
};

/**
 * Delete analysis with confirmation modal.
 * @param analysisData - Analysis data to delete
 */
const handleDeleteAnalysis = (analysisData) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: analysisData.name }),
    onOk () {
      return analysis.deleteAnalysis([analysisData.id])
        .then(() => {
          notification.success(t('actions.tips.deleteSuccess'));
          paginationConfig.pageNo = 1;
          loadAnalysisList();
        });
    }
  });
};

/**
 * Update analysis snapshot with confirmation modal.
 * @param analysisData - Analysis data to update
 */
const handleUpdateSnapshot = (analysisData) => {
  modal.confirm({
    content: t('functionAnalysis.list.updateSnapshotConfirm', { name: analysisData.name }),
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
 * Handle infinite scroll for analysis list.
 * @param event - Scroll event
 */
const handleInfiniteScroll = throttle(300, (event) => {
  if (analysisList.value.length === paginationConfig.total || isLoading.value) {
    return;
  }

  const { clientHeight, scrollHeight, scrollTop } = event.currentTarget;
  const scrollThreshold = 100;

  if (clientHeight + scrollTop + scrollThreshold > scrollHeight) {
    paginationConfig.pageNo += 1;
    loadAnalysisList();
  }
});

// Window resize handling
let previousWrapperHeight = 0;

/**
 * Handle window resize to adjust pagination based on container height.
 */
const handleWindowResize = debounce(500, () => {
  if (!props.onShow) {
    return;
  }

  const currentHeight = dataListContainerRef.value?.clientHeight || 0;
  if (currentHeight <= previousWrapperHeight) {
    return;
  }

  previousWrapperHeight = currentHeight;
  if (!analysisList.value.length) {
    return;
  }

  // Calculate optimal page size based on container height
  const itemHeight = 120;
  const minRows = 4;

  if ((currentHeight / itemHeight) > minRows) {
    const visibleRows = Math.floor(currentHeight / itemHeight) + 2;
    paginationConfig.pageSize = visibleRows * 5;
  } else {
    paginationConfig.pageSize = 20;
  }

  // Reload if current data is less than new page size
  if (analysisList.value.length < paginationConfig.pageSize) {
    paginationConfig.pageNo = 1;
    loadAnalysisList();
  }
});

// Lifecycle hooks
onMounted(() => {
  // Watch template changes
  watch(() => selectedTemplate.value, () => {
    paginationConfig.pageNo = 1;
    loadAnalysisList();
  });

  // Watch refresh notification
  watch(() => props.refreshNotify, () => {
    paginationConfig.pageNo = 1;
    loadAnalysisList();
  });

  // Watch sort configuration changes
  watch(() => sortConfig.value, () => {
    paginationConfig.pageNo = 1;
    loadAnalysisList();
  }, {
    deep: true
  });

  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
});

</script>
<template>
  <div class="p-5 h-full flex flex-col overflow-x-hidden">
    <Introduce />
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('functionAnalysis.list.title') }}
    </div>

    <SearchPanel
      v-model:orderBy="sortConfig.orderBy"
      v-model:orderSort="sortConfig.orderSort"
      :userInfo="props.userInfo"
      :projectId="props.projectId"
      @change="handleSearchSubmit"
      @add="navigateToAddAnalysis" />

    <div class="flex-1 min-h-50 flex mt-2.5">
      <TemplateSelectList
        v-model:template="selectedTemplate"
        v-model:templateData="templateOptions"
        v-model:templateDesc="templateDescriptions"
        class="w-50 h-full overflow-y-auto bg-gray-1" />

      <div
        v-show="analysisList.length"
        ref="dataListContainerRef"
        style="height: fit-content"
        class="flex-1 max-h-full pb-2.5 pl-2.5 flex flex-wrap min-w-0 overflow-y-auto"
        @scroll="handleInfiniteScroll">
        <div
          v-for="item in analysisList"
          :key="item.id"
          class="pr-2 flex-1/5 min-w-0 flex-grow-0 mb-2.5">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div>
              <div class="flex space-x-2 ">
                <Icon :icon="TemplateIconConfig[item.template]" class="text-8 mt-6" />
                <div class="flex-1 min-w-0">
                  <div class="flex justify-between">
                    <div
                      :title="item.name"
                      class="flex-1 min-w-0 text-3.5 font-semibold mb-1 text-theme-special cursor-pointer truncate"
                      @click="navigateToAnalysisDetail(item)">
                      {{ item.name }}
                    </div>

                    <Tag class="relative -top-1 mr-0 px-0.5 h-5" color="geekblue">
                      {{ item.datasource?.value === AnalysisDataSource.REAL_TIME_DATA ? t('functionAnalysis.list.realTime') : t('functionAnalysis.list.snapshot') }}
                    </Tag>
                  </div>

                  <p class="">{{ templateDescriptionConfig[item.template] }}</p>
                </div>
              </div>

              <div class="mt-1  text-right">
                <div>
                  <span class="font-semibold mr-1">{{ item.createdByName }}</span>
                  {{ t('functionAnalysis.list.createdBy') }}{{ item.createdDate }}
                </div>
              </div>
            </div>
            <div class="flex justify-end">
              <Button
                v-show="item.datasource?.value === AnalysisDataSource.SNAPSHOT_DATA"
                size="small"
                type="text"
                @click="handleUpdateSnapshot(item)">
                <Icon icon="icon-shuaxin" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="navigateToEditAnalysis(item)">
                <Icon icon="icon-bianji" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="handleDeleteAnalysis(item)">
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
          v-for="item in templateOptions"
          :key="item.value"
          class="pr-2 flex-1/5 flex-grow-0 mb-2.5">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div>
              <div class="flex space-x-2 ">
                <Icon :icon="TemplateIconConfig[item.value]" class="text-8 mt-6" />
                <div>
                  <div
                    class="text-3.5 font-semibold mb-1 text-theme-special cursor-pointer flex items-center"
                    @click="navigateToAddAnalysis(item.value)">
                    <Icon icon="icon-jia" />
                    {{ item.description }}
                  </div>
                  <p class="">{{ templateDescriptionConfig[item.value] }}</p>
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
        <div>{{ templateDescriptionConfig[selectedTemplate] }}</div>
        <div>
          {{ t('functionAnalysis.list.notCreated') }}
          <Button
            type="link"
            size="small"
            @click="navigateToAddAnalysis(selectedTemplate)">
            {{ getSelectedTemplateName() }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

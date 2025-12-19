<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, AsyncComponent, Colon, Icon, Spin, Tooltip } from '@xcan-angus/vue-ui';
import { Button, Collapse, CollapsePanel, Popconfirm, Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { EnumMessage, ActionOnEOF, SharingMode, enumUtils, TESTER, http } from '@xcan-angus/infra';
import { paramTarget } from '@/api/tester';
import { DataSetItem } from './PropsType';
import { DataMenuKey } from '@/views/data/menu';

const { t } = useI18n();

/**
 * Component props interface for dataset parameterization
 */
type Props = {
  projectId: string;
  targetId: string;
  targetType: 'API' | 'SCENARIO' | 'API_CASE';
  datasetActionOnEOF: ActionOnEOF;
  datasetSharingMode: SharingMode;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  targetId: undefined,
  targetType: undefined,
  datasetActionOnEOF: ActionOnEOF.RECYCLE,
  datasetSharingMode: SharingMode.ALL_THREAD
});

// Component events
const emit = defineEmits<{
  (e: 'targetInfoChange', value: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }): void;
}>();

// Async component imports
const DataSetModal = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/ListModal.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/UseList.vue'));
const PreviewData = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/PreviewData.vue'));
const StaticParameters = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/StaticParameter.vue'));
const ExtractParameters = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/ExtractParameter.vue'));

// Component state
const isDataLoaded = ref(false);
const isLoading = ref(false);
const datasetTableData = ref<DataSetItem[]>([]);
const isModalVisible = ref(false);

// Collapse state
const collapseActiveKeys = ref<string[]>([]);

// Action on EOF configuration
const actionOnEOF = ref<ActionOnEOF>(ActionOnEOF.RECYCLE);
const eofEnums = ref<EnumMessage<ActionOnEOF>[]>([]);

// Sharing mode configuration
const sharingMode = ref<SharingMode>(SharingMode.ALL_THREAD);
const sharingModeEnums = ref<EnumMessage<SharingMode>[]>([]);

/**
 * Handle collapse arrow change
 */
const handleCollapseArrowChange = (open: boolean, id: string) => {
  if (!open) {
    collapseActiveKeys.value = collapseActiveKeys.value.filter(item => item !== id);
    return;
  }

  collapseActiveKeys.value.push(id);
};

/**
 * Handle open dataset modal
 */
const handleOpenDatasetModal = () => {
  isModalVisible.value = true;
};

/**
 * Handle dataset action on EOF change
 */
const handleDatasetActionOnEOFChange = async (event: any) => {
  const value = event.target.value;
  const params = {
    id: props.targetId,
    datasetActionOnEOF: value,
    datasetSharingMode: sharingMode.value
  };
  const [error] = await updateTargetInfo(params);
  if (error) {
    return;
  }

  actionOnEOF.value = value;
};

/**
 * Handle dataset sharing mode change
 */
const handleDatasetSharingModeChange = async (event: any) => {
  const value = event.target.value;
  const params = {
    id: props.targetId,
    datasetActionOnEOF: actionOnEOF.value,
    datasetSharingMode: value
  };
  const [error] = await updateTargetInfo(params);
  if (error) {
    return;
  }

  sharingMode.value = value;
};

/**
 * Update target information via API
 */
const updateTargetInfo = async (params: { id: string; datasetActionOnEOF: ActionOnEOF; datasetSharingMode: SharingMode; }) => {
  let url = '';
  if (props.targetType === 'API') {
    url = `${TESTER}/apis`;
  } else if (props.targetType === 'API_CASE') {
    url = `${TESTER}/apis/case`;
  }

  isLoading.value = true;
  const [error, response] = await http.patch(url, [params]);
  isLoading.value = false;
  if (error) {
    return [error];
  }

  // Sync external information
  emit('targetInfoChange', params);

  return [error, response];
};

/**
 * Handle selected datasets confirmation
 */
const handleSelectedDatasetsConfirmation = async (data: DataSetItem[]) => {
  if (!data?.length) {
    return;
  }

  const datasetIds = data.map((item) => item.id);
  isLoading.value = true;
  const [error] = await paramTarget.addDataSet(props.targetId, props.targetType, datasetIds);
  isLoading.value = false;
  if (error) {
    return;
  }

  datasetTableData.value.unshift(...data);
};

/**
 * Handle dataset deletion
 */
const handleDatasetDeletion = async (data: DataSetItem) => {
  const datasetId = data.id;
  isLoading.value = true;
  const [error] = await paramTarget.deleteDataSet(`${TESTER}/target/${props.targetId}/${props.targetType}/dataset`, datasetId, { dataType: true } as any);
  isLoading.value = false;
  if (error) {
    return;
  }

  datasetTableData.value = datasetTableData.value.filter((item) => item.id !== datasetId);
};

/**
 * Load dataset data from API
 */
const loadDatasetData = async () => {
  isLoading.value = true;
  const [error, response] = await paramTarget.getDataSet(props.targetId, props.targetType);
  isLoading.value = false;
  isDataLoaded.value = true;
  if (error) {
    return;
  }

  datasetTableData.value = response?.data || [];
};

/**
 * Load action on EOF enums
 */
const loadActionOnEOFEnums = () => {
  eofEnums.value = enumUtils.enumToMessages(ActionOnEOF);
};

/**
 * Load sharing mode enums
 */
const loadSharingModeEnums = () => {
  sharingModeEnums.value = enumUtils.enumToMessages(SharingMode);
};

/**
 * Reset component state
 */
const resetComponentState = () => {
  isDataLoaded.value = false;
  isLoading.value = false;
  datasetTableData.value = [];
  isModalVisible.value = false;
  collapseActiveKeys.value = [];
};

// Component initialization and watchers
onMounted(() => {
  loadActionOnEOFEnums();
  loadSharingModeEnums();

  watch(() => props.datasetActionOnEOF, (newValue) => {
    actionOnEOF.value = newValue;
  }, { immediate: true });

  watch(() => props.datasetSharingMode, (newValue) => {
    sharingMode.value = newValue;
  }, { immediate: true });

  watch(() => props.targetId, (newValue) => {
    resetComponentState();

    if (!newValue) {
      return;
    }

    loadDatasetData();
  }, { immediate: true });
});

// Computed properties
const selectedDatasetNames = computed(() => {
  return datasetTableData.value?.map(item => item.name);
});

// Hint text mapping
const hintTextMap = {
  FILE: t('commonComp.apis.parameterizationDataset.fileHint'),
  JDBC: t('commonComp.apis.parameterizationDataset.jdbcHint')
};
</script>

<template>
  <Spin :spinning="isLoading" class="text-3 leading-5">
    <div class="flex items-center flex-nowrap mb-2.5">
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>
      <div class="flex-shrink-0 text-theme-title mr-2.5">{{ t('commonComp.apis.parameterizationDataset.title') }}</div>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">
        {{ t('common.description') }}
      </div>
    </div>

    <div class="flex items-center space-x-15 mb-2">
      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('commonComp.apis.parameterizationDataset.readToEnd') }}</span>
          <Colon />
        </div>
        <RadioGroup
          :value="actionOnEOF"
          name="action"
          @change="handleDatasetActionOnEOFChange">
          <Radio
            v-for="item in eofEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === 'RECYCLE'" :title="t('commonComp.apis.parameterizationDataset.recycleTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === 'STOP_THREAD'" :title="t('commonComp.apis.parameterizationDataset.stopThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>

      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('commonComp.apis.parameterizationDataset.sharingMode') }}</span>
          <Colon />
        </div>
        <RadioGroup
          :value="sharingMode"
          name="share"
          @change="handleDatasetSharingModeChange">
          <Radio
            v-for="item in sharingModeEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === SharingMode.ALL_THREAD" :title="t('commonComp.apis.parameterizationDataset.allThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === SharingMode.CURRENT_THREAD" :title="t('commonComp.apis.parameterizationDataset.currentThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>
    </div>

    <div class="mb-2">
      <Button
        type="link"
        size="small"
        class="flex items-center h-5 leading-5 p-0 space-x-1"
        @click="handleOpenDatasetModal">
        <Icon icon="icon-jia" class="text-3.5" />
        <span>{{ t('commonComp.apis.parameterizationDataset.addDataset') }}</span>
      </Button>
    </div>

    <template v-if="isDataLoaded">
      <div v-if="datasetTableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
        <img style="width:100px;" src="../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3">
          <span>{{ t('commonComp.apis.parameterizationDataset.noDataMessage') }}</span>
        </div>
      </div>

      <div v-else>
        <div class="flex items-center table-thead-tr">
          <div class="table-thead-th">{{ t('commonComp.apis.parameterizationDataset.name') }}</div>
          <div class="table-thead-th">{{ t('common.creator') }}</div>
          <div class="table-thead-th">{{ t('common.actions') }}</div>
        </div>

        <Collapse v-model:activeKey="collapseActiveKeys" collapsible="disabled">
          <CollapsePanel
            v-for="item in datasetTableData"
            :key="item.id"
            :showArrow="false">
            <template #header>
              <div class="flex items-center table-tbody-tr">
                <div class="table-tbody-td">
                  <Arrow
                    :open="collapseActiveKeys.includes(item.id)"
                    type="dashed"
                    style="font-size:12px;"
                    class="mr-1"
                    @change="handleCollapseArrowChange($event, item.id)" />
                  <div class="flex-1 truncate">
                    <span
                      :title="item.name"
                      class="truncate cursor-pointer"
                      @click="handleCollapseArrowChange(!collapseActiveKeys.includes(item.id), item.id)">{{ item.name }}</span>
                  </div>
                </div>
                <div class="table-tbody-td" :title="item.creator">
                  <div class="flex-1 truncate">{{ item.creator }}</div>
                </div>
                <div class="table-tbody-td flex items-center space-x-2.5">
                  <Popconfirm :title="t('commonComp.apis.parameterizationDataset.cancelReferenceConfirm', { name: item.name })" @confirm="handleDatasetDeletion(item)">
                    <Button
                      :title="t('commonComp.apis.parameterizationDataset.cancelReference')"
                      type="text"
                      size="small"
                      class="flex items-center p-0 h-5 leading-5 space-x-1">
                      <Icon icon="icon-qingchu" class="text-3.5" />
                      <span>{{ t('commonComp.apis.parameterizationDataset.cancelReference') }}</span>
                    </Button>
                  </Popconfirm>

                  <Button
                    :title="t('commonComp.apis.parameterizationDataset.viewDefinition')"
                    type="text"
                    size="small"
                    class="p-0 h-5 leading-5">
                    <RouterLink
                      class="flex items-center space-x-1"
                      :to="`/data#${DataMenuKey.DATASET}?id=${item.id}`"
                      target="_blank">
                      <Icon icon="icon-zhengyan" class="text-3.5" />
                      <span>{{ t('commonComp.apis.parameterizationDataset.viewDefinition') }}</span>
                    </RouterLink>
                  </Button>
                </div>
              </div>
            </template>

            <Tabs size="small" class="ant-tabs-nav-mb-2.5 normal-tabs">
              <template v-if="!item.extraction">
                <TabPane key="value" :tab="t('commonComp.apis.parameterizationDataset.parameters')">
                  <StaticParameters :dataSource="item.parameters" class="mb-5" />
                </TabPane>
              </template>

              <template v-else>
                <TabPane key="value" :tab="t('common.extract')">
                  <ExtractParameters
                    :dataSource="item.parameters"
                    :columnIndex="+item.extraction.columnIndex"
                    :hintText="hintTextMap[item.extraction.source]"
                    class="mb-5" />
                </TabPane>
              </template>

              <TabPane key="preview" :tab="t('actions.preview')">
                <PreviewData :dataSource="item as any" />
              </TabPane>

              <TabPane key="use" :tab="t('actions.use')">
                <DataSetUseList :id="item.id" />
              </TabPane>
            </Tabs>
          </CollapsePanel>
        </Collapse>
      </div>
    </template>

    <AsyncComponent :visible="isModalVisible">
      <DataSetModal
        v-model:visible="isModalVisible"
        :projectId="props.projectId"
        :selectedNames="selectedDatasetNames"
        @ok="handleSelectedDatasetsConfirmation" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
.table-thead-tr {
  width: 100%;
  border-bottom: 1px solid var(--border-divider);
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
  background-color: var(--table-header-bg);
}

.table-thead-tr .table-thead-th {
  position: relative;
  height: 37px;
  padding: 2px 8px;
  overflow: hidden;
  line-height: 37px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-thead-tr .table-thead-th:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 50%;
  right: 0;
  width: 1px;
  height: 1.6em;
  transform: translateY(-50%);
  transition: background-color .3s;
  background-color: var(--border-divider);
}

.table-tbody-tr {
  width: 100%;
}

.table-tbody-tr:hover {
  background: var(--content-tabs-bg-hover);
}

.table-tbody-tr .table-tbody-td {
  display: flex;
  position: relative;
  flex-wrap: nowrap;
  align-items: center;
  height: 37px;
  padding: 2px 8px;
  overflow: hidden;
  line-height: 18px;
}

.table-thead-tr .table-thead-th:nth-child(1),
.table-tbody-tr .table-tbody-td:nth-child(1) {
  flex: 1 1 calc(85% - 180px);
}

.table-thead-tr .table-thead-th:nth-child(2),
.table-tbody-tr .table-tbody-td:nth-child(2) {
  width: 15%;
}

.table-thead-tr .table-thead-th:nth-child(3),
.table-tbody-tr .table-tbody-td:nth-child(3) {
  width: 180px;
}

.ant-collapse {
  border: none;
  background-color: #fff;
  font-size: 12px;
  line-height: 18px;
}

.ant-collapse :deep(.ant-collapse-header) {
  display: flex;
  align-items: center;
  padding: 0;
  line-height: 18px;
}

.ant-collapse>:deep(.ant-collapse-item).ant-collapse-no-arrow>.ant-collapse-header {
  padding-left: 0;
}

.ant-collapse :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 24px;
}

.ant-tabs.ant-tabs-top.ant-tabs-small.normal-tabs>:deep(.ant-tabs-nav) {
  margin: 0 0 10px;
}
</style>

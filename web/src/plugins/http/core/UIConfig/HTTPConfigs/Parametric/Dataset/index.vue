<script setup lang="ts">
import { ref, onMounted, defineAsyncComponent, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, Colon, Tooltip, Arrow } from '@xcan-angus/vue-ui';
import { Button, RadioGroup, Radio, Collapse, CollapsePanel, TabPane, Tabs, Popconfirm } from 'ant-design-vue';
import { EnumMessage, ActionOnEOF, SharingMode, enumUtils } from '@xcan-angus/infra';

import { DatasetItem } from './PropsType';
import { HTTPConfig } from '../../PropsType';

const { t } = useI18n();

type Props = {
  projectId: string;
  datasets: HTTPConfig['datasets'];
  actionOnEOF: ActionOnEOF;
  sharingMode: SharingMode;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  datasets: () => [],
  actionOnEOF: ActionOnEOF.RECYCLE,
  sharingMode: SharingMode.ALL_THREAD
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: HTTPConfig['datasets']): void;
  (e: 'targetInfoChange', value: { actionOnEOF?: ActionOnEOF; sharingMode?: SharingMode; }): void;
}>();

const DatasetModal = defineAsyncComponent(() => import('./DatasetModal/index.vue'));
const DatasetUseList = defineAsyncComponent(() => import('./DatasetUseList/index.vue'));
const PreviewData = defineAsyncComponent(() => import('./PreviewData/index.vue'));
const StaticParameters = defineAsyncComponent(() => import('./StaticParameters/index.vue'));
const ExtractParameters = defineAsyncComponent(() => import('./ExtractParameters/index.vue'));

const tableData = ref<DatasetItem[]>([]);
const modalVisible = ref(false);

const collapseActiveKeys = ref<string[]>([]);

const eofEnums = ref<EnumMessage<ActionOnEOF>[]>([]);
const sharingModeEnums = ref<EnumMessage<SharingMode>[]>([]);

const arrowChange = (open: boolean, id: string) => {
  if (!open) {
    collapseActiveKeys.value = collapseActiveKeys.value.filter(item => item !== id);
    return;
  }

  collapseActiveKeys.value.push(id);
};

const toUse = () => {
  modalVisible.value = true;
};

const datasetActionOnEOFChange = async (event: { target: { value: ActionOnEOF } }) => {
  const value = event.target.value;
  const params = { actionOnEOF: value };
  emit('targetInfoChange', params);
};

const datasetSharingModeChange = async (event: { target: { value: SharingMode } }) => {
  const value = event.target.value;
  const params = { sharingMode: value };
  emit('targetInfoChange', params);
};

const selectedVariablesOk = async (data: DatasetItem[]) => {
  if (!data?.length) {
    return;
  }

  tableData.value.unshift(...data);
  emit('change', tableData.value);
};

const toDelete = async (data: DatasetItem) => {
  const id = data['x-id'];
  const index = tableData.value.findIndex((item) => item['x-id'] === id);
  tableData.value.splice(index, 1);
  emit('change', tableData.value);
};

const loadActionOnEOFEnums = () => {
  eofEnums.value = enumUtils.enumToMessages(ActionOnEOF);
};

const loadSharingModeEnums = () => {
  sharingModeEnums.value = enumUtils.enumToMessages(SharingMode);
};

const reset = () => {
  tableData.value = [];
  modalVisible.value = false;
  collapseActiveKeys.value = [];
};

onMounted(() => {
  loadActionOnEOFEnums();
  loadSharingModeEnums();

  watch(() => props.datasets, (newValue) => {
    reset();
    if (!newValue?.length) {
      return;
    }
    tableData.value = newValue;
  }, { immediate: true });
});

const selectedNames = computed(() => {
  return tableData.value?.map(item => item.name) || [];
});

const hintTextMap = {
  FILE: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.hintTextMap.file'),
  JDBC: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.hintTextMap.jdbc')
};
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex items-center flex-nowrap mb-2.5">
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>
      <div class="flex-shrink-0 text-theme-title mr-2.5">{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.title') }}</div>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">
        {{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.description') }}
      </div>
    </div>

    <div class="flex items-center space-x-15 mb-2">
      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.actionOnEOF') }}</span>
          <Colon />
        </div>
        <RadioGroup
          :value="props.actionOnEOF"
          name="action"
          @change="datasetActionOnEOFChange">
          <Radio
            v-for="item in eofEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === 'RECYCLE'" :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.recycleTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === 'STOP_THREAD'" :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.stopThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>

      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.sharingMode') }}</span>
          <Colon />
        </div>
        <RadioGroup
          :value="props.sharingMode"
          name="share"
          @change="datasetSharingModeChange">
          <Radio
            v-for="item in sharingModeEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === 'ALL_THREAD'" :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.allThreadTooltip')">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === 'CURRENT_THREAD'" :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.currentThreadTooltip')">
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
        @click="toUse">
        <Icon icon="icon-jia" class="text-3.5" />
        <span>{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.addDataset') }}</span>
      </Button>
    </div>

    <div v-if="tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
      <img style="width:100px;" src="./images/nodata.png">
      <div class="flex items-center text-theme-sub-content text-3">
        <span>{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.noDatasetsDefined') }}</span>
      </div>
    </div>

    <div v-else>
      <div class="flex items-center table-thead-tr">
        <div class="table-thead-th">{{ t('common.name') }}</div>
        <div class="table-thead-th">{{ t('common.creator') }}</div>
        <div class="table-thead-th">{{ t('common.actions') }}</div>
      </div>

      <Collapse v-model:activeKey="collapseActiveKeys" collapsible="disabled">
        <CollapsePanel
          v-for="item in tableData"
          :key="item['x-id']"
          :showArrow="false">
          <template #header>
            <div class="flex items-center table-tbody-tr">
              <div class="table-tbody-td">
                <Arrow
                  :open="collapseActiveKeys.includes(item['x-id'])"
                  type="dashed"
                  style="font-size:12px;"
                  class="mr-1"
                  @change="arrowChange($event, item['x-id'])" />
                <div class="flex-1 truncate">
                  <span
                    :title="item.name"
                    class="truncate cursor-pointer"
                    @click="arrowChange(!collapseActiveKeys.includes(item['x-id']), item['x-id'])">{{ item.name }}</span>
                </div>
              </div>
              <div class="table-tbody-td" :title="item['x-createdByName']">
                <div class="flex-1 truncate">{{ item['x-createdByName'] }}</div>
              </div>
              <div class="table-tbody-td flex items-center space-x-2.5">
                <Popconfirm :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.confirmUnlinkDataset', { name: item.name })" @confirm="toDelete(item)">
                  <Button
                    :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.unlinkDataset')"
                    type="text"
                    size="small"
                    class="flex items-center p-0 h-5 leading-5 space-x-1">
                    <Icon icon="icon-qingchu" class="text-3.5" />
                  </Button>
                </Popconfirm>

                <Button
                  :title="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.viewDefinition')"
                  type="text"
                  size="small"
                  class="p-0 h-5 leading-5">
                  <RouterLink
                    class="flex items-center space-x-1"
                    :to="`/data#dataSet?id=${item['x-id']}`"
                    target="_blank">
                    <Icon icon="icon-lianjie1" class="text-3.5" />
                  </RouterLink>
                </Button>
              </div>
            </div>
          </template>

          <Tabs size="small" class="ant-tabs-nav-mb-2.5 normal-tabs">
            <template v-if="!item.extraction">
              <TabPane key="value" :tab="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.parameters')">
                <StaticParameters :dataSource="item.parameters" class="mb-5" />
              </TabPane>
            </template>

            <template v-else>
              <TabPane key="value" :tab="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.extractParameters')">
                <ExtractParameters
                  :dataSource="item.parameters"
                  :columnIndex="+item.extraction.columnIndex"
                  :hintText="hintTextMap[item.extraction.source]"
                  class="mb-5" />
              </TabPane>
            </template>

            <TabPane key="preview" :tab="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.previewData')">
              <PreviewData :dataSource="item" />
            </TabPane>

            <TabPane key="use" :tab="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseList')">
              <DatasetUseList :id="item['x-id']" />
            </TabPane>
          </Tabs>
        </CollapsePanel>
      </Collapse>
    </div>

    <AsyncComponent :visible="modalVisible">
      <DatasetModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        :selectedNames="selectedNames"
        @ok="selectedVariablesOk" />
    </AsyncComponent>
  </div>
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
  width: 80px;
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

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Arrow, AsyncComponent, Colon, Icon, Spin, Tooltip } from '@xcan-angus/vue-ui';
import { Button, Collapse, CollapsePanel, Popconfirm, Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { enumLoader, TESTER, http } from '@xcan-angus/tools';

import { DataSetItem } from './PropsType';

type Props = {
  projectId: string;
  targetId: string;
  targetType: 'API' | 'SCENARIO' | 'API_CASE';
  datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD';
  datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD';
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  targetId: undefined,
  targetType: undefined,
  datasetActionOnEOF: 'RECYCLE',
  datasetSharingMode: 'ALL_THREAD'
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'targetInfoChange', value: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }): void;
}>();

const DataSetModal = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/listModal/index.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/useList/index.vue'));
const PreviewData = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/previewData/index.vue'));
const StaticParameters = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/staticParameter/index.vue'));
const ExtractParameters = defineAsyncComponent(() => import('@/components/apis/parameterization/dataset/extractParameter/index.vue'));

const loaded = ref(false);
const loading = ref(false);
const tableData = ref<DataSetItem[]>([]);
const modalVisible = ref(false);

const collapseActiveKeys = ref<string[]>([]);

const actionOnEOF = ref<'RECYCLE' | 'STOP_THREAD'>('RECYCLE');
const eofEnums = ref<{ message: string; value: 'RECYCLE' | 'STOP_THREAD'; }[]>([]);

const sharingMode = ref<'ALL_THREAD' | 'CURRENT_THREAD'>('ALL_THREAD');
const sharingModeEnums = ref<{ message: string; value: 'ALL_THREAD' | 'CURRENT_THREAD'; }[]>([]);

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

const datasetActionOnEOFChange = async (event: { target: { value: 'RECYCLE' | 'STOP_THREAD' } }) => {
  const value = event.target.value;
  const params = {
    id: props.targetId,
    datasetActionOnEOF: value,
    datasetSharingMode: sharingMode.value
  };
  const [error] = await patchTargetInfo(params);
  if (error) {
    return;
  }

  actionOnEOF.value = value;
};

const datasetSharingModeChange = async (event: { target: { value: 'ALL_THREAD' | 'CURRENT_THREAD' } }) => {
  const value = event.target.value;
  const params = {
    id: props.targetId,
    datasetActionOnEOF: actionOnEOF.value,
    datasetSharingMode: value
  };
  const [error] = await patchTargetInfo(params);
  if (error) {
    return;
  }

  sharingMode.value = value;
};

const patchTargetInfo = async (params: { id: string; datasetActionOnEOF: 'RECYCLE' | 'STOP_THREAD'; datasetSharingMode: 'ALL_THREAD' | 'CURRENT_THREAD'; }) => {
  let url = '';
  if (props.targetType === 'API') {
    url = `${TESTER}/apis`;
  } else if (props.targetType === 'API_CASE') {
    url = `${TESTER}/apis/case`;
  }

  loading.value = true;
  const [error, res] = await http.patch(url, [params]);
  loading.value = false;
  if (error) {
    return [error];
  }

  // 同步外部信息
  emit('targetInfoChange', params);

  return [error, res];
};

const selectedVariablesOk = async (data: DataSetItem[]) => {
  if (!data?.length) {
    return;
  }

  const ids = data.map((item) => item.id);
  loading.value = true;
  const [error] = await http.post(`${TESTER}/target/${props.targetId}/${props.targetType}/dataset`, ids);
  loading.value = false;
  if (error) {
    return;
  }

  tableData.value.unshift(...data);
};

const toDelete = async (data: DataSetItem) => {
  const id = data.id;
  loading.value = true;
  const [error] = await http.del(`${TESTER}/target/${props.targetId}/${props.targetType}/dataset`, [id], { dataType: true });
  loading.value = false;
  if (error) {
    return;
  }

  tableData.value = tableData.value.filter((item) => item.id !== id);
};

const loadData = async () => {
  loading.value = true;
  const [error, res] = await http.get(`${TESTER}/target/${props.targetId}/${props.targetType}/dataset`);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  tableData.value = res?.data || [];
};

const loadActionOnEOFEnums = async () => {
  const [error, res] = await enumLoader.load('ActionOnEOF');
  if (error) {
    return;
  }

  eofEnums.value = (res || []) as { message: string; value: 'RECYCLE' | 'STOP_THREAD'; }[];
};

const loadSharingModeEnums = async () => {
  const [error, res] = await enumLoader.load('SharingMode');
  if (error) {
    return;
  }

  sharingModeEnums.value = (res || []) as { message: string; value: 'ALL_THREAD' | 'CURRENT_THREAD'; }[];
};

const reset = () => {
  loaded.value = false;
  loading.value = false;
  tableData.value = [];
  modalVisible.value = false;
  collapseActiveKeys.value = [];
};

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
    reset();

    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const selectedNames = computed(() => {
  return tableData.value?.map(item => item.name);
});

const hintTextMap = {
  FILE: '从文件中读取数据集，每个数据集最大允许创建200个参数，每个参数文件最大不超过500MB，总行数不超过100万行。',
  JDBC: '每次执行测试前从数据库中查询结果中提取一个值作为数据集值。'
};
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center flex-nowrap mb-2.5">
      <div class="flex-shrink-0 w-1 h-3.5 rounded bg-blue-400 mr-1.5"></div>
      <div class="flex-shrink-0 text-theme-title mr-2.5">数据集</div>
      <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 mr-1" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap">
        引入已定义数据集，引入后可以在当前请求中使用数据集参数。注意：只有引用后数据集参数才会生效。当数据集参数名和变量名冲突时，同名数据集参数值比变量值优先级高。
      </div>
    </div>

    <div class="flex items-center space-x-15 mb-2">
      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>读到末尾时</span>
          <Colon />
        </div>
        <RadioGroup
          :value="actionOnEOF"
          name="action"
          @change="datasetActionOnEOFChange">
          <Radio
            v-for="item in eofEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === 'RECYCLE'" title="数据集中所有数据行都被使用后，‌将重新从开头使用数据。">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === 'STOP_THREAD'" title="数据集中所有数据行都被使用后，停止当前采样线程执行，所有线程都读完所有行时整个执行退出并结束。">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
            </div>
          </Radio>
        </RadioGroup>
      </div>

      <div class="flex-shrink-0 flex items-center">
        <div class="flex-shrink-0 flex items-center mr-2.5">
          <span>共享模式</span>
          <Colon />
        </div>
        <RadioGroup
          :value="sharingMode"
          name="share"
          @change="datasetSharingModeChange">
          <Radio
            v-for="item in sharingModeEnums"
            :key="item.value"
            :value="item.value">
            <div class="flex items-center space-x-1">
              <span>{{ item.message }}</span>
              <Tooltip v-if="item.value === 'ALL_THREAD'" title="所有线程共享同一份数据集数据。">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer" />
              </Tooltip>
              <Tooltip v-else-if="item.value === 'CURRENT_THREAD'" title="每个线程各自复制一份数据集数据。">
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
        <span>引入数据集</span>
      </Button>
    </div>

    <template v-if="loaded">
      <div v-if=" tableData.length === 0" class="flex-1 flex flex-col items-center justify-center">
        <img style="width:100px;" src="../../../../assets/images/nodata.png">
        <div class="flex items-center text-theme-sub-content text-3">
          <span>您尚未引用任何数据集</span>
        </div>
      </div>

      <div v-else>
        <div class="flex items-center table-thead-tr">
          <div class="table-thead-th">名称</div>
          <div class="table-thead-th">添加人</div>
          <div class="table-thead-th">操作</div>
        </div>

        <Collapse v-model:activeKey="collapseActiveKeys" collapsible="disabled">
          <CollapsePanel
            v-for="item in tableData"
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
                    @change="arrowChange($event, item.id)" />
                  <div class="flex-1 truncate">
                    <span
                      :title="item.name"
                      class="truncate cursor-pointer"
                      @click="arrowChange(!collapseActiveKeys.includes(item.id), item.id)">{{ item.name }}</span>
                  </div>
                </div>
                <div class="table-tbody-td" :title="item.createdByName">
                  <div class="flex-1 truncate">{{ item.createdByName }}</div>
                </div>
                <div class="table-tbody-td flex items-center space-x-2.5">
                  <Popconfirm :title="`确定取消引用数据集【${item.name}】吗？`" @confirm="toDelete(item)">
                    <Button
                      title="取消引用"
                      type="text"
                      size="small"
                      class="flex items-center p-0 h-5 leading-5 space-x-1">
                      <Icon icon="icon-qingchu" class="text-3.5" />
                      <span>取消引用</span>
                    </Button>
                  </Popconfirm>

                  <Button
                    title="查看定义"
                    type="text"
                    size="small"
                    class="p-0 h-5 leading-5">
                    <RouterLink
                      class="flex items-center space-x-1"
                      :to="`/data#dataSet?id=${item.id}`"
                      target="_blank">
                      <Icon icon="icon-zhengyan" class="text-3.5" />
                      <span>查看定义</span>
                    </RouterLink>
                  </Button>
                </div>
              </div>
            </template>

            <Tabs size="small" class="ant-tabs-nav-mb-2.5 normal-tabs">
              <template v-if="!item.extraction">
                <TabPane key="value" tab="参数">
                  <StaticParameters :dataSource="item.parameters" class="mb-5" />
                </TabPane>
              </template>

              <template v-else>
                <TabPane key="value" tab="提取">
                  <ExtractParameters
                    :dataSource="item.parameters"
                    :columnIndex="+item.extraction.columnIndex"
                    :hintText="hintTextMap[item.extraction.source]"
                    class="mb-5" />
                </TabPane>
              </template>

              <TabPane key="preview" tab="预览">
                <PreviewData :dataSource="item" />
              </TabPane>

              <TabPane key="use" tab="使用">
                <DataSetUseList :id="item.id" />
              </TabPane>
            </Tabs>
          </CollapsePanel>
        </Collapse>
      </div>
    </template>

    <AsyncComponent :visible="modalVisible">
      <DataSetModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        :selectedNames="selectedNames"
        @ok="selectedVariablesOk" />
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

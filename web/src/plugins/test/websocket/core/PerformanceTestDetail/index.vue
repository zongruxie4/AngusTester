<script lang="ts" setup>
import { ref, defineAsyncComponent, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Tabs, TabPane } from 'ant-design-vue';

import { NoData } from '@xcan-angus/vue-ui';

import {
  allCvsKeys,
  allColumns,
  oneThroughputColumns,
  throughputColumns,
  throughputOptions,
  threadCvsKeys,
  threadOptions,
  threadColumns,
  responseTimeCvsKeys,
  responseTimeOptions,
  responseTimeColumns,
  errorOptions,
  errorCvsKeys,
  errorColumns,
  oneApiErrorColumns
} from './ChartConfig';
import { getCurrentDuration, splitTime } from '../utils';

const { t } = useI18n();

interface Props {
  execInfo: Record<string, any>;
  delayInSeconds:number;
  apiDimensionObj: {[key:string]:{ [key:string]: number[]}};
  indexDimensionObj: {[key:string]:{ [key:string]: number[]}};
  apiNames:string[];
  timestampData:string[];
  isLoaded:boolean;
  brpsUnit:'KB' | 'MB';
  minBrpsUnit: 'KB' | 'MB';
  maxBrpsUnit:'KB' | 'MB';
  meanBrpsUnit:'KB' | 'MB';
  bwpsUnit:'KB' | 'MB';
  minBwpsUnit: 'KB' | 'MB';
  maxBwpsUnit:'KB' | 'MB';
  meanBwpsUnit:'KB' | 'MB';
  stutasCodeData:Record<string, any>;
  errCountList: Record<string, any>[];
  sampleList: Record<string, any>[];
  exception?:{
    codeName:string;
    messageName:string;
    code:string;
    message:string;
  };
  infoMaxQps: number | string;
  infoMaxTps: number | string;
}

const props = withDefaults(defineProps<Props>(), {
  delayInSeconds: 3000,
  exception: undefined
});

const emit = defineEmits<{(e:'setCountTabKey', value:string):void;}>();
const TestBasicInfo = defineAsyncComponent(() => import('@/components/TestBasicInfo/index.vue'));
const AggregateVue = defineAsyncComponent(() => import('./components/Aggregate/index.vue'));
const StatusCode = defineAsyncComponent(() => import('./components/StatusCode/index.vue'));
const CountTemplate = defineAsyncComponent(() => import('./components/CountTemplate.vue'));
const ConterList = defineAsyncComponent(() => import('./components/ConterList/index.vue'));
const Sampling = defineAsyncComponent(() => import('./components/Sampling.vue'));
const NodeData = defineAsyncComponent(() => import('./components/NodeData/index.vue'));
const SuperimposeAnalysis = defineAsyncComponent(() => import('./components/SuperimposeAnalysis/index.vue'));

const nodeDataRef = ref();
const counTabKey = ref('aggregation');

const handleLeftTab = (value) => {
  emit('setCountTabKey', value);
};

const getNumFixed = (str:string) => {
  return str ? Number(str).toFixed(2) : '0';
};

const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + e[f];
};

const countCard = computed(() => {
  return [
    {
      key: 'currentDuration',
      name: t('httpPlugin.performanceTestDetail.countCards.currentDuration'),
      icon: 'icon-yizhihangshijian',
      color: '129, 154, 218'
    },
    {
      key: 'vu',
      name: t('httpPlugin.performanceTestDetail.countCards.vu'),
      icon: 'icon-bingfashu',
      color: '255, 177, 59'
    },
    {
      key: 'qps',
      name: t('httpPlugin.performanceTestDetail.countCards.qps'),
      icon: 'icon-meimiaochaxunshu',
      color: '3, 206, 92'
    },
    {
      key: 'tps',
      name: t('httpPlugin.performanceTestDetail.countCards.tps'),
      icon: 'icon-meimiaoshiwushu',
      color: '45, 142, 255'
    },
    {
      key: 'err',
      name: t('httpPlugin.performanceTestDetail.countCards.err'),
      icon: 'icon-cuowushuai',
      color: '245, 34, 45'
    },
    {
      key: 'currentIterations',
      name: t('httpPlugin.performanceTestDetail.countCards.currentIterations'),
      icon: 'icon-yidiedaicishu',
      color: '3, 185, 208'
    },
    {
      key: 'rt',
      name: t('httpPlugin.performanceTestDetail.countCards.rt'),
      icon: 'icon-pingjunxiangyingshijian',
      color: '255, 129, 0'
    },
    {
      key: 'p50',
      name: t('chart.p50'),
      icon: 'icon-baifenwei',
      color: '201, 119, 255'
    },
    {
      key: 'export',
      name: t('actions.export'),
      icon: 'icon-xiazaida',
      color: '111, 198, 191'
    },
    {
      key: 'upload',
      name: t('actions.upload'),
      icon: 'icon-shangchuanda',
      color: '175, 196, 32'
    }
  ];
});

const aggregateColumns = computed(() => {
  if (props.execInfo?.singleTargetPipeline) {
    return allColumns.map(item => {
      return {
        ...item,
        width: item.dataIndex === 'name' ? '14%' : item.width
      };
    });
  }

  return allColumns;
});

defineExpose({
  restartNode: () => {
    if (nodeDataRef.value) {
      nodeDataRef.value.restart(true);
    }
  }
});
</script>
<template>
  <div>
    <TestBasicInfo
      :value="props.execInfo"
      :exception="props.exception"
      :hasIgnoreAssertions="false" />
    <div class="text-3 flex flex-wrap text-text-sun-content mt-5 -mr-2">
      <div
        v-for="item in countCard"
        :key="item.key"
        class="border boder-border-divider rounded mr-3 p-3.5 flex-none mb-3 border-l-4"
        style="width: calc((100% - 60px) / 5);"
        :style="{
          borderLeft: '4px solid rgb(' + item.color + ')',
        }">
        <div
          class="text-text-title mb-1 text-4 font-semibold flex items-center"
          :style="{ color: 'rgb(' + item.color + ')'}">
          <template v-if="item.key === 'currentDuration'">
            <template v-if="props.execInfo?.durationProgress">
              <span>
                {{ getCurrentDuration(props.execInfo?.currentDuration,props.execInfo?.duration) }}
              </span>
              <em
                class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded"
                style="transform: rotate(25deg);"
                :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
              <span>{{ props.execInfo?.duration? splitTime(props.execInfo.duration)[0]:'--' }}</span>
            </template>
            <template v-else>
              <span>--</span>
              <em
                class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded"
                style="transform: rotate(25deg);"
                :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
              <span>--</span>
            </template>
          </template>
          <template v-if="item.key === 'vu'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.threadPoolActiveSize || "--" }}</span>
            <em
              class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded"
              style="transform: rotate(25deg);"
              :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ props.execInfo?.thread || "--" }}</span>
          </template>
          <template v-if="item.key === 'qps'">
            <span> {{ getNumFixed(props.execInfo?.sampleSummaryInfo?.ops) }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span> {{ props.infoMaxQps || '--' }}</span>
          </template>
          <template v-if="item.key === 'tps'">
            <span> {{ getNumFixed(props.execInfo?.sampleSummaryInfo?.tps) }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span> {{ props.infoMaxTps || '--' }}</span>
          </template>
          <template v-if="item.key === 'rt'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.tranMin ||"-" }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span> {{ props.execInfo?.sampleSummaryInfo?.tranMean ||"--" }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span> {{ props.execInfo?.sampleSummaryInfo?.tranMax ||"--" }}</span>
            <span>&nbsp;{{ t('httpPlugin.performanceTestDetail.units.ms') }}</span>
          </template>
          <template v-if="item.key === 'err'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.errors ||"--" }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ props.execInfo?.sampleSummaryInfo?.errorRate?props.execInfo.sampleSummaryInfo.errorRate+'%':'--' }}</span>
          </template>
          <template v-if="item.key === 'currentIterations'">
            <template v-if="props.execInfo?.iterationsProgress">
              <span> {{ props.execInfo?.currentIterations || "--" }}</span>
              <em
                class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded"
                style="transform: rotate(25deg);"
                :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
              <span>{{ props.execInfo?.iterations || "--" }}</span>
            </template>
            <template v-else>
              <span>--</span>
              <em
                class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded"
                style="transform: rotate(25deg);"
                :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
              <span>--</span>
            </template>
          </template>
          <template v-if="item.key === 'p50'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.tranP50 || "--" }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ props.execInfo?.sampleSummaryInfo?.tranP90 || "--" }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ props.execInfo?.sampleSummaryInfo?.tranP99 || "--" }}</span>
          </template>
          <template v-if="item.key === 'upload'">
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.bwps)? `${formatBytes(props.execInfo?.sampleSummaryInfo?.bwps)}${t('httpPlugin.performanceTestDetail.units.perSecond')}`:'--' }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.writeBytes) || '--' }}</span>
          </template>
          <template v-if="item.key === 'export'">
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.brps)? `${formatBytes(props.execInfo?.sampleSummaryInfo?.brps)}${t('httpPlugin.performanceTestDetail.units.perSecond')}`:'--' }}</span>
            <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" :style="{ 'background-color': 'rgb(' + item.color + ')'}" />
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.readBytes) || '--' }}</span>
          </template>
        </div>
        <div>
          <template v-if="item.key === 'currentDuration'">
            <template v-if="props.execInfo?.durationProgress">
              {{ item.name }}({{ splitTime(props.execInfo.duration)[1] }})
            </template>
            <template v-else>
              {{ item.name }}
            </template>
          </template>
          <template v-else>
            {{ item.name }}
          </template>
        </div>
      </div>
    </div>
    <div class="border border-theme-divider rounded mt-2">
      <Tabs
        v-model:activeKey="counTabKey"
        destroyInactiveTabPane
        size="small"
        tabPosition="left"
        class="count-tab"
        @change="handleLeftTab">
        <TabPane
          key="aggregation"
          :tab="t('httpPlugin.performanceTestDetail.tabs.aggregation')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <AggregateVue
                :isSingleInterface="props.execInfo?.singleTargetPipeline"
                :indexDimensionObj="indexDimensionObj"
                :apiDimensionObj="apiDimensionObj"
                :timestampData="timestampData"
                :apiNames="apiNames"
                :cvsKeys="allCvsKeys"
                :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                :columns="aggregateColumns"
                :brpsUnit="props.brpsUnit"
                :bwpsUnit="props.bwpsUnit"
                :minBrpsUnit="props.minBrpsUnit"
                :maxBrpsUnit="props.maxBrpsUnit"
                :meanBrpsUnit="props.meanBrpsUnit"
                :minBwpsUnit="props.minBwpsUnit"
                :maxBwpsUnit="props.maxBwpsUnit"
                :meanBwpsUnit="props.meanBwpsUnit" />
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
        <TabPane key="throughput" :tab="t('httpPlugin.performanceTestDetail.tabs.throughput')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <CountTemplate
                ref="countTemplateRef"
                :isSingleInterface="props.execInfo?.singleTargetPipeline"
                :apiDimensionObj="apiDimensionObj"
                :indexDimensionObj="indexDimensionObj"
                :timestampData="timestampData"
                :apiNames="apiNames"
                :cvsKeys="props.execInfo?.singleTargetPipeline? ['ops'] : ['ops','tps']"
                :indexOptions="props.execInfo?.singleTargetPipeline?throughputOptions.filter(item => item.value !== 'operations') : throughputOptions"
                :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                :columns="props.execInfo?.singleTargetPipeline? oneThroughputColumns : throughputColumns"
                :brpsUnit="brpsUnit"
                :bwpsUnit="bwpsUnit"
                tabKey="throughput">
              </CountTemplate>
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
        <TabPane key="vu" :tab="t('httpPlugin.performanceTestDetail.tabs.vu')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <CountTemplate
                ref="countTemplateRef"
                :isSingleInterface="props.execInfo?.singleTargetPipeline"
                :apiDimensionObj="apiDimensionObj"
                :indexDimensionObj="indexDimensionObj"
                :timestampData="timestampData"
                :apiNames="apiNames"
                :cvsKeys="threadCvsKeys"
                :indexOptions="threadOptions"
                :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                :columns="threadColumns" />
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
        <TabPane key="responseTime" :tab="t('httpPlugin.performanceTestDetail.tabs.responseTime')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <CountTemplate
                ref="countTemplateRef"
                :isSingleInterface="props.execInfo?.singleTargetPipeline"
                :apiDimensionObj="apiDimensionObj"
                :indexDimensionObj="indexDimensionObj"
                :timestampData="timestampData"
                :apiNames="apiNames"
                :cvsKeys="responseTimeCvsKeys"
                :indexOptions="responseTimeOptions"
                :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                :columns="responseTimeColumns" />
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
        <TabPane key="error" :tab="t('httpPlugin.performanceTestDetail.tabs.error')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <div class="min-h-full">
                <CountTemplate
                  ref="countTemplateRef"
                  tabKey="error"
                  :isSingleInterface="props.execInfo?.singleTargetPipeline"
                  :apiDimensionObj="apiDimensionObj"
                  :indexDimensionObj="indexDimensionObj"
                  :timestampData="timestampData"
                  :apiNames="apiNames"
                  :cvsKeys="props.execInfo?.singleTargetPipeline ? errorCvsKeys.filter(item => item !== 'operations') : errorCvsKeys"
                  :indexOptions="props.execInfo?.singleTargetPipeline ? errorOptions.filter(item => item.value !== 'operations') : errorOptions"
                  :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                  :columns="props.execInfo?.singleTargetPipeline? oneApiErrorColumns : errorColumns" />
                <template v-if="props.errCountList?.length">
                  <ConterList class="mt-1.5" :list="props.errCountList" />
                </template>
                <template v-if="props.sampleList?.length">
                  <Sampling :class="props.errCountList?.length? 'mt-3.5':'mt-1.5'" :sampleList="props.sampleList" />
                </template>
              </div>
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
        <TabPane key="httpCode" :tab="t('httpPlugin.performanceTestDetail.tabs.httpCode')">
          <template v-if="isLoaded">
            <StatusCode
              :isSingleInterface="props.execInfo?.singleTargetPipeline"
              :stutasCodeData="props.stutasCodeData" />
          </template>
        </TabPane>
        <TabPane key="node" :tab="t('httpPlugin.performanceTestDetail.tabs.node')">
          <NodeData
            ref="nodeDataRef"
            :activeChart="counTabKey"
            :delayInSeconds="props.delayInSeconds"
            :reportInterval="props.execInfo?.reportInterval"
            :startTime="props.execInfo?.actualStartDate || props.execInfo?.createdDate"
            :endTime="props.execInfo?.endDate"
            :appNodes="props.execInfo?.appNodes"
            :execNodes="props.execInfo?.execNodes"
            :status="props.execInfo?.status.value" />
        </TabPane>
        <TabPane key="analyze" :tab="t('httpPlugin.performanceTestDetail.tabs.analyze')">
          <template v-if="isLoaded">
            <template v-if="timestampData.length > 0">
              <SuperimposeAnalysis
                ref="superimposeAnalysisRef"
                :isSingleInterface="props.execInfo?.singleTargetPipeline"
                :apiDimensionObj="apiDimensionObj"
                :indexDimensionObj="indexDimensionObj"
                :timestampData="timestampData"
                :apiNames="apiNames"
                :cvsKeys="allCvsKeys"
                :pipelineTargetMappings="props.execInfo?.pipelineTargetMappings"
                tabKey="analyze" />
            </template>
            <template v-else>
              <NoData class="h-full" />
            </template>
          </template>
        </TabPane>
      </Tabs>
    </div>
  </div>
</template>
<style scoped>
.count-tab :deep(.ant-tabs-content-holder) {
  height: auto;
    padding-right: 0;
  padding-left: 0;
}

.count-tab :deep(.ant-tabs-content-holder > .ant-tabs-content > .ant-tabs-tabpane) {
  padding: 14px;
}

.count-tab :deep(.ant-tabs-nav-list) {
  background-color: var(--table-header-bg);
}

.count-tab :deep(.ant-tabs-nav-wrap) {
  padding-left: 0;
}

.count-tab :deep(.ant-tabs-nav-list .ant-tabs-tab) {
  min-width: 160px;
  height: 60px;
  margin: 0;
}

.count-tab :deep(.ant-tabs-tab.ant-tabs-tab-active) {
  background-color: rgba(255,255,255,100%);
}

.count-tab :deep(.ant-tabs-ink-bar) {
  left: 0;
}
</style>


<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import * as echarts from 'echarts';
import elementResizeDetector from 'element-resize-detector';

import { analysis } from 'src/api/tester';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const { t } = useI18n();
const erd = elementResizeDetector({ strategy: 'scroll' });

const loading = ref(false);
const state = reactive({
  statistic: {
    allService: '0',
    serviceByLastWeek: '0',
    serviceByLastMonth: '0',
    allApis: '0',
    apisByLastWeek: '0',
    apisByLastMonth: '0',
    allUnarchivedApis: '0',
    unarchivedApisByLastWeek: '0',
    unarchivedApisByLastMonth: '0'
  }
});

const loadMyStatistics = async (): Promise<void> => {
  loading.value = true;
  const params = {
    creatorObjectType: 'USER',
    creatorObjectId: props.userInfo?.id,
    projectId: props.projectId
  };
  const [error, res] = await analysis.getDataStatistics(params);
  loading.value = false;
  if (error || utils._typeof(res?.data) !== 'object') {
    return;
  }

  state.statistic = res.data;
};

const allVariable = ref(0);
const allDataset = ref(0);
const allDatasource = ref(0);
const allFile = ref(0);
const loadAllStatistics = async (): Promise<void> => {
  loading.value = true;
  const params = {
    projectId: props.projectId
  };
  const [error, res] = await analysis.getDataStatistics(params);
  loading.value = false;
  if (error || utils._typeof(res?.data) !== 'object') {
    return;
  }
  const { data } = res;
  allVariable.value = data.allVariable;
  allDataset.value = data.allDataset;
  allDatasource.value = data.allDatasource;
  allFile.value = data.allFile;
  const { IN_USE = 0, NOT_IN_USE = 0 } = data.variableByUse;
  variableOption.series[0].data[0].value = IN_USE;
  variableOption.series[0].data[1].value = NOT_IN_USE;

  const { SPACE = 0, DIRECTORY = 0, FILE = 0 } = data.fileByResourceType;
  fileOption.series[0].data = [SPACE, DIRECTORY, FILE];

  const { H2 = 0, HSQLDB = 0, SQLITE = 0, POSTGRES = 0, MARIADB = 0, MYSQL = 0, ORACLE = 0, SQLSERVER = 0 } = data.datasourceByDb;
  const dataSourceData = [H2, HSQLDB, SQLITE, POSTGRES, MARIADB, MYSQL, ORACLE, SQLSERVER];
  dateSourceOption.series[0].data.forEach((item, idx) => {
    item.value = dataSourceData[idx];
  });

  if (data.datasetByUse) {
    const { IN_USE = 0, NOT_IN_USE = 0 } = data.datasetByUse;
    dataSetOption.series[0].data[0].value = IN_USE;
    dataSetOption.series[0].data[1].value = NOT_IN_USE;
  } else {
    dataSetOption.series[0].data[0].value = 0;
    dataSetOption.series[0].data[1].value = 0;
  }

  variableEchart.setOption(variableOption);
  dataSetEchart.setOption(dataSetOption);
  fileEchart.setOption(fileOption);
  dataSourceEchart.setOption(dateSourceOption);
};

const statisticConfig = [
  {
    topClass: 'huang-top',
    bottomClass: 'huang-bottom',
    total: 'allVariable',
    week: 'variableByLastWeek',
    month: 'variableByLastMonth',
    name: t('dataHome.statistics.totalVariable')
  },
  {
    topClass: 'hong-top',
    bottomClass: 'hong-bottom',
    total: 'allDataset',
    week: 'datasetByLastWeek',
    month: 'datasetByLastMonth',
    name: t('dataHome.statistics.dataset')
  },
  {
    topClass: 'lan-top',
    bottomClass: 'lan-bottom',
    total: 'allFile',
    week: 'fileByLastWeek',
    month: 'fileByLastMonth',
    name: t('dataHome.statistics.file')
  },
  {
    topClass: 'qin-top',
    bottomClass: 'qin-bottom',
    total: 'allDatasource',
    week: 'datasourceByLastWeek',
    month: 'datasourceByLastMonth',
    name: t('dataHome.statistics.dataSourceSet')
  }
];

const echartsWrapRef = ref();
const variableRef = ref();
const dataSetRef = ref();
const fileRef = ref();
const dataSourceRef = ref();

let variableEchart;
let dataSetEchart;
let fileEchart;
let dataSourceEchart;
const variableOption = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    show: true
  },
  legend: {
    bottom: '0',
    right: 'center',
    orient: 'horizontal',
    itemHeight: 14,
    itemWidth: 14
  },
  grid: {
    top: '8%',
    left: '3%',
    right: '3%',
    bottom: '3%',
    containLabel: true
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['30%', '40%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('dataHome.statistics.usedCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('dataHome.statistics.unusedCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(173, 177, 185, 1)'
          }
        }
      ]
    }
  ]
};

const dataSetOption = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    tooltip: {
      show: true
    }
  },
  legend: {
    bottom: '0',
    right: 'center',
    orient: 'horizontal',
    itemHeight: 14,
    itemWidth: 14
  },
  grid: {
    top: '8%',
    left: '3%',
    right: '3%',
    bottom: '3%',
    containLabel: true
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['30%', '40%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('dataHome.statistics.usedCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('dataHome.statistics.unusedCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(173, 177, 185, 1)'
          }
        }
      ]
    }
  ]
};

const fileOption = {
  tooltip: {
    show: true
  },
  grid: {
    left: '50',
    right: 10,
    bottom: 20
  },
  xAxis: {
    type: 'category',
    data: [t('dataHome.statistics.space'), t('dataHome.statistics.directory'), t('dataHome.statistics.file')]
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      type: 'bar',
      barMaxWidth: 16,
      data: [0, 0, 0],
      label: {
        show: true,
        position: 'top'
      },
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      }
    }
  ]
};
const dataSourceType = ['H2', 'HSQLDB', 'SQLITE', 'POSTGRES', 'MARIADB', 'MYSQL', 'ORACLE', 'SQLSERVER'];
const dateSourceOption = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    show: true
  },
  legend: {
    bottom: '0',
    left: 'center',
    orient: 'horizontal',
    itemHeight: 10,
    itemWidth: 10,
    itemGap: 5
  },
  grid: {
    top: '0%',
    left: '0%',
    right: '0%',
    bottom: '0%',
    containLabel: true
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['30', '40%'],
      center: ['50%', '35%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: dataSourceType.map(type => {
        return {
          name: type,
          value: 0
        };
      })
    }
  ]
};

const resizeHandler = () => {
  variableEchart.resize();
  dataSetEchart.resize();
  fileEchart.resize();
  dataSourceEchart.resize();
};

onMounted(() => {
  loadMyStatistics();
  loadAllStatistics();
  variableEchart = echarts.init(variableRef.value);
  dataSetEchart = echarts.init(dataSetRef.value);
  fileEchart = echarts.init(fileRef.value);
  dataSourceEchart = echarts.init(dataSourceRef.value);
  variableEchart.setOption(variableOption);
  dataSetEchart.setOption(dataSetOption);
  fileEchart.setOption(fileOption);
  dataSourceEchart.setOption(dateSourceOption);

  watch(() => props.projectId, () => {
    loadMyStatistics();
    loadAllStatistics();
  });
  erd.listenTo(echartsWrapRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(echartsWrapRef.value, resizeHandler);
});

</script>
<template>
  <div class="mb-7.5 text-3 leading-5">
    <div class="text-3.5 font-semibold mb-3">{{ t('dataHome.statistics.myAdded') }}</div>
    <div class="flex space-x-3.75 text-content">
      <div
        v-for="item,index in statisticConfig"
        :key="index"
        class="rounded w-1/4 relative">
        <div class="vertical-layout-top" :class="item.topClass"><div>{{ item.name }}</div><div>{{ state.statistic[item.total] }}</div></div>
        <div class="vertical-layout-bottom" :class="item.bottomClass">
          <div>
            <div>{{ t('dataHome.statistics.last7Days') }}</div><div>{{ state.statistic[item.week] }}</div>
          </div>
          <div>
            <div>{{ t('dataHome.statistics.last30Days') }}</div>
            <div>{{ state.statistic[item.month] }}</div>
          </div>
        </div>
        <template v-if="index ===0">
          <img src="./images/icon-service.png" class="w-15 absolute right-0 top-0" />
        </template>
        <template v-if="index ===1">
          <img src="./images/icon-api.png" class="w-15 absolute right-0 top-0" />
        </template>
        <template v-if="index ===2">
          <img src="./images/weiuidangicon.png" class="w-15 absolute right-0 top-0" />
        </template>
        <template v-if="index ===3">
          <img src="./images/icon-task.png" class="w-15 absolute right-0 top-0" />
        </template>
      </div>
    </div>
  </div>

  <div class="text-3 leading-5">
    <div class="text-3.5 font-semibold mb-3">{{ t('dataHome.statistics.resourceStatistics') }}</div>
    <div ref="echartsWrapRef" class="flex space-x-3.75">
      <div class="border border-theme-text-box w-1/4 p-2 rounded">
        <div class="font-semibold flex items-center px-2">{{ t('dataHome.statistics.totalVariable') }} <span class="text-4 ml-2">{{ allVariable }}</span></div>
        <div ref="variableRef" class="w-full h-50"></div>
      </div>
      <div class="border border-theme-text-box w-1/4 p-2 rounded">
        <div class="font-semibold flex items-center px-2">{{ t('dataHome.statistics.dataset') }} <span class="text-4 ml-2">{{ allDataset }}</span></div>
        <div ref="dataSetRef" class="w-full h-50"></div>
      </div>
      <div class="border border-theme-text-box w-1/4 p-2 rounded">
        <div class="font-semibold">{{ t('dataHome.statistics.file') }} <span class="text-4 ml-2">{{ allFile }}</span></div>
        <div ref="fileRef" class="w-full h-50"></div>
      </div>
      <div class="border border-theme-text-box w-1/4 px-2 pt-2 rounded">
        <div class="font-semibold">{{ t('dataHome.statistics.dataSourceSet') }} <span class="text-4 ml-2">{{ allDatasource }}</span></div>
        <div ref="dataSourceRef" class="w-full h-50"></div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.vertical-layout-top{
  @apply p-3.5 rounded-t;
}

.vertical-layout-bottom{
 @apply  p-3.5 rounded-b;
}

.vertical-layout-top > div:first-child{
 margin-bottom: 4px
}

.vertical-layout-bottom > div:first-child{
  margin-bottom: 4px
}

.vertical-layout-bottom > div > div:first-child{
  margin-bottom: 4px
}

@media (min-width: 1400px) {
  .vertical-layout-top {
        display: flex;
        align-items: center;
   }

  .vertical-layout-bottom > div {
    display: flex;
    align-items: center;
  }

  .vertical-layout-top > div:first-child{
    margin-right: 10px;
    margin-bottom: 0;
  }

  .vertical-layout-bottom > div:first-child{
    margin-bottom: 4px
  }

  .vertical-layout-bottom > div > div:first-child{
    margin-right: 10px;
    margin-bottom: 0;
  }
}

@media (min-width: 1600px) {
  .vertical-layout-top {
      display: flex;
      align-items: center;
   }

  .vertical-layout-bottom {
    display: flex;
    align-items: center;
  }

  .vertical-layout-bottom > div {
      display: flex;
      align-items: center;
      width: 50%
   }

  .vertical-layout-top > div:first-child{
    margin-right: 10px;
    margin-bottom: 0;
  }

  .vertical-layout-bottom > div:first-child{
    margin-bottom: 0
  }

  .vertical-layout-bottom > div > div:first-child{
    margin-right: 10px;
    margin-bottom: 0;
  }
}

.zi-top{
  background: linear-gradient(to right, #cab9ff, #f3f0ff);
}

.zi-bottom {
  background-color: #f3f0ff;
}

.huang-top{
  background: linear-gradient(to right, #f7cb71, #fef7df);
}

.huang-bottom {
  background-color: #fef7df;
}

.hong-top{
  background: linear-gradient(to right, #ffb99f, #fff0e8);
}

.hong-bottom {
  background-color: #fff0e8;
}

.lan-top{
  background: linear-gradient(to right, #d1e7ff, #eff6ff);
}

.lan-bottom {
  background-color: #eff6ff;
}

.qin-top{
  background: linear-gradient(to right, #87d7ee, #e2f7fd);
}

.qin-bottom {
  background-color: #e2f7fd;
}
</style>

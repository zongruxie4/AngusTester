<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const statusColorSet = {
  0: 'rgba(200, 202, 208, 1)',
  1: '#52C41A',
  2: '#FFB925',
  3: '#FF8100',
  4: '#7F91FF'
};

const methodColorSet = {
  get: 'rgba(30, 136, 229, 1)',
  head: '#67D7FF',
  post: 'rgba(51, 183, 130, 1)',
  put: 'rgba(255, 167, 38, 1)',
  patch: 'rgba(171, 71, 188, 1)',
  delete: 'rgba(255, 82, 82, 1)',
  options: 'rgba(0, 150, 136, 1)',
  trace: '#7F91FF'
};

const apisStatusRef = ref();
const methodRef = ref();

const apisStatusOption = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {},
  grid: {
    top: '8%',
    left: '3%',
    right: '8%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    splitLine: { show: false }
  },
  yAxis: {
    type: 'category',
    axisTick: { show: false },
    splitLine: { show: false },
    axisLine: { show: false },
    data: [t('reportPreview.services.apisGroup.byStatus.statuses.unknown'), t('reportPreview.services.apisGroup.byStatus.statuses.designing'), t('reportPreview.services.apisGroup.byStatus.statuses.devInProgress'), t('reportPreview.services.apisGroup.byStatus.statuses.devCompleted'), t('reportPreview.services.apisGroup.byStatus.statuses.published')]
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: 20,
      label: {
        show: true,
        position: 'right'
      },
      data: [{
        value: 0,
        itemStyle: {
          color: '#F7F8FB'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#FF8100'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#FFB925'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#2D8EFF'
        }
      }]
    }
  ]
};

const methodOptions = {
  title: {
    text: 0,
    subtext: t('reportPreview.services.apisGroup.total'),
    left: '29.5%',
    top: '35%',
    padding: 2,
    textAlign: 'center',
    textStyle: {
      fontSize: 14,
      width: 120,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 14
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 5,
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true
      },
      data: [
        {
          name: 'PUT',
          value: 0,
          itemStyle: {
            color: methodColorSet.put
          }
        },
        {
          name: 'POST',
          value: 0,
          itemStyle: {
            color: methodColorSet.post
          }
        },
        {
          name: 'HEAD',
          value: 0,
          itemStyle: {
            color: methodColorSet.head
          }
        },
        {
          name: 'GET',
          value: 0,
          itemStyle: {
            color: methodColorSet.get
          }
        },
        {
          name: 'DELETE',
          value: 0,
          itemStyle: {
            color: methodColorSet.delete
          }
        },
        {
          name: 'PATCH',
          value: 0,
          itemStyle: {
            color: methodColorSet.patch
          }
        },
        {
          name: 'OPTIONS',
          value: 0,
          itemStyle: {
            color: methodColorSet.options
          }
        },
        {
          name: 'TRACE',
          value: 0,
          itemStyle: {
            color: methodColorSet.trace
          }
        }
      ]
    }
  ]
};

let statusEchart;
let methodEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    if (newValue?.content?.testResult?.apisByStatus) {
      const { UNKNOWN = 0, IN_DESIGN = 0, IN_DEV = 0, DEV_COMPLETED = 0, RELEASED = 0 } = newValue?.content?.testResult?.apisByStatus;

      apisStatusOption.series[0].data = [UNKNOWN, IN_DESIGN, IN_DEV, DEV_COMPLETED, RELEASED].map((value, idx) => {
        return {
          value,
          itemStyle: {
            color: statusColorSet[idx]
          }
        };
      });
    }

    if (newValue?.content?.testResult?.apisByMethod) {
      const { PUT = 0, POST = 0, DELETE = 0, GET = 0, HEAD = 0, PATCH = 0, OPTIONS = 0, TRACE = 0 } = newValue.content.testResult.apisByMethod;
      const methodData = [PUT, POST, HEAD, GET, DELETE, PATCH, OPTIONS, TRACE];
      methodOptions.series[0].data.forEach((item, idx) => {
        item.value = methodData[idx];
      });
    } else {
      methodOptions.series[0].data.forEach(item => {
        item.value = 0;
      });
    }

    statusEchart = echarts.init(apisStatusRef.value);
    methodEchart = echarts.init(methodRef.value);
    statusEchart.setOption(apisStatusOption);
    methodEchart.setOption(methodOptions);
  }, {
    immediate: true
  });
});

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a2" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.services.apisGroup.title') }}</span>
    </h1>

    <h1 class="text-theme-title font-medium mb-3">
      <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.services.apisGroup.byStatus.title') }}</span>
    </h1>

    <div ref="apisStatusRef" class="flex-1 h-30 w-120 mb-7">
    </div>

    <h1 class="text-theme-title font-medium mb-3">
      <span id="a3.2" class="text-3 text-theme-title font-medium">{{ t('reportPreview.services.apisGroup.byRequestMethod.title') }}</span>
    </h1>

    <div ref="methodRef" class="flex-1 h-30  w-120">
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>

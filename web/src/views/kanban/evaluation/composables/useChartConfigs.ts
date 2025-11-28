import { ChartConfig } from '../types';
import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

/**
 * Gets color based on score value
 */
const getScoreColor = (score: number): string => {
  if (score >= 9) return '#52c41a'; // green
  if (score >= 6) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

const getRateColor = (rate: number): string => {
  if (rate >= 90) return '#52c41a'; // green
  if (rate >= 60) return '#faad14'; // orange
  return '#ff4d4f'; // red
};

/**
 * Creates score pie chart configuration (circular progress)
 */
export const createScorePieConfig = (score: number, title: string): ChartConfig => {
  const color = getScoreColor(score);
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        return `${title}: ${score}${t('kanban.evaluation.statistics.points')}`;
      }
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['60%', '85%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: false
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: score,
            itemStyle: { color: color }
          },
          {
            value: 10 - score,
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '35%',
        z: 10,
        style: {
          text: `${Math.round(score)}分`,
          fontSize: 32,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      },
      // {
      //   type: 'text',
      //   left: 'center',
      //   bottom: '0%',
      //   z: 10,
      //   style: {
      //     text: title,
      //     fontSize: 14,
      //     fill: '#666',
      //     textAlign: 'center'
      //   }
      // }
    ]
  };
};

/**
 * Creates pie chart configuration for completion rates
 */
export const createCompletionPieConfig = (rate: number, completed: number, total: number, title: string): ChartConfig => {
  const color = getScoreColor(rate);
  const remaining = total - completed;
  
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{d}%',
          fontSize: 16,
          fontWeight: 'bold',
          color: '#333'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: completed,
            name: t('kanban.evaluation.chartLabels.completed'),
            itemStyle: { color: color }
          },
          {
            value: remaining,
            name: t('kanban.evaluation.chartLabels.incomplete'),
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        z: 10,
        style: {
          text: `${rate}%`,
          fontSize: 28,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      },
      {
        type: 'text',
        left: 'center',
        bottom: '0%',
        z: 10,
        style: {
          text: title,
          fontSize: 14,
          fill: '#666',
          textAlign: 'center'
        }
      }
    ]
  };
};

/**
 * Creates simple progress pie chart configuration
 */
export const createSimpleProgressPieConfig = (rate: number): ChartConfig => {
  const color = getRateColor(rate);
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{d}%'
    },
    series: [
      {
        type: 'pie',
        radius: ['70%', '90%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: false
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: rate,
            itemStyle: { color: color }
          },
          {
            value: 100 - rate,
            itemStyle: { color: '#E4E7ED' }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'center',
        z: 10,
        style: {
          text: `${rate}%`,
          fontSize: 24,
          fontWeight: 'bold',
          fill: color,
          textAlign: 'center'
        }
      }
    ]
  };
};

/**
 * Creates radar chart configuration for quality scores
 */
export const createQualityRadarConfig = (
  compatibility: number,
  usability: number,
  maintainability: number,
  extensibility: number,
  security: number
): ChartConfig => {
  return {
    tooltip: {
      trigger: 'axis'
    },
    radar: {
      indicator: [
        { name: t('kanban.evaluation.qualityRadar.compatibility'), max: 10 },
        { name: t('kanban.evaluation.qualityRadar.usability'), max: 10 },
        { name: t('kanban.evaluation.qualityRadar.maintainability'), max: 10 },
        { name: t('kanban.evaluation.qualityRadar.scalability'), max: 10 },
        { name: t('kanban.evaluation.qualityRadar.security'), max: 10 }
      ],
      center: ['50%', '50%'],
      radius: '70%',
      axisName: {
        fontSize: 14,
        color: '#666'
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(250, 250, 250, 0.3)', 'rgba(200, 200, 200, 0.1)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
    },
    series: [
      {
        name: t('kanban.evaluation.qualityRadar.qualityScore'),
        type: 'radar',
        data: [
          {
            value: [compatibility, usability, maintainability, extensibility],
            name: t('kanban.evaluation.qualityRadar.score'),
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)'
            },
            lineStyle: {
              color: '#1890ff',
              width: 2
            },
            itemStyle: {
              color: '#1890ff'
            },
            label: {
              show: true,
              formatter: '{c}',
              fontSize: 12,
              color: '#333'
            }
          }
        ]
      }
    ]
  };
};

/**
 * Creates bar chart configuration for statistics
 */
export const createStatisticsBarConfig = (labels: string[], values: number[]): ChartConfig => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: {
        alignWithLabel: true
      },
      axisLabel: {
        fontSize: 12,
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: t('kanban.evaluation.chartLabels.value'),
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: '#1890ff'
              },
              {
                offset: 1,
                color: '#40a9ff'
              }
            ]
          },
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 12,
          color: '#333'
        },
        data: values
      }
    ]
  };
};

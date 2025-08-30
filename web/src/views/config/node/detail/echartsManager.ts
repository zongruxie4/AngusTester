import * as echarts from 'echarts';
import { Ref, reactive, ref } from 'vue';
import { formatBytesToUnit } from '@/utils/common';

// ECharts配置选项
export const echartsOpt = {
  title: {
    text: ' '
  },
  tooltip: {
    trigger: 'axis',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    top: '3%',
    left: '3%',
    right: '3%',
    bottom: '12%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [] as string[],
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  yAxis: {
    type: 'value',
    splitLine: {
      show: true,
      lineStyle: {
        type: 'dashed'
      }
    }
  },
  series: [] as any[]
};

export const defaultLegend = {
  type: 'plain',
  data: [] as string[],
  y: 'bottom',
  x: 'center'
};

// 获取默认线条配置
export const getDefaultLineConfig = () => {
  return {
    data: [] as number[],
    type: 'line',
    smooth: true,
    showSymbol: false,
    lineStyle: {
      width: 1
    }
  };
};

// ECharts管理器类
export class EChartsManager {
  private myEchart: echarts.ECharts | null = null;
  private chartsData: any[] = [];
  private memoryTableData: any[] = [];
  private diskTableData: any[] = [];
  private networkTableData: any[] = [];

  // 图表选项缓存
  private memoryEchartOption: any = null;
  private memoryPercentEchartOption: any = null;
  private diskChartOption: any = null;
  private percentChartOption: any = null;
  private rateChartOption: any = null;
  private bytesRateChartOption: any = null;
  private networkChartOption: any = null;
  private bytesChartOption: any = null;
  private packetChartOption: any = null;

  private tableData: Ref<any[]> = ref([]);
  private sourceUse: any = reactive({
    cpu: 0,
    cpuPercent: 0,
    cpuTotal: 0,
    memory: '0',
    memoryPercent: 0,
    memoryTotal: '0',
    swap: '0',
    swapPercent: 0,
    swapTotal: '0',
    disk: '0',
    diskPercent: 0,
    diskTotal: '0',
    txBytesRate: 0,
    rxBytesRate: 0,
    txBytes: '0',
    rxBytes: '0'
  });

  // 初始化ECharts实例
  initEcharts (echartRef: HTMLElement) {
    if (echartRef) {
      this.myEchart = echarts.init(echartRef);
      this.myEchart.setOption(echartsOpt);
    }
  }

  // 设置图表数据
  setChartsData (data: any[]) {
    this.chartsData = data;
  }

  // 获取表格数据
  getTableData () {
    return this.tableData;
  }

  // 获取资源使用数据
  getSourceUse () {
    return this.sourceUse;
  }

  // 加载CPU图表数据
  loadCpuEchartData (data: any[], t: (key: string) => string, notMerge = true) {
    this.chartsData = data;
    const dataType = [
      t('node.nodeDetail.chartOptions.cpu.idle'),
      t('node.nodeDetail.chartOptions.cpu.sys'),
      t('node.nodeDetail.chartOptions.cpu.user'),
      t('node.nodeDetail.chartOptions.cpu.wait'),
      t('node.nodeDetail.chartOptions.cpu.other'),
      t('node.nodeDetail.chartOptions.cpu.total')
    ];

    const seriesData = dataType.map(type => {
      return {
        ...getDefaultLineConfig(),
        name: type
      };
    });

    this.chartsData.forEach(item => {
      const cpusValues = item.cvsCpu.split(',');
      cpusValues.forEach((val: string, idx: number) => {
        seriesData[idx].data.push(+(+val).toFixed(2));
      });
    });

    if (this.chartsData.length) {
      this.tableData.value = dataType.map((name, idx) => {
        const total = seriesData[idx].data.reduce((pre: number, current: number) => {
          return pre + current;
        }, 0);
        const average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
        return {
          name,
          unit: '%',
          average: average + '%',
          high: Math.max(...(seriesData[idx].data)) + '%',
          low: Math.min(...(seriesData[idx].data)) + '%',
          latest: seriesData[idx].data[seriesData[idx].data.length - 1] + '%'
        };
      });
    } else {
      this.tableData.value = [];
    }

    const legend = notMerge
      ? {
          legend: {
            type: 'plain',
            data: dataType,
            y: 'bottom',
            x: 'center'
          }
        }
      : {};

    if (this.myEchart) {
      this.myEchart.setOption({
        ...echartsOpt,
        ...legend,
        xAxis: [
          {
            data: this.chartsData.map(i => i.timestamp)
          }
        ],
        series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [60] }] : seriesData
      }, notMerge);
    }
  }

  // 加载内存图表数据
  loadMemoryEchartData (data: any[], t: (key: string) => string, showMemoryPercentChart: boolean, notMerge = true) {
    const dataType = [
      t('node.nodeDetail.chartOptions.memory.free'),
      t('node.nodeDetail.chartOptions.memory.used'),
      t('node.nodeDetail.chartOptions.memory.freePercent'),
      t('node.nodeDetail.chartOptions.memory.usedPercent'),
      t('node.nodeDetail.chartOptions.memory.actualFree'),
      t('node.nodeDetail.chartOptions.memory.actualUsed'),
      t('node.nodeDetail.chartOptions.memory.actualFreePercent'),
      t('node.nodeDetail.chartOptions.memory.actualUsedPercent'),
      t('node.nodeDetail.chartOptions.memory.swapFree'),
      t('node.nodeDetail.chartOptions.memory.swapUsed')
    ];

    const dataTypeKey = [
      'free', 'used', 'freePercent', 'usedPercent', 'actualFree', 'actualUsed',
      'actualFreePercent', 'actualUsedPercent', 'swapFree', 'swapUsed'
    ];

    this.chartsData = data;
    const seriesData = dataType.map(type => {
      return {
        ...getDefaultLineConfig(),
        name: type
      };
    });

    this.chartsData.forEach(item => {
      const values = item.cvsMemory.split(',');
      values.forEach((val: string, idx: number) => {
        if (dataTypeKey[idx].includes('Percent')) {
          seriesData[idx].data.push(+val);
        } else {
          const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
          const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
          seriesData[idx].data.push(numericValue);
        }
      });
    });

    if (this.chartsData.length) {
      this.memoryTableData = dataType.map((name, idx) => {
        const total = seriesData[idx].data.reduce((pre: number, current: number) => {
          return pre + current;
        }, 0);
        let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
        let high = Math.max(...(seriesData[idx].data)) + '';
        let low = Math.min(...(seriesData[idx].data)) + '';
        let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';

        if (dataTypeKey[idx].includes('Percent')) {
          average += '%';
          high += '%';
          low += '%';
          latest += '%';
        } else {
          average += ' GB';
          high += ' GB';
          low += ' GB';
          latest += ' GB';
        }

        return {
          name,
          unit: dataTypeKey[idx].includes('Percent') ? '%' : 'B',
          average: average,
          high: high,
          low: low,
          latest: latest
        };
      });
    } else {
      this.memoryTableData = [];
    }

    if (showMemoryPercentChart) {
      this.tableData.value = this.memoryTableData.filter(i => i.unit === '%');
    } else {
      this.tableData.value = this.memoryTableData.filter(i => i.unit !== '%');
    }

    const seriesPercentData = seriesData.splice(2, 2).concat(seriesData.splice(4, 2));
    const percentDataType = dataType.splice(2, 2).concat(dataType.splice(4, 2));
    const legend = {
      ...defaultLegend,
      data: dataType
    };
    const percentLegend = {
      ...defaultLegend,
      data: percentDataType
    };

    this.memoryEchartOption = {
      ...echartsOpt,
      legend: legend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
    };

    this.memoryPercentEchartOption = {
      ...echartsOpt,
      legend: percentLegend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: seriesPercentData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesPercentData
    };

    if (this.myEchart) {
      this.myEchart.setOption(
        showMemoryPercentChart ? this.memoryPercentEchartOption : this.memoryEchartOption,
        notMerge
      );
    }
  }

  // 加载磁盘图表数据
  loadDiskEchartData (
    data: any[],
    t: (key: string) => string,
    diskChartKey: string,
    notMerge = true
  ) {
    this.chartsData = data;
    const dataType = [
      t('node.nodeDetail.chartOptions.disk.total'),
      t('node.nodeDetail.chartOptions.disk.free'),
      t('node.nodeDetail.chartOptions.disk.used'),
      t('node.nodeDetail.chartOptions.disk.avail'),
      t('node.nodeDetail.chartOptions.disk.usePercent'),
      t('node.nodeDetail.chartOptions.disk.readsRate'),
      t('node.nodeDetail.chartOptions.disk.writesRate'),
      t('node.nodeDetail.chartOptions.disk.readBytesRate'),
      t('node.nodeDetail.chartOptions.disk.writeBytesRate')
    ];

    const dataTypeKey = [
      'total', 'free', 'used', 'avail', 'usePercent', 'readsRate', 'writesRate',
      'readBytesRate', 'writeBytesRate'
    ];

    const seriesData = dataType.map(type => {
      return {
        ...getDefaultLineConfig(),
        name: type
      };
    });

    this.chartsData.forEach(item => {
      const diskValues = item.cvsValue.split(',');
      diskValues.forEach((val: string, idx: number) => {
        if (dataTypeKey[idx].includes('Percent') || dataTypeKey[idx].includes('BytesRate') || dataTypeKey[idx].includes('Rate')) {
          seriesData[idx].data.push(+val);
        } else {
          const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
          const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
          seriesData[idx].data.push(numericValue);
        }
      });
    });

    if (this.chartsData.length) {
      this.diskTableData = dataType.map((name, idx) => {
        const total = seriesData[idx].data.reduce((pre: number, current: number) => {
          return pre + current;
        }, 0);
        let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
        let high = Math.max(...(seriesData[idx].data)) + '';
        let low = Math.min(...(seriesData[idx].data)) + '';
        let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';
        let unit;

        if (dataTypeKey[idx].includes('Percent')) {
          average += '%';
          high += '%';
          low += '%';
          latest += '%';
          unit = '%';
        } else if (dataTypeKey[idx].includes('BytesRate')) {
          average += ' MB/s';
          high += ' MB/s';
          low += ' MB/s';
          latest += 'MB/s';
          unit = 'MB/s';
        } else if (dataTypeKey[idx].includes('Rate')) {
          average += ' IO/s';
          high += ' IO/s';
          low += ' IO/s';
          latest += ' IO/s';
          unit = 'IO/s';
        } else {
          average += ' GB';
          high += ' GB';
          low += ' GB';
          latest += ' GB';
          unit = 'GB';
        }

        return {
          name,
          average: average,
          high: high,
          low: low,
          latest: latest,
          unit
        };
      });
    } else {
      this.diskTableData = [];
    }

    switch (diskChartKey) {
      case 'disk':
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'GB');
        break;
      case 'percent':
        this.tableData.value = this.diskTableData.filter(i => i.unit === '%');
        break;
      case 'rate':
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'IO/s');
        break;
      case 'bytesRate':
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'MB/s');
        break;
      default:
        this.tableData.value = [];
    }

    const percentSeriesData = seriesData.splice(4, 1);
    const percentDataType = dataType.splice(4, 1);
    const percentLegend = { ...defaultLegend, data: percentDataType };

    this.percentChartOption = {
      ...echartsOpt,
      legend: percentLegend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: percentSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : percentSeriesData
    };

    const rateSeriesData = seriesData.splice(4, 2);
    const rateDataType = dataType.splice(4, 2);
    const rateLegend = { ...defaultLegend, data: rateDataType };

    this.rateChartOption = {
      ...echartsOpt,
      legend: rateLegend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: rateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : rateSeriesData
    };

    const bytesRateSeriesData = seriesData.splice(4, 2);
    const bytesRateDataType = dataType.splice(4, 2);
    const bytesLegend = { ...defaultLegend, data: bytesRateDataType };

    this.bytesRateChartOption = {
      ...echartsOpt,
      legend: bytesLegend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: bytesRateSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : bytesRateSeriesData
    };

    const legend = { ...defaultLegend, data: dataType };

    this.diskChartOption = {
      ...echartsOpt,
      legend: legend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
    };

    let showEchartOptions;
    if (diskChartKey === 'disk') {
      showEchartOptions = this.diskChartOption;
    }
    if (diskChartKey === 'percent') {
      showEchartOptions = this.percentChartOption;
    }
    if (diskChartKey === 'rate') {
      showEchartOptions = this.rateChartOption;
    }
    if (diskChartKey === 'bytesRate') {
      showEchartOptions = this.bytesRateChartOption;
    }

    if (this.myEchart) {
      this.myEchart.setOption(showEchartOptions, notMerge);
    }
  }

  // 加载网络图表数据
  loadNetworkEchartData (
    data: any[],
    t: (key: string) => string,
    networkChartKey: string,
    notMerge = true
  ) {
    this.chartsData = data;
    const dataTypeKey = ['rxBytes', 'rxBytesRate', 'rxErrors', 'txBytes', 'txBytesRate'];
    const dataType = [
      t('node.nodeDetail.chartOptions.network.rxBytes'),
      t('node.nodeDetail.chartOptions.network.rxBytesRate'),
      t('node.nodeDetail.chartOptions.network.rxErrors'),
      t('node.nodeDetail.chartOptions.network.txBytes'),
      t('node.nodeDetail.chartOptions.network.txBytesRate')
    ];

    const seriesData = dataType.map(type => {
      return {
        ...getDefaultLineConfig(),
        name: type
      };
    });

    this.chartsData.forEach(item => {
      const valus = item.cvsValue.split(',');
      valus.forEach((val: string, idx: number) => {
        if (dataTypeKey[idx].includes('BytesRate')) {
          seriesData[idx].data.push(+val);
        } else if (dataTypeKey[idx].includes('Bytes')) {
          const formattedValue = formatBytesToUnit(parseFloat(val), 'GB', 2);
          const numericValue = typeof formattedValue === 'string' ? parseFloat(formattedValue) : formattedValue;
          seriesData[idx].data.push(numericValue);
        } else {
          seriesData[idx].data.push(+val);
        }
      });
    });

    if (this.chartsData.length) {
      this.networkTableData = dataType.map((name, idx) => {
        const total = seriesData[idx].data.reduce((pre: number, current: number) => {
          return pre + current;
        }, 0);
        let average = (total / (seriesData[idx].data.length || 1)).toFixed(2);
        let high = Math.max(...(seriesData[idx].data)) + '';
        let low = Math.min(...(seriesData[idx].data)) + '';
        let latest = seriesData[idx].data[seriesData[idx].data.length - 1] + '';

        if (dataTypeKey[idx].includes('BytesRate')) {
          average += 'MB/s';
          high += 'MB/s';
          low += 'MB/s';
          latest += 'MB/s';
        } else if (dataTypeKey[idx].includes('Bytes')) {
          average += ' GB';
          high += ' GB';
          low += ' GB';
          latest += ' GB';
        } else {
          average += ' packets';
          high += ' packets';
          low += ' packets';
          latest += ' packets';
        }

        const unit = dataTypeKey[idx].includes('BytesRate') ? 'MB/s' : dataTypeKey[idx].includes('Bytes') ? 'GB' : 'packets';
        return {
          name,
          unit,
          average: average,
          high: high,
          low: low,
          latest: latest
        };
      });
    } else {
      this.networkTableData = [];
    }

    if (networkChartKey === 'network') {
      this.tableData.value = this.networkTableData.filter(i => i.unit === 'MB/s');
    } else if (networkChartKey === 'packet') {
      this.tableData.value = this.networkTableData.filter(i => i.unit === 'packets');
    } else {
      this.tableData.value = this.networkTableData.filter(i => i.unit === 'GB');
    }

    const networkSeriesData = seriesData.splice(1, 1).concat(seriesData.splice(3, 1));
    const networkDataTypeKey = dataType.splice(1, 1).concat(dataType.splice(3, 1));
    const networkLengend = { ...defaultLegend, data: networkDataTypeKey };

    this.networkChartOption = {
      ...echartsOpt,
      legend: networkLengend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: networkSeriesData.every(series => !series.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : networkSeriesData
    };

    const packetSeriesData = seriesData.splice(1, 1);
    const packetDataTypeKey = dataType.splice(1, 1);
    const packetLegend = { ...defaultLegend, data: packetDataTypeKey };

    this.packetChartOption = {
      ...echartsOpt,
      legend: packetLegend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: packetSeriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : packetSeriesData
    };

    const legend = { ...defaultLegend, data: dataType };

    this.bytesChartOption = {
      ...echartsOpt,
      legend: legend,
      xAxis: [
        {
          data: this.chartsData.map(i => i.timestamp)
        }
      ],
      series: seriesData.every(serries => !serries.data.length) ? [{ ...getDefaultLineConfig(), data: [50] }] : seriesData
    };

    let showChartOption;
    if (networkChartKey === 'network') {
      showChartOption = this.networkChartOption;
    }
    if (networkChartKey === 'packet') {
      showChartOption = this.packetChartOption;
    }
    if (networkChartKey === 'bytes') {
      showChartOption = this.bytesChartOption;
    }

    if (this.myEchart) {
      this.myEchart.setOption(showChartOption, notMerge);
    }
  }

  // 根据图表类型更新显示
  updateChartDisplay (
    activeTab: string,
    showMemoryPercentChart: boolean,
    diskChartKey: string,
    networkChartKey: string
  ) {
    if (activeTab === 'memory' && (this.memoryPercentEchartOption || this.memoryEchartOption)) {
      if (this.myEchart) {
        this.myEchart.setOption(
          showMemoryPercentChart ? this.memoryPercentEchartOption : this.memoryEchartOption,
          true
        );
      }
      if (showMemoryPercentChart) {
        this.tableData.value = this.memoryTableData.filter(i => i.unit === '%');
      } else {
        this.tableData.value = this.memoryTableData.filter(i => i.unit !== '%');
      }
    }

    if (activeTab === 'disk') {
      let showEchartOptions;
      if (diskChartKey === 'disk') {
        showEchartOptions = this.diskChartOption;
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'GB');
      }
      if (diskChartKey === 'percent') {
        showEchartOptions = this.percentChartOption;
        this.tableData.value = this.diskTableData.filter(i => i.unit === '%');
      }
      if (diskChartKey === 'rate') {
        showEchartOptions = this.rateChartOption;
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'IO/s');
      }
      if (diskChartKey === 'bytesRate') {
        showEchartOptions = this.bytesRateChartOption;
        this.tableData.value = this.diskTableData.filter(i => i.unit === 'MB/s');
      }
      if (showEchartOptions && this.myEchart) {
        this.myEchart.setOption(showEchartOptions, true);
      }
    }

    if (activeTab === 'network') {
      let showChartOption;
      if (networkChartKey === 'network') {
        showChartOption = this.networkChartOption;
        this.tableData.value = this.networkTableData.filter(i => i.unit === 'MB/s');
      }
      if (networkChartKey === 'packet') {
        showChartOption = this.packetChartOption;
        this.tableData.value = this.networkTableData.filter(i => i.unit === 'packets');
      }
      if (networkChartKey === 'bytes') {
        showChartOption = this.bytesChartOption;
        this.tableData.value = this.networkTableData.filter(i => i.unit === 'GB');
      }
      if (showChartOption && this.myEchart) {
        this.myEchart.setOption(showChartOption, true);
      }
    }
  }
}

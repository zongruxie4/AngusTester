export const contentTreeData = [
  {
    title: '执行信息',
    key: 'exec',
    children: [
      {
        title: '执行基本信息',
        key: 'basic'
      },
      {
        title: '执行结果',
        key: 'execResult'
      }
    ]
  },
  {
    title: '汇总结果',
    key: 'total',
    children: [
      {
        title: '名称',
        key: 'name'
      },
      {
        title: '采样数',
        key: 'sampling'
      },
      {
        title: '事务数',
        key: 'qps'
      },
      {
        title: '最小/最大/平均',
        key: 'minmax'
      },
      {
        title: '每秒事务数',
        key: 'qps/s'
      },
      {
        title: '错误率',
        key: 'errRate0'
      },
      {
        title: 'P50/P75/P90/P99/P999',
        key: 'percent'
      },
      {
        title: '下载/秒',
        key: 'download/s0'
      },
      {
        title: '上传/秒',
        key: 'upload/s0'
      }
    ]
  },
  {
    title: '采样指标时序图',
    key: 'indicatorChart',
    children: [
      {
        title: '吞吐量',
        key: 'tps'
      },
      {
        title: '并发数',
        key: 'thread0'
      },
      {
        title: '响应时间',
        key: 'response'
      },
      {
        title: '错误',
        key: 'error0'
      },
      {
        title: '状态码（只有HTTP协议才有）',
        key: 'statusCode'
      },
      {
        title: '节点资源',
        key: 'node',
        tips: '有应用节点时展示应用节点资源数据、没有时展示执行节点资源数据，多个节点时只展示第一个节点数据。'
      },
      {
        title: '叠加分析',
        key: 'superposition',
        children: [
          {
            title: '吞吐量（QPS/TPS）',
            key: 'QPS/TPS',
            children: [
              // {
              //   title: '每秒查询数',
              //   key: 'TPS/s',
              // },
              {
                title: '每秒事务数',
                key: 'QPS/s'
              }
              // {
              //   title: '下载/秒',
              //   key: 'download/s'
              // },
              // {
              //   title: '上传/秒',
              //   key: 'upload/s'
              // },
            ]
          },
          {
            title: '并发数（VU）',
            key: 'vu',
            children: [
              {
                title: '线程数',
                key: 'thread'
              }
              // {
              //   title: '最大线程数',
              //   key: 'maxthread',
              // },
              // {
              //   title: '活跃线程数',
              //   key: 'activethread'
              // }
            ]
          },
          {
            title: '响应时间（RT、毫秒）',
            key: 'rt',
            children: [
              {
                title: '平均',
                key: 'average'
              }
              // {
              //   title: '最小',
              //   key: 'min',
              // },
              // {
              //   title: '最大',
              //   key: 'max'
              // },
              // {
              //   title: 'P50',
              //   key: 'P50'
              // },
              // {
              //   title: 'P90',
              //   key: 'P90'
              // },
              // {
              //   title: 'P95',
              //   key: 'P95'
              // },
              // {
              //   title: 'P99',
              //   key: 'P99'
              // },
              // {
              //   title: 'P999',
              //   key: 'P999'
              // },
            ]
          },
          {
            title: '错误（ERROR）',
            key: 'error',
            children: [
              // {
              //   title: '错误数',
              //   key: 'errNum',
              // },
              {
                title: '错误率',
                key: 'errRate'
              }
            ]
          }
        ]
      }
    ]
  },
  {
    title: '其他信息',
    key: 'other',
    children: [
      {
        title: '执行调度日志',
        key: 'dispatch',
        tips: '多个节点时只展示第一个执行节点日志。'
      },
      {
        title: '执行采样日志',
        key: 'sampling',
        tips: '多个节点时只展示第一个执行节点日志，最多支持10000行日志信息。'
      }
    ]
  }
];

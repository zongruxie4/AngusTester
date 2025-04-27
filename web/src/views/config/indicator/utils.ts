// 空性能指标
export const getDefaultPerfData = () => {
  return {
    art: '0',
    threads: '0',
    errorRate: '0',
    tps: '0',
    percentile: 'ALL',
    targetId: '0',
    duration: '0s',
    rampUpInterval: '0min'
  };
};

// 空稳定性指标
export const getDefaultStabilityData = () => {
  return {
    threads: '0',
    cpu: '0',
    disk: '0',
    duration: '0s',
    errorRate: '0',
    memory: '0',
    network: '0',
    targetId: '0',
    percentile: 'ALL',
    art: '',
    tps: '0'
  };
};

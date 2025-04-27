interface ITarget {
  id: string | undefined,
  timestap: number | undefined
}

const target: ITarget = {
  id: undefined,
  timestap: undefined
};

interface IObserver {
  id: string | undefined,
  timestap: number | undefined,
  data: PerformanceEntry
}

const observer: IObserver = {
  id: undefined,
  timestap: undefined,
  data: {} as PerformanceEntry
};

const bootstrap = (): void => {
  // Catch errors since some browsers throw when using the new `type` option.
  // https://bugs.webkit.org/show_bug.cgi?id=209216
  try {
    // Create the perf observer.
    const po = new PerformanceObserver((list) => {
      const { id, timestap } = target;
      for (const entry of list.getEntries()) {
        const { initiatorType, name } = entry as any;
        if (initiatorType === 'xmlhttprequest' && name === id) {
          observer.id = id;
          observer.timestap = timestap;
          observer.data = entry;
        }
      }
    });
    // Start listening for `resource` entries to be dispatched.
    po.observe({ type: 'resource', buffered: true });
  } catch (e) {
    // Do nothing if the browser doesn't support this API.
  }
};

const setTarget = (_target: ITarget): void => {
  const { id, timestap } = _target;
  target.id = id;
  target.timestap = timestap;
};

const getObserver = async (_target: ITarget): Promise<PerformanceEntry> => {
  const { id, timestap } = _target;
  if (!id || !timestap) {
    return {} as PerformanceEntry;
  }

  // 已经加载完成
  if (observer[id] && observer[timestap]) {
    return observer.data;
  }

  // 没有加载完成，定时刷新
  const temp = new Promise((resolve) => {
    const timer = setInterval(() => {
      // 已经加载完成
      if (observer.id === id && observer.timestap === timestap) {
        clearInterval(timer);
        resolve(observer.data);
      }
    }, 16.66);
  });

  return temp.then((res: any) => {
    return res;
  });
};

export default { bootstrap, getObserver, setTarget };

export default {
  namespaced: true,
  state: {
    focusNofify: 0,
    collectNofify: 0,
    projectNo: 0,
    serviceNo: 0,
    activeDrawer: 0,
    activeDrawerId: ''
  },
  mutations: {
    updateFocusNotify (state: { focesNotify: number; }):void {
      state.focesNotify++;
    },
    updateCollectNofify (state: { collectNofify: number; }):void {
      state.collectNofify++;
    },
    openDrawer (state: { activeDrawer: number; activeDrawerId: string; }, payload:string):void {
      state.activeDrawer++;
      state.activeDrawerId = payload;
    }
  },
  actions: {}
};

export default {
  namespaced: true,
  state: {
    notify: 0
  },
  mutations: {
    updateList (state: { notify: number; }):void {
      state.notify++;
    }
  },
  actions: {}
};

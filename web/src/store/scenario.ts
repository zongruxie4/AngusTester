export default {
  namespaced: true,
  state: {
    addScript: {
      count: 0,
      dir: { id: '' },
      type: ''
    } // 主页添加脚本，通知左侧菜单添加脚本
  },
  mutations: {
    homeAddScript (state, payload):void {
      state.addScript.count++;
      state.addScript.dir.id = payload.folderId;
      state.addScript.type = payload.scriptType;
    }
  },
  actions: {}
};

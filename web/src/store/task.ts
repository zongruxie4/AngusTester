import { task } from 'src/api/tester';

export default {
  namespaced: true,
  state: {
    globalTaskInfo: undefined,
    apiNotify: 0,
    delTaskId: undefined
  },
  mutations: {
    setTaskInfo (state:{globalTaskInfo}, payload):void {
      state.globalTaskInfo = payload;
    },
    updateApiTask (state):void {
      state.apiNotify++;
    },
    setDelTaskId (state:{delTaskId}, payload):void {
      state.delTaskId = payload;
    }
  },
  actions: {
    async loadTaskInfo ({ commit }, id:string):Promise<void> {
      const [error, { data = {} }] = await task.getTaskDetail(id);
      if (error) {
        return;
      }

      commit('setTaskInfo', data);
    }
  }
};

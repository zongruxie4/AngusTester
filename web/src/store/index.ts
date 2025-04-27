import { createStore } from 'vuex';
import apiStore from './api';
import apiSecurityStore from './apiSecurity';
import apiSyncStore from './apiSync';
import apiServerUrlStore from './apiServerUrl';
import taskStore from './task';
import scenarioStore from './scenario';
import guideStore from './guide';

export default createStore({
  state: {
    statusCode: 200,
    activeTab: {},
    closeTabList: [],
    newPaneInfo: {},
    maxPageSize: 2000
  },
  mutations: {
    setStatusCode (state, payload) {
      state.statusCode = payload;
    }
  },
  actions: {
  },
  modules: {
    apiStore,
    apiSecurityStore,
    apiSyncStore,
    apiServerUrlStore,
    taskStore,
    scenarioStore,
    guideStore
  }
});

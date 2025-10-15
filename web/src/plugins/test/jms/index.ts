import type { App } from 'vue';

import 'tailwindcss/components.css';
import 'tailwindcss/utilities.css';

import Core from './core/index.vue';
import DebugResult from './core/DebugResult/index.vue';
import DebugLog from '@/plugins/test/core/DebugLog/index.vue';

Core.install = (app:App) => {
  app.component('VVXCHttp', Core);
  return app;
};

DebugResult.install = (app:App) => {
  app.component('VVXCHttpDebugResult', Core);
  return app;
};

DebugLog.install = (app:App) => {
  app.component('VVXCHttpDebugLog', Core);
  return app;
};

export { DebugResult, DebugLog };
export default Core;

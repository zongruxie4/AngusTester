import { createRouter, createWebHistory } from 'vue-router';
import { guard, appContext } from '@xcan-angus/infra';

import routes from './routes';
import store from '@/store';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

const setCode = (code: number) => {
  store.commit('setStatusCode', code);
};

const startupGuard = (): void => {
  // debugMode is true, turn on debug mode, do not check routing permissions
  guard.navigationGuard(router, appContext.getAccessAppFuncTree() || [], setCode, true); // TODO navigationGuard appContext#menuList
};

export { startupGuard };
export default router;

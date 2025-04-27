import { createRouter, createWebHistory } from 'vue-router';
import { guard, app } from '@xcan-angus/tools';

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
  guard.navigationGuard(router, app.menuList, setCode, true);// debugMode为true，开启调试模式，不校验路由权限
};

export { startupGuard };
export default router;

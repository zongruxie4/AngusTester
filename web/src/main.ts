import { createApp, defineAsyncComponent } from 'vue';
import { createI18n } from 'vue-i18n';
import { app, preference, http } from '@xcan-angus/tools';

import router, { startupGuard } from '@/router';
import store from '@/store';

import '@xcan-angus/vue-ui/style.css';
import 'angus-design/style.css';
import 'tailwindcss/base.css';
import 'tailwindcss/components.css';
import 'tailwindcss/utilities.css';
import '@xcan-angus/frappe-gantt/style.css';

const bootstrap = async () => {
  await app.check();
  await http.create();

  const path = window.location.pathname;
  const sharePaths = ['/share/file', '/apis/share'];
  if (sharePaths.includes(path)) {
    const locale = 'zh_CN';
    const messages = (await import(`./locales/${locale}/index.js`)).default;
    const i18n = createI18n({
      locale,
      legacy: false,
      messages: {
        [locale]: messages
      }
    });

    const App = defineAsyncComponent(() => import('./AppShare.vue'));
    preference.initialize({ themeCode: 'gray' }).then(() => {
      createApp(App)
        .use(router)
        .use(store)
        .use(i18n)
        .mount('#app');
    });

    return;
  }

  app.initialize({ code: 'at' }).then((res) => {
    preference.initialize(res.preference).then(async () => {
      startupGuard();
      const locale = 'zh_CN';
      const messages = (await import(`./locales/${locale}/index.js`)).default;
      const i18n = createI18n({
        locale,
        legacy: false,
        messages: {
          [locale]: messages
        }
      });

      const App = defineAsyncComponent(() => import('./App.vue'));
      createApp(App)
        .use(router)
        .use(store)
        .use(i18n)
        .mount('#app');
    });
  });
};

bootstrap();

import { createApp, defineAsyncComponent } from 'vue';
// import { createI18n } from 'vue-i18n';
import { app, preference, http, cookie } from '@xcan-angus/tools';
import { I18n } from '@xcan-angus/infra';

import router, { startupGuard } from '@/router';
import store from '@/store';

import '@xcan-angus/vue-ui/style.css';

import 'tailwindcss/base.css';
import 'tailwindcss/components.css';
import 'tailwindcss/utilities.css';
import '@xcan-angus/frappe-gantt/style.css';

const getDefaultLanguage = () => {
  let navigatorLang = navigator.language;
  if (navigatorLang.startsWith('zh')) {
    navigatorLang = 'zh_CN'
  } else if (navigatorLang.startsWith('en')){
    navigatorLang = 'en'
  }

  return cookie.get('localeCookie') || navigatorLang
};

const bootstrap = async () => {
  await app.check();
  await http.create();

  const path = window.location.pathname;
  const sharePaths = ['/share/file', '/apis/share'];
  if (sharePaths.includes(path)) {
    const locale = getDefaultLanguage();
    const messages = (await import(`./locales/${locale}/index.js`)).default;
    const i18n = I18n.setupI18n({
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

  app.initialize({ code: 'tester' }).then((res) => {
    preference.initialize(res.preference).then(async () => {
      startupGuard();
      const locale = getDefaultLanguage();
      const messages = (await import(`./locales/${locale}/index.js`)).default;
      const i18n = I18n.setupI18n({
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

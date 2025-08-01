import { createApp, defineAsyncComponent } from 'vue';
import { i18n as I18n, app, http, cookieUtils } from '@xcan-angus/infra';

import router, { startupGuard } from '@/router';
import store from '@/store';

import '@xcan-angus/vue-ui/style.css';

import 'tailwindcss/base.css';
import 'tailwindcss/components.css';
import 'tailwindcss/utilities.css';
import '@xcan-angus/frappe-gantt/style.css';

import zhEnumCNLocale from '@/enums/locales/zh_CN.json';
import enEnumLocale from '@/enums/locales/en.json';

const bootstrap = async () => {
  await app.initEnvironment();
  await http.create();

  const path = window.location.pathname;
  const sharePaths = ['/share/file', '/apis/share'];
  if (sharePaths.includes(path)) {
    const locale = cookieUtils.getCurrentLanguage();
    const messages = (await import(`./locales/${locale}/index.js`)).default;
    const i18n = I18n.setupI18n({
      locale,
      legacy: false,
      messages: {
        [locale]: messages
      }
    });

    // Merge locale messages
    i18n.global.mergeLocaleMessage(SupportedLanguage.zh_CN, zhEnumCNLocale);
    i18n.global.mergeLocaleMessage(SupportedLanguage.en, enEnumLocale);

    const enumPluginOptions = {
      i18n,
      enumUtils,
      appEnums: enumNamespaceMap
    } as EnumPluginOptions;

    const App = defineAsyncComponent(() => import('./AppShare.vue'));
    createApp(App)
      .use(router)
      .use(store)
      .use(EnumPlugin, enumPluginOptions)
      .use(i18n)
      .mount('#app');
    return;
  }

  app.initAfterAuthentication({ code: 'tester' }).then(async () => {
    startupGuard();
    const locale = cookieUtils.getCurrentLanguage();
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
};

bootstrap();

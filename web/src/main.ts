import { createApp, defineAsyncComponent } from 'vue';
import { createI18n } from 'vue-i18n';
import { AppOrServiceRoute, i18n as I18n, app, http, EnumPlugin, enumUtils } from '@xcan-angus/infra';
import router, { startupGuard } from '@/router';
import store from '@/store';
import { enumNamespaceMap } from '@/enums/enums';
import { localeBundles } from '@/utils/locale';

import 'tailwindcss/base.css';
import 'tailwindcss/components.css';
import 'tailwindcss/utilities.css';
import '@xcan-angus/frappe-gantt/style.css';
import '@xcan-angus/vue-ui/style.css';

// Constants
const SUPPORTED_LOCALES = ['en', 'zh_CN'] as const;
const DEFAULT_LOCALE = 'en';

const bootstrap = async () => {
  await app.initEnvironment();
  await http.create();

  app.initAfterAuthentication({ code: AppOrServiceRoute.tester }).then(async () => {
    await app.initializeDefaultThemeStyle();
    startupGuard();

    // Get current locale
    const locale = I18n.getI18nLanguage();

    // Create messages object
    const messages = {
      en: {
        ...localeBundles.en
      },
      zh_CN: {
        ...localeBundles.zh_CN
      }
    };

    const i18n = createI18n({
      locale: SUPPORTED_LOCALES.includes(locale as any) ? locale : DEFAULT_LOCALE,
      legacy: false,
      messages,
      fallbackLocale: DEFAULT_LOCALE,
      missingWarn: false, // Disable missing key warnings in production
      fallbackWarn: false
    });

    const enumPluginOptions = {
      i18n: i18n,
      enumUtils: enumUtils,
      appEnums: enumNamespaceMap
    };

    const App = defineAsyncComponent(() => import('./App.vue'));
    createApp(App)
      .use(router)
      .use(store)
      .use(EnumPlugin, enumPluginOptions)
      .use(i18n)
      .mount('#app');
  });
};

bootstrap();

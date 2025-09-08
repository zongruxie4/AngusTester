// Enum locales
import zhEnumLocale from '@/enums/locale/zh_CN.json';
import enEnumLocale from '@/enums/locale/en.json';

// Locale files - English
import enCommon from '@/locales/en/common.json';
import enApi from '@/locales/en/api.json';
import enApiShare from '@/locales/en/apiShare.json';
import enData from '@/locales/en/data.json';
import enExecution from '@/locales/en/execution.json';
import enKanban from '@/locales/en/kanban.json';
import enPlugin from '@/locales/en/plugin.json';
import enProject from '@/locales/en/project.json';
import enReport from '@/locales/en/report.json';
import enScenario from '@/locales/en/scenario.json';
import enScript from '@/locales/en/script.json';
import enTask from '@/locales/en/task.json';
import enConfig from '@/locales/en/config.json';
import enFunction from '@/locales/en/function.json';
import enCommonComp from '@/locales/en/commonComp.json';

// Locale files - Chinese
import zhCommon from '@/locales/zh_CN/common.json';
import zhApi from '@/locales/zh_CN/api.json';
import zhApiShare from '@/locales/zh_CN/apiShare.json';
import zhData from '@/locales/zh_CN/data.json';
import zhExecution from '@/locales/zh_CN/execution.json';
import zhKanban from '@/locales/zh_CN/kanban.json';
import zhPlugin from '@/locales/zh_CN/plugin.json';
import zhProject from '@/locales/zh_CN/project.json';
import zhReport from '@/locales/zh_CN/report.json';
import zhScenario from '@/locales/zh_CN/scenario.json';
import zhScript from '@/locales/zh_CN/script.json';
import zhTask from '@/locales/zh_CN/task.json';
import zhConfig from '@/locales/zh_CN/config.json';
import zhFunction from '@/locales/zh_CN/function.json';
import zhCommonComp from '@/locales/zh_CN/commonComp.json';

// Locale message bundles
export const localeBundles = {
  en: {
    ...enEnumLocale,
    ...enCommon,
    ...enApi,
    ...enApiShare,
    ...enData,
    ...enExecution,
    ...enKanban,
    ...enPlugin,
    ...enProject,
    ...enReport,
    ...enScenario,
    ...enScript,
    ...enTask,
    ...enConfig,
    ...enFunction,
    ...enCommonComp
  },
  zh_CN: {
    ...zhEnumLocale,
    ...zhCommon,
    ...zhApi,
    ...zhApiShare,
    ...zhData,
    ...zhExecution,
    ...zhKanban,
    ...zhPlugin,
    ...zhProject,
    ...zhReport,
    ...zhScenario,
    ...zhScript,
    ...zhTask,
    ...zhConfig,
    ...zhFunction,
    ...zhCommonComp
  }
} as const;

import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);

export const navs = [
  { icon: 'icon-baocundaoweiguidang', name: t('service.apis.navs.saveUnarchived'), disabled: false, value: 'saveUnarchived', auth: 'VIEW' },
  { icon: 'icon-fuwuxinxi', name: t('actions.save'), value: 'save', disabled: false, auth: 'MODIFY' },
  { icon: 'icon-zhibiao', name: t('service.apis.navs.indicator'), value: 'performance', disabled: false, auth: 'MODIFY' },
  { icon: 'icon-zhihangceshi', name: t('service.apis.navs.test'), value: 'test', disabled: false, auth: 'VIEW' },
  { icon: 'icon-lishijilu', name: t('service.apis.navs.activity'), value: 'activity', disabled: false, auth: 'VIEW' },
  { icon: 'icon-jiekoudaili', name: t('service.apis.navs.agent'), value: 'agent', auth: 'MODIFY', disabled: false }
];

export interface Menu { name: string; value: string }

export const menus: Menu[] = [
  { name: t('protocol.request'), value: 'request' },
  { name: t('protocol.response'), value: 'response' },
  { name: t('service.apis.menus.time'), value: 'time' },
  { name: t('protocol.cookie'), value: 'cookie' },
  { name: t('common.assertionResult'), value: 'assert' },
  { name: t('service.apis.menus.generateCode'), value: 'generateCode' }
];

export const getStatusColor = (status: number) => {
  const statusStr = String(status);
  if (statusStr.startsWith('4') || statusStr.startsWith('5')) return 'text-status-error';
  return 'text-status-success';
};

export const docInfo = [
  {
    title: t('service.apis.docInfo.pathParams.title'),
    rules: [
      t('service.apis.docInfo.pathParams.rules.primitive'),
      t('service.apis.docInfo.pathParams.rules.array'),
      t('service.apis.docInfo.pathParams.rules.object'),
      t('service.apis.docInfo.pathParams.rules.limit')
    ]
  },
  {
    title: t('service.apis.docInfo.queryFormParams.title'),
    rules: [
      t('service.apis.docInfo.queryFormParams.rules.primitive'),
      t('service.apis.docInfo.queryFormParams.rules.array'),
      t('service.apis.docInfo.queryFormParams.rules.object'),
      t('service.apis.docInfo.queryFormParams.rules.support')
    ]
  },
  {
    title: t('service.apis.docInfo.headerParams.title'),
    rules: [
      t('service.apis.docInfo.headerParams.rules.primitive'),
      t('service.apis.docInfo.headerParams.rules.array'),
      t('service.apis.docInfo.headerParams.rules.object'),
      t('service.apis.docInfo.headerParams.rules.limit')
    ]
  },
  {
    title: t('service.apis.docInfo.cookieParams.title'),
    rules: [
      t('service.apis.docInfo.cookieParams.rules.primitive'),
      t('service.apis.docInfo.cookieParams.rules.array'),
      t('service.apis.docInfo.cookieParams.rules.object'),
      t('service.apis.docInfo.cookieParams.rules.limit')
    ]
  }
];

export const debugTip = [
  t('service.apis.debugTip.componentSync'),
  t('service.apis.debugTip.variableSupport'),
  t('service.apis.debugTip.functionSupport')
];

import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);

export const navs = [
  { icon: 'icon-baocundaoweiguidang', name: t('service.apis.navs.saveUnarchived'), disabled: false, value: 'saveUnarchived', auth: 'VIEW' },
  { icon: 'icon-fuwuxinxi', name: t('actions.save'), value: 'save', disabled: false, auth: 'MODIFY' },
  { icon: 'icon-zhibiao', name: t('service.apis.navs.indicator'), value: 'performance', disabled: false, auth: 'MODIFY' },
  { icon: 'icon-zhihangceshi', name: t('service.apis.navs.test'), value: 'test', disabled: false, auth: 'VIEW' },
  { icon: 'icon-lishijilu', name: t('common.activity'), value: 'activity', disabled: false, auth: 'VIEW' },
  { icon: 'icon-jiekoudaili', name: t('service.apis.navs.agent'), value: 'agent', auth: 'MODIFY', disabled: false }
];

export interface Menu { name: string; value: string }

export const menus: Menu[] = [
  { name: t('protocol.request'), value: 'request' },
  { name: t('protocol.response'), value: 'response' },
  { name: t('service.apis.menus.time'), value: 'time' },
  { name: t('protocol.cookie'), value: 'cookie' },
  { name: t('common.assertionResult'), value: 'assert' },
  { name: t('common.coding'), value: 'generateCode' }
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
      'Primitive id = 5 -> /users/5',
      'Array id = 「3, 4, 5」 -> /users/3,4,5',
      'Object id = {"role": "admin", "firstName": "Alex"} -> /users/role=admin,firstName=Alex',
      '> Only supports primitive types, primitive type arrays, and object types. Other complex type data models are not parsed.'
    ]
  },
  {
    title: t('service.apis.docInfo.queryFormParams.title'),
    rules: [
      'Primitive id = 5 -> id=5',
      'Array id = 「3, 4, 5」 -> id「0」=3&id「1」=4&id「2」=5',
      'Object id = {"role": "admin", "firstName": "Alex"} -> id.role=admin&id.firstName=Alex',
      '> Supports all types, including object and array type nesting.'
    ]
  },
  {
    title: t('service.apis.docInfo.headerParams.title'),
    rules: [
      'Primitive X-MyHeader = 5 ->  X-MyHeader: 5',
      'Array X-MyHeader = 「3, 4, 5」 -> X-MyHeader: 3,4,5',
      'Object X-MyHeader = {"role": "admin", "firstName": "Alex"} -> X-MyHeader: role=admin,firstName=Alex',
      '> Only supports primitive types, primitive type arrays, and object types. Other complex type data models are not parsed.'
    ]
  },
  {
    title: t('service.apis.docInfo.cookieParams.title'),
    rules: [
      'Primitive id = 5 ->  Cookie: id=5',
      'Array id = 「3, 4, 5」 -> Cookie: id=3,4,5',
      'Object id = {"role": "admin", "firstName": "Alex"} -> Cookie: id=role,admin,firstName,Alex',
      '> Only supports primitive types, primitive type arrays, and object types. Other complex type data models are not parsed.'
    ]
  }
];

export const debugTip = [
  t('service.apis.debugTip.componentSync'),
  t('service.apis.debugTip.variableSupport'),
  t('service.apis.debugTip.functionSupport')
];

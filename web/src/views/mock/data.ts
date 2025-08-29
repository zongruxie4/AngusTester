import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || (() => {});
export const textList = [
  {
    icon: 'icon-shoudongchuangjianMockfuwu',
    name: t('mock.introduce.addMockService'),
    description: t('mock.introduce.addMockService_desc')
  },
  {
    icon: 'icon-daoruyiyoufuwujixitongyangli1',
    name: t('mock.introduce.importService'),
    description: t('mock.introduce.importService_desc')
  },
  {
    icon: 'icon-daoruxiangmufuwu',
    name: t('mock.introduce.importFile'),
    description: t('mock.introduce.importFile_desc')
  }
];

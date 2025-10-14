import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { AuthAppFuncTree, WebTagValue } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/menu';

/**
 * Menu item interface for navigation
 */
export interface MenuItem {
  code: 'apis' | 'request' | 'activity' | 'monitor' | 'set' | 'log';
  icon: 'icon-jiekou' | 'icon-lishijilu' | 'icon-jiankong' | 'icon-shezhi1' | 'icon-qingqiushu' | 'icon-zidingyizhibiao1';
  showName: string;
  url: string;
  show: true;
  breadcrumb: [
    { id: string; name: 'Mock'; path: '/apis#mock' },
    { id: string; name: string; path?: string }
  ];
}

/**
 * Composable for managing mock service navigation menu
 * Handles menu configuration and current menu tracking
 */
export function useMockNavigation (id: string) {
  const { t } = useI18n();
  const route = useRoute();

  // Current active menu item
  const currentMenu = ref<MenuItem>();

  /**
   * Menu configuration for mock service pages
   */
  const menuItems: AuthAppFuncTree[] = [
    {
      code: 'apis',
      icon: 'icon-jiekou',
      showName: t('common.api'),
      url: `/mockservice/${id}/apis`,
      show: true,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '1001' },
        { name: t('common.api'), id: '1002' }
      ]
    },
    {
      code: 'request',
      icon: 'icon-qingqiushu',
      showName: t('mock.detail.menu.request'),
      url: `/mockservice/${id}/request`,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '2001' },
        { name: t('mock.detail.menu.request'), id: '2002' }
      ]
    },
    {
      code: 'log',
      icon: 'icon-zidingyizhibiao1',
      showName: t('common.log'),
      url: `/mockservice/${id}/log`,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '2001' },
        { name: t('common.log'), id: '6002' }
      ]
    },
    {
      code: 'activity',
      icon: 'icon-lishijilu',
      showName: t('common.activity'),
      url: `/mockservice/${id}/activity`,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '3001' },
        { name: t('common.activity'), id: '3002' }
      ]
    },
    {
      code: 'monitor',
      icon: 'icon-jiankong',
      showName: t('common.monitor'),
      url: `/mockservice/${id}/monitor`,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '4001' },
        { name: t('common.monitor'), id: '4002' }
      ]
    },
    {
      code: 'setting',
      icon: 'icon-shezhi1',
      showName: t('common.setting'),
      url: `/mockservice/${id}/setting`,
      enabled: true,
      authCtrl: false,
      tags: [{ id: '', name: WebTagValue.DYNAMIC_POSITION }],
      breadcrumb: [
        { name: 'Mock', path: `/apis#${ApiMenuKey.MOCK}`, id: '5001' },
        { name: t('common.setting'), id: '5002' }
      ]
    }
  ];

  /**
   * Update current menu based on route
   */
  const updateCurrentMenu = (fullPath: string) => {
    if (!fullPath) return;

    const code = fullPath.match(/\/mock\/service\/\d+\/(\w+)/)?.[1];
    currentMenu.value = menuItems.find(item => item.code === code);
  };

  // Watch for route changes to update current menu
  onMounted(() => {
    watch(() => route.fullPath, (newValue) => {
      updateCurrentMenu(newValue);
    }, { immediate: true });
  });

  return {
    currentMenu,
    menuItems
  };
}

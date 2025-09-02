import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

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
export function useMockNavigation(id: string) {
  const { t } = useI18n();
  const route = useRoute();

  // Current active menu item
  const currentMenu = ref<MenuItem>();

  /**
   * Menu configuration for mock service pages
   */
  const menuItems: MenuItem[] = [
    {
      code: 'apis',
      icon: 'icon-jiekou',
      showName: t('mock.mockDetail.menu.apis'),
      url: `/mockservice/${id}/apis`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '1001' },
        { name: t('mock.mockDetail.menu.apis'), id: '1002' }
      ]
    },
    {
      code: 'request',
      icon: 'icon-qingqiushu',
      showName: t('mock.mockDetail.menu.request'),
      url: `/mockservice/${id}/request`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '2001' },
        { name: t('mock.mockDetail.menu.request'), id: '2002' }
      ]
    },
    {
      code: 'log',
      icon: 'icon-zidingyizhibiao1',
      showName: t('mock.mockDetail.menu.log'),
      url: `/mockservice/${id}/log`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '2001' },
        { name: t('mock.mockDetail.menu.log'), id: '6002' }
      ]
    },
    {
      code: 'activity',
      icon: 'icon-lishijilu',
      showName: t('mock.mockDetail.menu.activity'),
      url: `/mockservice/${id}/activity`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '3001' },
        { name: t('mock.mockDetail.menu.activity'), id: '3002' }
      ]
    },
    {
      code: 'monitor',
      icon: 'icon-jiankong',
      showName: t('mock.mockDetail.menu.monitor'),
      url: `/mockservice/${id}/monitor`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '4001' },
        { name: t('mock.mockDetail.menu.monitor'), id: '4002' }
      ]
    },
    {
      code: 'set',
      icon: 'icon-shezhi1',
      showName: t('mock.mockDetail.menu.setting'),
      url: `/mockservice/${id}/setting`,
      show: true,
      breadcrumb: [
        { name: 'Mock', path: '/apis#mock', id: '5001' },
        { name: t('mock.mockDetail.menu.setting'), id: '5002' }
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

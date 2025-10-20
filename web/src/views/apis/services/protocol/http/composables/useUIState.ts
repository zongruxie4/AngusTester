import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ApiPermission } from '@/enums/enums';
import { navs, menus } from '@/views/apis/services/protocol/http/index';

/**
 * UI状态管理composable
 * <p>
 * 管理组件的UI状态，包括标签页、抽屉、工具栏等
 * </p>
 */
export function useUIState (props: {
  id?: string;
  valueObj: Record<string, any>;
}) {
  const { t } = useI18n();

  // 标签页状态
  const activeTabKey = ref('debug');
  const activeKey = ref('parameters');
  const currentTab = ref<string>('');

  // 抽屉状态
  const activeDrawerKey = ref();
  const shareVisible = ref(false);

  // 工具栏状态
  const height = ref<number>(34);
  const maxHeight = ref(0);
  const moving = ref(false);

  // 错误状态
  const errorTitle = ref(t('service.apis.errors.requestErrorWithProxy'));

  // 计算属性 - 导航菜单
  const myNavs = computed(() => {
    if (props.valueObj.unarchived || !props.id) {
      const archivedNav = ['performance', 'variable', 'share', 'test', 'case', 'activity', 'apiMock'];
      return navs.filter(nav => !archivedNav.includes(nav.value)).map(i => ({ ...i, key: i.value }));
    }
    const unarchivedNav = ['saveUnarchived'];
    return navs.filter(nav => !unarchivedNav.includes(nav.value)).map(i => {
      if (i.value === 'save') {
        return {
          ...i,
          key: i.value,
          disabled: false, // 这里需要从外部传入权限
          name: t('actions.save')
        };
      }
      return {
        ...i,
        key: i.value,
        disabled: false // 这里需要从外部传入权限
      };
    });
  });

  // 计算属性 - 工具栏菜单
  const toolbarMenus = computed(() => {
    // 这里需要根据是否有WebSocket来决定显示哪些菜单
    return menus;
  });

  /**
   * 处理分享
   * <p>
   * 打开分享对话框
   * </p>
   */
  const handleShare = () => {
    shareVisible.value = true;
  };

  /**
   * 打开响应工具栏
   * <p>
   * 打开响应工具栏，如果未展开或未选择当前标签页，则将默认标签页设置为'response'
   * </p>
   */
  const openToolBar = (toolbarRef: any) => {
    if (!toolbarRef.value.isSpread || !currentTab.value) {
      toolbarRef.value.handleSelected({ value: 'response' });
    }
    if (height.value < 300) {
      height.value = 300;
    }
  };

  /**
   * 处理工具栏菜单变化
   * <p>
   * 当选择不同的工具栏菜单时更新当前标签页
   * </p>
   * @param menuKey - 选择的菜单键
   */
  const toolbarChange = (menuKey: string) => {
    currentTab.value = menuKey;
  };

  /**
   * 设置错误标题
   * <p>
   * 根据是否有WebSocket设置不同的错误标题
   * </p>
   * @param hasWebSocket - 是否有WebSocket连接
   */
  const setErrTitle = (hasWebSocket: boolean) => {
    if (hasWebSocket) {
      errorTitle.value = t('service.apis.errors.requestErrorWithProxy');
    } else {
      errorTitle.value = t('service.apis.errors.requestErrorWithCors');
    }
  };

  /**
   * 关闭抽屉
   * <p>
   * 关闭当前打开的抽屉
   * </p>
   * @param drawerRef - 抽屉引用
   */
  const closeDrawer = (drawerRef: any) => {
    drawerRef.value.close();
  };

  /**
   * 处理窗口大小调整事件
   * <p>
   * 防抖的大小调整处理器，在窗口大小调整时更新主包装器的最大高度
   * </p>
   * @param mainWrapper - 主包装器引用
   */
  const createResizeHandler = (mainWrapper: any) => {
    const resizeHandler = () => {
      if (mainWrapper.value) {
        maxHeight.value = mainWrapper.value.clientHeight;
      }
    };
    return resizeHandler;
  };

  /**
   * 重置UI状态
   * <p>
   * 重置所有UI相关状态到初始值
   * </p>
   */
  const resetUIState = () => {
    activeTabKey.value = 'debug';
    activeKey.value = 'parameters';
    currentTab.value = '';
    activeDrawerKey.value = undefined;
    shareVisible.value = false;
    height.value = 34;
    maxHeight.value = 0;
    moving.value = false;
    errorTitle.value = t('service.apis.errors.requestErrorWithProxy');
  };

  return {
    // 状态
    activeTabKey,
    activeKey,
    currentTab,
    activeDrawerKey,
    shareVisible,
    height,
    maxHeight,
    moving,
    errorTitle,

    // 计算属性
    myNavs,
    toolbarMenus,

    // 方法
    handleShare,
    openToolBar,
    toolbarChange,
    setErrTitle,
    closeDrawer,
    createResizeHandler,
    resetUIState
  };
}

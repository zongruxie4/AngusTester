<script setup lang="ts">
import { computed, inject, onMounted, ref, Ref, watch } from 'vue';
import { Dropdown, Menu, MenuItem, TabPane, Tabs } from 'ant-design-vue';
import { localStore } from '@xcan-angus/tools';

import { Icon, notification } from '@xcan-angus/vue-ui';

export type IPane = {
  name: string;// pane 的tab文案
  _id: string;// pane 的key，唯一标识
  uiKey: string;// pane 的key，唯一标识
  closable?: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
  forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
  icon?: string;// tab文案前面的icon，可选
  active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选
  noCache?: boolean; // 不保存该tabPane，true-不保存，false-保存
}

export type MenuType = 'current' | 'other' | 'right' | 'all';

// 更多详细API请移步：https://www.antdv.com/components/tabs-cn
export interface Props {
  activeKey?: string;// 当前激活 tab 面板的 key
  animated?: boolean | { inkBar: boolean, tabPane: boolean };// 是否使用动画切换 Tabs，在 tabPosition="top" | "bottom" 时有效
  centered?: boolean;// 标签居中展示
  destroyInactiveTabPane?: boolean;// 被隐藏时是否销毁 DOM 结构
  hideAdd?: boolean;// 是否隐藏加号图标，在 type="editable-card" 时有效
  size?: 'large' | 'middle' | 'small';// 大小
  tabBarGutter?: number;// tabs 之间的间隙
  tabBarStyle?: { [key: string]: string };// tab bar 的样式对象
  tabPosition?: 'top' | 'right' | 'bottom' | 'left';// 页签位置，可选值有 top right bottom left
  type?: 'line' | 'card' | 'editable-card';// 页签的基本样式
  storageKey?: string;// 缓存标识，设置会自动开启缓存功能
  userId?: string; // 用户id，设置动态列缓存的索引key，如果支持inject获取用户信息，则不用传递该值
}

const props = withDefaults(defineProps<Props>(), {
  activeKey: undefined,
  animated: false,
  centered: false,
  destroyInactiveTabPane: false,
  hideAdd: false,
  size: 'small',
  tabBarGutter: 0,
  tabBarStyle: undefined,
  tabPosition: 'top',
  type: 'editable-card',
  storageKey: undefined,
  userId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'add'): void;
  (e: 'update:activeKey', value:string): void;
  (e: 'storageKeyChange', value:string): void;
}>();

const userInfo = inject<Ref<{ [key: string]: any }>>('tenantInfo');

const MAX_PANE_LENGTH = 30;

const checkedId = ref<string>();
const dataList = ref<IPane[]>([]);

const add = (callback: (_panes: string[]) => IPane) => {
  if (dataList.value.length >= MAX_PANE_LENGTH) {
    notification.info(`打开Tab页数太多，最大不允许超过 ${MAX_PANE_LENGTH} 个。`);
    return;
  }

  if (typeof callback === 'function') {
    const keys = dataList.value.map(item => item._id);
    const pane = callback(keys);
    const _id = pane?._id;
    if (!_id) {
      return;
    }

    if (!keys.includes(_id)) {
      dataList.value.push({ forceRender: false, closable: true, uiKey: _id, ...pane });
    }

    const uiKey = dataList.value.find(item => item._id === _id)?.uiKey;
    change(uiKey);
  }
};

const change = (_id: string) => {
  checkedId.value = _id;
  store();
  emit('update:activeKey', _id);
};

const getData = (_id?: string): IPane[] => {
  let _data: IPane[] = dataList.value;
  if (_id) {
    _data = dataList.value.filter(item => {
      return _id === item._id;
    });
  }

  return _data.map(item => {
    return { ...item, active: checkedId.value === item._id };
  });
};

const store = () => {
  if (!_storageKey.value) {
    return;
  }

  const panes = getData();
  localStore.set(_storageKey.value, panes);
};

const tabRemove = (type: MenuType, index: number) => {
  let keys: string[] = [];
  if (type === 'all') {
    keys = dataList.value.filter((item) => item.closable).map(item => item._id);
  } else if (type === 'current') {
    keys = dataList.value.filter((item, _index) => _index === index && item.closable).map(item => item._id);
  } else if (type === 'other') {
    keys = dataList.value.filter((item, _index) => _index !== index && item.closable).map(item => item._id);
  } else if (type === 'right') {
    keys = dataList.value.filter((item, _index) => _index > index && item.closable).map(item => item._id);
  }

  remove(keys);
};

const remove = (keys: string[]) => {
  if (keys?.length === 0) {
    checkedId.value = undefined;
    dataList.value = [];
    store();
    return;
  }

  const _checkedId = checkedId.value;
  const prevIndex = dataList.value.findIndex(item => item._id === _checkedId);
  dataList.value = dataList.value.filter(item => !keys.includes(item._id));
  const checkedIndex = dataList.value.findIndex(item => item._id === _checkedId);
  if (checkedIndex === -1) {
    if (prevIndex === -1) {
      checkedId.value = dataList.value[0]?._id;
    } else {
      if (prevIndex <= dataList.value.length - 1) {
        checkedId.value = dataList.value[prevIndex]?._id;
      } else {
        checkedId.value = dataList.value[dataList.value.length - 1]?._id;
      }
    }
  }

  store();
};

// 添加或移除标签
const onEdit = (uiKey: string, action: 'add' | 'remove') => {
  if (action === 'add') {
    emit('add');
    return;
  }

  const _id = dataList.value.find(item => item.uiKey === uiKey)?._id;
  if (_id) {
    remove([_id]);
  }
};

// 更新pane的信息
const update = (data: { _id: string } & Partial<IPane>) => {
  const _id = data?._id;
  const index = dataList.value.findIndex(item => item._id === _id);
  if (!_id || index === -1) {
    return;
  }

  const dataSource = dataList.value[index];
  for (const key in data) {
    dataSource[key] = data[key];
  }
  store();
};

// 全部替换，包括key
const replace = (_id: string, data: IPane) => {
  const index = dataList.value.findIndex(item => item._id === _id);
  if (index > -1) {
    const dataSource = dataList.value[index];
    if (dataSource.uiKey === checkedId.value) {
      checkedId.value = data.uiKey;
    }

    for (const key in data) {
      dataSource[key] = data[key];
    }
    store();
  }
};

const open = (_id: string) => {
  const uiKey = dataList.value.find(item => item._id === _id)?.uiKey;
  checkedId.value = uiKey;
};

const mousedown = (event: MouseEvent, data: IPane) => {
  if (event.button !== 1 || !data.closable) {
    return;
  }

  remove([data._id]);
};

const getStoreData = () => {
  if (!_storageKey.value) {
    return [];
  }

  let list = localStore.get(_storageKey.value) as IPane[];
  if (!Array.isArray(list)) {
    list = [];
  }

  return list.filter((item) => item._id);
};

onMounted(() => {
  watch(() => _storageKey.value, (newValue) => {
    if (!newValue) {
      return;
    }

    dataList.value = getStoreData();
    const activeItem = dataList.value.find((item) => item.active) || dataList.value[0];
    if (activeItem) {
      checkedId.value = activeItem._id;
    }

    emit('storageKeyChange', newValue);
  }, { immediate: true });
});

const userId = computed(() => {
  return props.userId || userInfo?.value?.id;
});

const _storageKey = computed<string | undefined>(() => {
  if (!userId.value || !props.storageKey) {
    return undefined;
  }

  return btoa(userId.value + props.storageKey + 'browserTab');
});

const disabledMap = computed(() => {
  const result: {
    [key: string]: {
      all: boolean;
      current: boolean;
      other: boolean;
      right: boolean;
    }
  } = {};
  const _data = dataList.value;
  for (let i = 0, len = _data.length; i < len; i++) {
    const { closable, _id } = _data[i];
    result[_id] = {
      all: !closable && len === 1,
      current: !closable,
      other: len === 1,
      right: (len - 1) <= i
    };
  }

  return result;
});

defineExpose({ add, update, replace, remove, getData, open });
</script>
<template>
  <Tabs
    v-model:activeKey="checkedId"
    :animated="props.animated"
    :centered="props.centered"
    :destroyInactiveTabPane="props.destroyInactiveTabPane"
    :hideAdd="props.hideAdd"
    :size="props.size"
    :tabBarGutter="props.tabBarGutter"
    :tabBarStyle="props.tabBarStyle"
    :tabPosition="props.tabPosition"
    :type="props.type"
    class="browser-tab-container"
    @edit="onEdit"
    @change="change">
    <TabPane key="hidden-default" tab="hidden-default">
      <slot name="empty"></slot>
    </TabPane>
    <TabPane
      v-for="(item, index) in dataList"
      :key="item.uiKey"
      :closable="item.closable"
      :forceRender="item.forceRender">
      <template #tab>
        <slot name="tab">
          <Dropdown
            class="tab-top-dropdown-outer-container"
            placement="topRight"
            size="small"
            :trigger="['contextmenu']">
            <template #overlay>
              <Menu>
                <MenuItem
                  key="current"
                  :disabled="disabledMap[item._id].current"
                  @click="tabRemove('current', index)">
                  关闭
                </MenuItem>
                <MenuItem
                  key="other"
                  :disabled="disabledMap[item._id].other"
                  @click="tabRemove('other', index)">
                  关闭其它标签页
                </MenuItem>
                <MenuItem
                  key="right"
                  :disabled="disabledMap[item._id].right"
                  @click="tabRemove('right', index)">
                  关闭右侧所有标签页
                </MenuItem>
                <MenuItem
                  key="all"
                  :disabled="disabledMap[item._id].all"
                  @click="tabRemove('all', index)">
                  关闭所有标签页
                </MenuItem>
              </Menu>
            </template>
            <div
              :title="item.name"
              class="w-full h-5.5 leading-5.5 truncate max-w-50"
              @mousedown="mousedown($event, item)">
              <template v-if="item.icon">
                <Icon class="browser-tab-icon" :icon="item.icon" />
              </template>{{ item.name }}
            </div>
          </Dropdown>
        </slot>
      </template>
      <slot v-bind="item" name="default"></slot>
    </TabPane>
  </Tabs>
</template>
<style scoped>
.browser-tab-icon {
  margin-right: 4px;
  transform: translateY(-1.5px);
  font-size: 14px;
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) {
  height: 40px;
  margin: 0;
  padding-top: 4px;
  background-color: rgba(247, 248, 251, 100%);
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding: 0 12px;
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) .ant-tabs-tab:not(.ant-tabs-tab-active) {
  position: relative;
  border-color: transparent;
  background-color: transparent;
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) .ant-tabs-tab:not(.ant-tabs-tab-active)::after {
  content: "";
  display: block;
  position: absolute;
  top: 50%;
  right: -2px;
  width: 1px;
  height: 16px;
  transform: translateY(-50%);
  background-color: rgba(229, 229, 229, 100%);
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) .ant-tabs-nav-add {
  border-color: transparent;
  background-color: transparent;
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav) .ant-tabs-tab-remove {
  margin-right: -2px;
  margin-left: 6px;
}

.ant-tabs.browser-tab-container> :deep(.ant-tabs-nav> .ant-tabs-nav-wrap > .ant-tabs-nav-list > .ant-tabs-tab:first-child) {
  display: none !important;
}
</style>

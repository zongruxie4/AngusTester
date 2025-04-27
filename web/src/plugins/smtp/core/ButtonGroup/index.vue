<script setup lang="ts">
import { computed } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Tooltip, VuexHelper } from '@xcan-angus/vue-ui';

import { ButtonGroupMenuItem } from './PropsType';

export interface Props {
  hideKeys: Set<string>;// 隐藏的按钮keys
}

const props = withDefaults(defineProps<Props>(), {
  hideKeys: () => new Set<string>()
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'click', value:ButtonGroupMenuItem):void;
}>();

const { useMutations, useState } = VuexHelper;
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');

const click = (value:ButtonGroupMenuItem) => {
  emit('click', value);
};

const buttonItems = computed(() => {
  const hideKeys = props.hideKeys;
  return MENUITEMS.filter(item => !hideKeys.has(item.key));
});

const guideStep = (key:string) => {
  updateGuideStep({ visible: true, key });
  click(SAVE_ITEM);
};

const MENUITEMS:readonly ButtonGroupMenuItem[] = [
  { name: '导出脚本', icon: 'icon-daochu', key: 'export' },
  { name: '选择脚本', icon: 'icon-shengchengceshijiaoben', key: 'select' },
  { name: '导入脚本', icon: 'icon-daoru', key: 'import' },
  { name: '代码视图', icon: 'icon-daimashitu', key: 'codeView' },
  { name: '页面视图', icon: 'icon-yemianshitu', key: 'UIView' },
  { name: '权限', icon: 'icon-quanxian1', key: 'authority' },
  { name: '关注', icon: 'icon-yiguanzhu', key: 'followFlag' },
  { name: '取消关注', icon: 'icon-quxiaoguanzhu', key: 'cancelFollowFlag' },
  { name: '收藏', icon: 'icon-yishoucang', key: 'favouriteFlag' },
  { name: '取消收藏', icon: 'icon-quxiaoshoucang', key: 'cancelFavouriteFlag' },
  { name: '刷新', icon: 'icon-shuaxin', key: 'refresh' },
  { name: '创建执行', icon: 'icon-tiaoshi', key: 'test' },
  { name: '调试', icon: 'icon-tiaoshi', key: 'debug' }
];

const SAVE_ITEM = { name: '保存', icon: 'icon-baocun', key: 'save' };
</script>

<template>
  <div class="flex items-center justify-end flex-nowrap space-x-2">
    <template v-for="(item, index) in buttonItems" :key="item.key">
      <div v-if="index > 0" class="h-3 w-0 border-r border-solid border-theme-divider"></div>
      <Button
        type="link"
        size="small"
        @click="click(item)">
        <div class="flex items-center space-x-1">
          <Icon :icon="item.icon" class="text-3.5" />
          <span>{{ item.name }}</span>
        </div>
      </Button>
    </template>
    <Tooltip
      :visible="stepVisible && stepKey === 'openSave'"
      :overlayStyle="{'min-width': '240px'}"
      placement="topRight"
      destroyTooltipOnHide>
      <template #title>
        <div class="p-2 text-3">
          <div class="text-4 text-text-title">{{ stepContent?.title }}</div>
          <div class="mt-2">{{ stepContent?.content }}</div>
          <div class="flex justify-end mt-5">
            <Button
              size="small"
              type="primary"
              @click="guideStep('saveScen')">
              下一步
            </Button>
          </div>
        </div>
      </template>
      <Button
        type="primary"
        size="small"
        @click="click(SAVE_ITEM)">
        <div class="flex items-center space-x-1">
          <Icon icon="icon-baocun" class="text-3.5" />
          <span>保存</span>
        </div>
      </Button>
    </Tooltip>
  </div>
</template>

<style scoped>
.ant-btn-link {
  padding: 0;
  color: var(--content-text-content);
}

.ant-btn-link:hover {
  color: var(--content-special-text-hover);
}
</style>

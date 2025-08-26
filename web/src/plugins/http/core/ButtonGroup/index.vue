<script setup lang="ts">
import { computed } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { ButtonGroupMenuItem } from './PropsType';

export interface Props {
  hideKeys: Set<string>;// 隐藏的按钮keys
}

const props = withDefaults(defineProps<Props>(), {
  hideKeys: () => new Set<string>()
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'click', value:ButtonGroupMenuItem):void;
}>();

const click = (value:ButtonGroupMenuItem) => {
  emit('click', value);
};

const buttonItems = computed(() => {
  const hideKeys = props.hideKeys;
  return MENUITEMS.filter(item => !hideKeys.has(item.key));
});

const MENUITEMS:readonly ButtonGroupMenuItem[] = [
  { name: t('httPlugin.actions.export'), icon: 'icon-daochu', key: 'export' },
  { name: t('httPlugin.actions.select'), icon: 'icon-shengchengceshijiaoben', key: 'select' },
  { name: t('httPlugin.actions.import'), icon: 'icon-daoru', key: 'import' },
  { name: t('httPlugin.actions.codeView'), icon: 'icon-daimashitu', key: 'codeView' },
  { name: t('httPlugin.actions.pageView'), icon: 'icon-yemianshitu', key: 'UIView' },
  { name: t('httPlugin.actions.authority'), icon: 'icon-quanxian1', key: 'authority' },
  { name: t('httPlugin.actions.follow'), icon: 'icon-yiguanzhu', key: 'followFlag' },
  { name: t('httPlugin.actions.cancelFollow'), icon: 'icon-quxiaoguanzhu', key: 'cancelFollowFlag' },
  { name: t('httPlugin.actions.favourite'), icon: 'icon-yishoucang', key: 'favouriteFlag' },
  { name: t('httPlugin.actions.cancelFavourite'), icon: 'icon-quxiaoshoucang', key: 'cancelFavouriteFlag' },
  { name: t('httPlugin.actions.refresh'), icon: 'icon-shuaxin', key: 'refresh' },
  { name: t('httPlugin.actions.createExecution'), icon: 'icon-tiaoshi', key: 'test' },
  { name: t('httPlugin.actions.debug'), icon: 'icon-tiaoshi', key: 'debug' }
];

const SAVE_ITEM = { name: t('httPlugin.actions.save'), icon: 'icon-baocun', key: 'save' };
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
    <Button
      type="primary"
      size="small"
      @click="click(SAVE_ITEM)">
      <div class="flex items-center space-x-1">
        <Icon icon="icon-baocun" class="text-3.5" />
        <span>{{ t('httPlugin.actions.save') }}</span>
      </div>
    </Button>
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

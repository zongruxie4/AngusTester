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
  { name: t('ftpPlugin.buttonGroup.export'), icon: 'icon-daochu', key: 'export' },
  { name: t('ftpPlugin.buttonGroup.select'), icon: 'icon-shengchengceshijiaoben', key: 'select' },
  { name: t('ftpPlugin.buttonGroup.import'), icon: 'icon-daoru', key: 'import' },
  { name: t('ftpPlugin.buttonGroup.codeView'), icon: 'icon-daimashitu', key: 'codeView' },
  { name: t('ftpPlugin.buttonGroup.uiView'), icon: 'icon-yemianshitu', key: 'UIView' },
  { name: t('ftpPlugin.buttonGroup.authority'), icon: 'icon-quanxian1', key: 'authority' },
  { name: t('ftpPlugin.buttonGroup.followFlag'), icon: 'icon-yiguanzhu', key: 'followFlag' },
  { name: t('ftpPlugin.buttonGroup.cancelFollowFlag'), icon: 'icon-quxiaoguanzhu', key: 'cancelFollowFlag' },
  { name: t('ftpPlugin.buttonGroup.favouriteFlag'), icon: 'icon-yishoucang', key: 'favouriteFlag' },
  { name: t('ftpPlugin.buttonGroup.cancelFavouriteFlag'), icon: 'icon-quxiaoshoucang', key: 'cancelFavouriteFlag' },
  { name: t('ftpPlugin.buttonGroup.refresh'), icon: 'icon-shuaxin', key: 'refresh' },
  { name: t('ftpPlugin.buttonGroup.test'), icon: 'icon-tiaoshi', key: 'test' },
  { name: t('ftpPlugin.buttonGroup.debug'), icon: 'icon-tiaoshi', key: 'debug' }
];

const SAVE_ITEM = { name: t('ftpPlugin.buttonGroup.save'), icon: 'icon-baocun', key: 'save' };
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
        <span>{{ t('actions.save') }}</span>
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

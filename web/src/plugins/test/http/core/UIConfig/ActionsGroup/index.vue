<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Switch } from 'ant-design-vue';
import { Arrow, Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();

export interface Props {
  enabled: boolean;
  open: boolean;
  arrowVisible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  enabled: false,
  open: true,
  arrowVisible: true
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:enabled', value: boolean): void;
  (e: 'update:open', value: boolean): void;
  (e: 'openChange', value: boolean): void;
  (e: 'enabledChange', value: boolean): void;
  (e: 'click', value: 'delete' | 'clone'): void;
}>();

const enabledChange = (value: boolean) => {
  emit('update:enabled', value);
  emit('enabledChange', value);
};

const openChange = (value: boolean) => {
  emit('update:open', value);
  emit('openChange', value);
};

const click = (value:'delete'|'clone') => {
  emit('click', value);
};
</script>

<template>
  <div class="flex items-center flex-shrink-0 space-x-3">
    <Switch
      :checked="props.enabled"
      size="small"
      @change="enabledChange" />

    <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.copy')">
      <Icon
        icon="icon-fuzhi"
        class="text-3.5"
        @click="click('clone')" />
    </div>

    <div class="flex items-center cursor-pointer hover:text-text-link-hover" :title="t('actions.delete')">
      <Icon
        icon="icon-qingchu"
        class="text-3.5"
        @click="click('delete')" />
    </div>

    <Arrow
      :open="props.open"
      :class="{ invisible: !props.arrowVisible }"
      @change="openChange" />
  </div>
</template>

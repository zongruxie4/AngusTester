<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();

type Props = {
  editFlag: boolean;
  okButtonDisabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  editFlag: false,
  okButtonDisabled: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'click', value: 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh'): void;
}>();
</script>

<template>
  <div class="flex items-center space-x-2.5">
    <Button
      :disabled="props.okButtonDisabled"
      type="primary"
      size="small"
      class="flex items-center space-x-1"
      @click="emit('click', 'ok')">
      <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
      <span>
        {{ t('actions.save') }}
      </span>
    </Button>

    <template v-if="props.editFlag">
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'delete')">
        <Icon icon="icon-qingchu" class="text-3.5" />
        <span>
          {{ t('actions.delete') }}
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'export')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          {{ t('actions.export') }}
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'clone')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          {{ t('actions.clone') }}
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'copyLink')">
        <Icon icon="icon-fuzhi" class="text-3.5" />
        <span>
          {{ t('actions.copyLink') }}
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'refresh')">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span>
          {{ t('actions.refresh') }}
        </span>
      </Button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

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
  (e: 'click', value:'ok'|'delete'|'export'|'clone'|'copyLink'|'refresh'): void;
}>();
</script>

<template>
  <div class="flex items-center space-x-2.5">
    <Button
      :disabled="props.okButtonDisabled"
      type="primary"
      size="small"
      class="flex items-center space-x-1"
      @click="emit('click','ok')">
      <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
      <span>
        保存
      </span>
    </Button>

    <template v-if="props.editFlag">
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click','delete')">
        <Icon icon="icon-qingchu" class="text-3.5" />
        <span>
          删除
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click', 'export')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          导出
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click','clone')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          克隆
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click','copyLink')">
        <Icon icon="icon-fuzhi" class="text-3.5" />
        <span>
          复制链接
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="emit('click','refresh')">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span>
          刷新
        </span>
      </Button>
    </template>
  </div>
</template>

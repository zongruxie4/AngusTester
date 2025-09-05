<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Input, Validate } from '@xcan-angus/vue-ui';

import { isHtml, isJSON, isXML, isYAML } from '@/utils/dataFormat';

interface Props {
  value: string;
  showAction?:boolean;
  maxlength?:number;// 最大支持字符数
  showCount?:boolean;// 是否显示已输入字符串数量
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  showAction: true,
  maxlength: 20 * 1024 * 1024,
  showCount: false
});

const content = ref('');

const error = ref(false);
const errorMessage = ref('');

const detectType = (value:string) => {
  if (isJSON(value)) {
    return 'json';
  }

  if (isXML(value) || isHtml(value)) {
    return 'html';
  }

  if (isYAML(value)) {
    return 'yaml';
  }

  return 'text';
};

const format = () => {
  const _lang = detectType(content.value);
  if (_lang === 'json') {
    content.value = JSON.stringify(JSON.parse(content.value), null, 2);
  }
};

const clear = () => {
  content.value = '';
};

const autoSize = {
  minRows: 10,
  maxRows: 10
};

const placeholder = computed(() => {
  return `最大支持${props.maxlength}个字符`;
});

onMounted(() => {
  watch(() => props.value, (newValue) => {
    content.value = newValue;
  }, { immediate: true });
});

const isValid = ():boolean => {
  const length = content.value.length;
  error.value = length >= props.maxlength;
  if (error.value) {
    errorMessage.value = `最大支持${props.maxlength}字符，已输入${length}字符`;
  } else {
    errorMessage.value = '';
  }

  return !error.value;
};

const getData = ():string => {
  return content.value;
};

defineExpose({
  format,
  isValid,
  getData
});
</script>

<template>
  <div>
    <div v-if="props.showAction" class="flex items-center justify-between">
      <slot name="leftextra"></slot>
      <div class="flex-1 flex items-center justify-end space-x-2">
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="format">
          <!-- <Icon icon="icon-geshihua" class="mr-0.5 text-3.25" /> -->
          <span>格式化</span>
        </Button>
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="clear">
          <!-- <Icon icon="icon-qingchu" class="mr-0.5 text-3.25" /> -->
          <span>清空</span>
        </Button>
      </div>
    </div>

    <Validate fixed :text="errorMessage">
      <Input
        v-model:value="content"
        type="textarea"
        autocomplete="off"
        :placeholder="placeholder"
        :maxlength="props.maxlength"
        :showCount="props.showCount"
        :autoSize="autoSize"
        :error="error" />
    </Validate>
  </div>
</template>

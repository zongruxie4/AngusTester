<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Composite, HttpMethodText, IconCopy, IconRequired, Input, Select, SelectEnum } from '@xcan-angus/vue-ui';

interface Props {
  method:string;
  options: { label: string; value: string; }[]
  endpoint:string|undefined;
  server?:string;
  readonly?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  method: undefined,
  endpoint: undefined,
  server: undefined,
  options: () => [],
  readonly: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:method', value: string): void;
  (e: 'update:endpoint', value: string): void;
  (e: 'update:server', value: string): void;
}>();

const domain = ref();
const pathname = ref();
const error = ref(false);

const selectChange = (value: string) => {
  emit('update:method', value);
};

const serverChange = (value: string) => {
  domain.value = value;
  emit('update:server', value);
};

const inputChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  pathname.value = value;
  emit('update:endpoint', value);
  error.value = false;
};

const inputBlur = (event: { target: { value: string } }) => {
  const value = event.target.value;
  pathname.value = value;
  emit('update:endpoint', value);
  error.value = !isValidPath(value);
};

const isValidPath = (_pathname: string): boolean => {
  if (!_pathname) {
    return false;
  }

  return true;
};

onMounted(() => {
  watch(() => props.endpoint, (newValue) => {
    error.value = false;
    pathname.value = newValue;
  }, { immediate: true });
});

defineExpose({
  isValid: ():boolean => {
    let errorNum = 0;
    if (!isValidPath(pathname.value)) {
      errorNum++;
    }

    error.value = !!errorNum;
    return !errorNum;
  }
});

const copyText = computed(() => {
  let url = '';
  if (domain.value) {
    url += domain.value;
  }

  if (pathname.value) {
    url += pathname.value;
  }

  return url;
});
</script>
<template>
  <div class="leading-5">
    <div class="mb-0.5">
      <IconRequired />
      <span>方法/路径</span>
    </div>
    <div class="flex items-center">
      <Composite>
        <SelectEnum
          :value="props.method"
          :disabled="props.readonly"
          class="w-25 flex-shrink-0"
          enumKey="HttpMethod"
          placeholder="请求方法"
          @change="selectChange">
          <template #option="record">
            <HttpMethodText :value="record.value" />
          </template>
        </SelectEnum>
        <Select
          v-model:value="domain"
          :options="props.options"
          defaultActiveFirstOption
          class="w-70 flex-shrink-0"
          placeholder="域名"
          @change="serverChange" />
        <Input
          :value="pathname"
          :error="error"
          :maxlength="800"
          :disabled="props.readonly"
          trim
          class="flex-1"
          placeholder="最大支持800个字符"
          @blur="inputBlur"
          @change="inputChange" />
      </Composite>
      <span class="text-theme-sub-content">
        <IconCopy class="flex-shrink-0 ml-1.5" :copyText="copyText" />
      </span>
    </div>
  </div>
</template>
<style scoped>
.error.ant-input-affix-wrapper {
  border-width: 1px;
  border-style: solid;
}

.error-border {
  border-color: rgba(245, 34, 45, 100%);
}

.select-input-container {
  transition: all 200ms linear 0ms;
}

.select-input-container:hover,
.select-input-container:focus,
.select-input-container-focused {
  border-color: #40a9ff;
}
</style>

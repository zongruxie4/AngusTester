<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Hints, Icon, Spin } from '@xcan-angus/vue-ui';
import { variable } from '@/api/tester';

type Props = {
  dataSource: {
    name: string;
    value: string;
    id: string;
    projectId: string;
    extraction: {
      defaultValue: string;
      expression: string;
      failureMessage: string;
      finalValue: string;
      matchItem: string;
      method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
      name: string;
      source: 'FILE' | 'http' | 'HTTP_ASSERTION' | 'HTTP_SAMPLING' | 'JDBC' | 'VALUE';
      value: string;
      fileType: 'CSV' | 'EXCEL' | 'TXT';
      path: string;
      encoding: 'UTF-8' | 'UTF-16' | 'UTF-16BE' | 'UTF-16LE' | 'US-ASCII' | 'ISO-8859-1';
      quoteChar: string;
      escapeChar: string;
      separatorChar: string;
      rowIndex: string;
      columnIndex: string;
      select: string;
      parameterName: string;
      request: {
        url: string;
      };
      datasource: {
        type: string;
        username: string;
        password: string;
        jdbcUrl: string;
      }
    };
  };
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const loading = ref(false);
const content = ref<string>();
const errorMessage = ref<string>();

const refresh = () => {
  if (loading.value) {
    return;
  }

  loadData();
};

const loadData = async () => {
  if (!props.dataSource) {
    return;
  }

  loading.value = true;
  const [error, res] = await variable.previewVariableValue(props.dataSource, { silence: true });
  loading.value = false;
  if (error) {
    errorMessage.value = error.message;
    return;
  }

  content.value = res?.data;
  errorMessage.value = undefined;
};

const reset = () => {
  loading.value = false;
  content.value = undefined;
  errorMessage.value = undefined;
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    reset();

    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between mb-2.5">
      <Hints text="实际变量值在执行采样前生成，并且会执行过程中保持不变。" />
      <Button
        :disabled="loading"
        size="small"
        type="link"
        class="px-0 h-5 leading-5 border-0 text-theme-content text-theme-text-hover"
        @click="refresh">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span class="ml-1">刷新</span>
      </Button>
    </div>

    <div class="content-container rounded border border-solid border-theme-text-box">
      <span v-if="errorMessage" class="text-status-error">{{ errorMessage }}</span>
      <span v-else>{{ content }}</span>
    </div>
  </Spin>
</template>
<style scoped>
.content-container {
  height: 316px;
  padding: 4px 7px;
  overflow: auto;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>

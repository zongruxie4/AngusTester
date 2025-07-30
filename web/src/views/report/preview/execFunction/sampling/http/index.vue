<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';

import { ExecContent, ExecInfo } from './PropsType';
import Collapse from '@/views/report/preview/execFunction/sampling/http/collapse/index.vue';

export interface Props {
  execInfo: ExecInfo;
  execContent: ExecContent[];
  exception: {
    codeName: string;
    messageName: string;
    code: string;
    message: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  execInfo: undefined,
  execContent: undefined,
  exception: undefined
});

const execContentList = ref<ExecContent[]>([]);
const totalAssertionNum = ref(0);
const disabledAssertionNum = ref(0);
const ignoreAssertionNum = ref(0);
const successAssertionNum = ref(0);

onMounted(() => {
  watch(() => props.execContent, (newValue) => {
    if (!newValue?.length) {
      execContentList.value = [];
      return;
    }

    const tempList = JSON.parse(JSON.stringify(newValue || '[]'));
    totalAssertionNum.value = 0;
    disabledAssertionNum.value = 0;
    ignoreAssertionNum.value = 0;
    successAssertionNum.value = 0;
    for (let i = 0, len = tempList.length; i < len; i++) {
      const item = tempList[i];
      const assertions = item.content?.assertions;
      let _successNum = 0;
      let enabledAssertions = [];
      if (assertions) {
        totalAssertionNum.value += assertions.length;
        disabledAssertionNum.value += assertions.filter(item => !item.enabled).length;
        ignoreAssertionNum.value += assertions.filter(item => item?.ignore).length;

        enabledAssertions = assertions.filter(item => item.enabled && !item?.ignore);
        _successNum = enabledAssertions.filter(item => item?.result?.failure === false).length;
        successAssertionNum.value += _successNum;
      }
    }

    execContentList.value = tempList.sort((a, b) => {
      return (+a.iteration) - (+b.iteration);
    });
  }, { immediate: true });
});

const execContentMap = computed(() => {
  return execContentList.value.reduce((prev, cur) => {
    if (!prev[cur.iteration]?.length) {
      prev[cur.iteration] = [cur];
    } else {
      prev[cur.iteration].push(cur);
    }

    return prev;
  }, {});
});

const pipelines = computed(() => {
  const _pipelines = props.execInfo?.task?.pipelines || [];
  const httpNum = _pipelines.filter(item => item.target === 'http').length;
  return _pipelines?.reduce((prev, cur) => {
    const _cur = { ...cur, linkName: cur.name, id: utils.uuid() };
    if (httpNum === 1 && _cur.target === 'http') {
      _cur.linkName = 'Total';
    }
    if (_cur.transactionName) {
      if (!prev[prev.length - 1].children?.length) {
        prev[prev.length - 1].children = [_cur];
      } else {
        prev[prev.length - 1].children.push(_cur);
      }
    } else {
      prev.push(_cur);
    }

    return prev;
  }, []);
});

const iterations = computed(() => {
  return execContentList.value?.reduce((prev, cur) => {
    if (!prev.includes(cur.iteration)) {
      prev.push(cur.iteration);
    }

    return prev;
  }, []) || [];
});
</script>
<template>
  <Collapse
    v-for="item in iterations"
    :key="item"
    :pipelines="pipelines"
    :execContent="execContentMap[item]"
    :iterations="item"
    :ignoreAssertions="props.execInfo?.ignoreAssertions" />
</template>

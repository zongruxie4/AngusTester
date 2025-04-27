<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER, http } from '@xcan-angus/tools';
import { contentTreeData } from './config';

interface Props {
  projectId: string;
  contentSetting: {
    targetId: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

const serviceId = ref();
const apisId = ref();
const fieldNames = {
  label: 'name',
  value: 'id'
};

const apisFieldNames = {
  label: 'summary',
  value: 'id'
};
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

const loadServiceId = async () => {
  const [error, { data }] = await http.get(`${TESTER}/apis/${apisId.value}`);
  if (error) {
    return;
  }
  serviceId.value = data?.serviceId;
};

onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
      apisId.value = newValue.targetId;
      loadServiceId();
    }
  }, {
    immediate: true
  });
});
const valid = ref(false);
const validate = () => {
  valid.value = true;
  if (apisId.value) {
    return true;
  }
  return false;
};

defineExpose({
  validate,
  getData: () => {
    valid.value = false;
    return {
      targetId: apisId.value,
      targetType: 'API'
    };
  }
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>过滤</span>
  </div>
  <div class="flex mt-2 pl-2">
    <div class="inline-flex flex-1 items-center space-x-2">
      <span>
        服务
      </span><Colon />
      <Select
        v-model:value="serviceId"
        placeholder="选择服务"
        :disabled="!props.projectId || props.disabled"
        :action="`${TESTER}/services/search?projectId=${props.projectId}`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="fieldNames"
        class="w-50" />
      <span>
        <IconRequired />
        接口
      </span><Colon />
      <Select
        v-model:value="apisId"
        placeholder="选择接口"
        :showSearch="true"
        :error="valid && !apisId"
        :disabled="!projectId || props.disabled"
        :action="`${TESTER}/apis/search?projectId=${props.projectId}&serviceId=${serviceId}`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="apisFieldNames"
        class="w-50" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>内容</span>
    <Hints text="以下是报告输出内容目录信息。" />
  </div>
  <Tree
    v-model:checkedKeys="checked"
    class="mt-2 text-3"
    disabled
    :treeData="contentTreeData"
    :defaultExpandAll="true"
    :selectable="false"
    :checkable="true">
    <template #title="{title}">
      <span style="color: rgb(82, 90, 101);">{{ title }}</span>
    </template>
  </Tree>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { Icon, Input } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import _ from 'lodash-es';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const names = ref(['']);

const addName = () => {
  names.value.push('');
};

const del = (idx: number) => {
  names.value.splice(idx, 1);
};

defineExpose({
  getData: () => {
    return _.uniq([...names.value].filter(Boolean));
  }
});
</script>
<template>
  <div class="space-y-2">
    <div
      v-for="(_name, idx) in names"
      :key="idx"
      class="flex items-center space-x-2">
      <Input
        v-model:value="names[idx]"
        class="w-60"
        :placeholder="t('service.sidebar.batchModifyApi.paramsNamePlaceholder')" />

      <Button
        size="small"
        @click="del(idx)">
        <Icon icon="icon-qingchu" />
      </Button>

      <Button
        v-show="idx === names.length - 1"
        size="small"
        @click="addName">
        <Icon icon="icon-jia" />
      </Button>
    </div>
  </div>
</template>

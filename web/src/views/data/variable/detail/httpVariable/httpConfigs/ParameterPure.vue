
<script lang="ts" setup>
import { Checkbox } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';

export interface OptionProps {
  name: string;
  enabled: boolean;
  disabled: boolean;
  id: string;
  value: string;
  type: 'string';
  in: ParameterIn;
}

export interface Props {
  value: OptionProps[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});
</script>
<template>
  <div class="space-y-3.5">
    <div
      v-for="item in props.value"
      :key="item.id"
      class="flex items-center pr-10 space-x-2">
      <Checkbox checked />
      <div class="flex items-center flex-1 space-x-2">
        <Input
          style="flex:0 0 calc((100% - 84px)*3.5/10);max-width: 400px;"
          disabled
          :value="item.name" />
        <Input
          :value="item.value"
          class="flex-1"
          disabled />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ScrolllMembers from './scrollMembers.vue';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { AsyncComponent } from '@xcan-angus/vue-ui';

const activeKey = ref('USER');

</script>
<template>
  <div>
    <RadioGroup
      v-model:value="activeKey"
      buttonStyle="solid"
      class="w-70 flex"
      size="small">
      <RadioButton value="USER" :tab="$t('orgNames.user')">{{ $t('orgNames.user') }}</RadioButton>
      <RadioButton value="DEPT" :tab="$t('orgNames.dept')">{{ $t('orgNames.dept') }}</RadioButton>
      <RadioButton value="GROUP" :tab="$t('orgNames.group')">{{ $t('orgNames.group') }}</RadioButton>
    </RadioGroup>
    <AsyncComponent :visible="activeKey === 'USER'">
      <ScrolllMembers v-show="activeKey === 'USER'" activeKey="USER" />
    </AsyncComponent>
    <AsyncComponent :visible="activeKey === 'DEPT'">
      <ScrolllMembers v-show="activeKey === 'DEPT'" activeKey="DEPT" />
    </AsyncComponent>
    <AsyncComponent :visible="activeKey === 'GROUP'">
      <ScrolllMembers v-show="activeKey === 'GROUP'" activeKey="GROUP" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
:deep(.ant-radio-group-small .ant-radio-button-wrapper) {
  @apply flex-1 text-center;
}

:deep(.ant-radio-group-small .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked)) {
  @apply text-gray-text bg-gray-light;
}
</style>

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
      <RadioButton value="USER" :tab="$t('settingApp.user')">{{ $t('settingApp.user') }}</RadioButton>
      <RadioButton value="DEPT" :tab="$t('settingApp.dept')">{{ $t('settingApp.dept') }}</RadioButton>
      <RadioButton value="GROUP" :tab="$t('settingApp.group')">{{ $t('settingApp.group') }}</RadioButton>
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

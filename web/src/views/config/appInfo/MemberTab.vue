<script setup lang="ts">
import { ref } from 'vue';
import { AuthObjectType } from '@xcan-angus/infra';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { AsyncComponent } from '@xcan-angus/vue-ui';

// Import MemberList component
import MemberList from './MemberList.vue';

interface Props {
  appId: string;
}

const props = defineProps<Props>();

/**
 * Active key for the radio group, determining which member type is currently selected
 * Defaults to USER type
 */
const activeKey = ref(AuthObjectType.USER);
</script>
<template>
  <div>
    <!-- Radio group for selecting member type -->
    <RadioGroup
      v-model:value="activeKey"
      buttonStyle="solid"
      class="w-70 flex"
      size="small">
      <RadioButton :value="AuthObjectType.USER" :tab="$t('orgNames.user')">{{ $t('orgNames.user') }}</RadioButton>
      <RadioButton :value="AuthObjectType.DEPT" :tab="$t('orgNames.dept')">{{ $t('orgNames.dept') }}</RadioButton>
      <RadioButton :value="AuthObjectType.GROUP" :tab="$t('orgNames.group')">{{ $t('orgNames.group') }}</RadioButton>
    </RadioGroup>

    <!-- Conditional rendering of MemberList based on active member type -->
    <AsyncComponent :visible="activeKey === AuthObjectType.USER">
      <MemberList
        v-show="activeKey === AuthObjectType.USER"
        :activeKey="AuthObjectType.USER"
        :appId="props.appId" />
    </AsyncComponent>
    <AsyncComponent :visible="activeKey === AuthObjectType.DEPT">
      <MemberList
        v-show="activeKey === AuthObjectType.DEPT"
        :activeKey="AuthObjectType.DEPT"
        :appId="props.appId" />
    </AsyncComponent>
    <AsyncComponent :visible="activeKey === AuthObjectType.GROUP">
      <MemberList
        v-show="activeKey === AuthObjectType.GROUP"
        :activeKey="AuthObjectType.GROUP"
        :appId="props.appId" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
/* Style adjustments for radio button group */
:deep(.ant-radio-group-small .ant-radio-button-wrapper) {
  @apply flex-1 text-center;
}

:deep(.ant-radio-group-small .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked)) {
  @apply text-gray-text bg-gray-light;
}
</style>

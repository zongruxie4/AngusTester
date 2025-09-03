<script setup lang="ts">
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useVersionManagement } from './composables/useVersionManagement';

/**
 * Version management main component
 * Provides tab-based navigation for version list, detail, and edit views
 */

// Component props interface
interface Props {
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Use version management composable for tab and routing logic
const { browserTabRef, List, Detail, storageKey, storageKeyChange } = useVersionManagement(props);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'versionList'">
        <List
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'versionDetails'">
        <Detail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>

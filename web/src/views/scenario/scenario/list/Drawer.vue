<script setup lang="ts">
import { ActivityTimeline, Drawer } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

// Composable for drawer functionality
import { useDrawer } from './composables/useDrawer';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

// Initialize drawer composable
const { menuItems, types } = useDrawer(t);
</script>

<template>
  <Drawer :menuItems="menuItems">
    <template #activity>
      <ActivityTimeline
        :id="props.userInfo?.id"
        :projectId="props.projectId"
        :types="types"
        class="pr-3.5 pt-2 py-3.5" />
    </template>
  </Drawer>
</template>

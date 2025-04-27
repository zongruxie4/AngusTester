<script setup lang="ts">
import { ActivityTimeline, Drawer } from '@xcan-angus/vue-ui';

type Props = {
  projectId: string;
  userInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined
});

const menuItems = [
  {
    icon: 'icon-lishijilu',
    name: '活动',
    key: 'activity'
  }
];

const types = ['SCENARIO'];
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

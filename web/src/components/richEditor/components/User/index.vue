<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { GM } from '@xcan-angus/infra';
import { Image, Popover, Scroll } from '@xcan-angus/vue-ui';

import { UserInfo } from './PropsType';

interface Props {
  top: number,
  left: number,
  visible: boolean,
}
const props = withDefaults(defineProps<Props>(), {
  top: 0,
  left: 0,
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', val: UserInfo),
  (e: 'update:visible', val: boolean),
}>();

const userList = ref<UserInfo[]>([]);
const scrollChange = (_data: UserInfo[]) => {
  userList.value = _data;
  if (!_data.length) {
    emit('update:visible', false);
  }
};

const onClick = (_user: UserInfo) => {
  emit('change', _user);
  emit('update:visible', false);
};

const style = computed(() => {
  return {
    top: props.top + 'px',
    left: props.left + 'px'
  };
});

const appInfo = inject('appInfo') as Ref<Record<string, any>>;
const appId = appInfo?.value?.id;
const action = computed(() => {
  return `${GM}/app/${appId}/auth/user`;
});
const fieldNames = {
  label: 'fullName',
  value: 'id'
};
</script>
<template>
  <Teleport to="body">
    <Popover
      :visible="props.visible"
      placement="rightTop"
      arrowPointAtCenter
      overlayClassName="rich-editor-popover">
      <template #content>
        <Scroll
          style="height: 160px;"
          :action="action"
          :fieldNames="fieldNames"
          :pageSize="6"
          :transition="false"
          class="w-50"
          @change="scrollChange">
          <div
            v-for="item in userList"
            :key="item.id"
            class="flex items-center w-full text-3 leading-3 px-2 pt-1.75 pb-2.5 rounded outline-none border-none cursor-pointer hover:hover:bg-gray-light-a text-theme-content"
            @click="onClick(item)">
            <Image
              type="avatar"
              class="w-5 h-5 rounded-xl"
              :src="item.avatar" />
            <span :title="item.fullName" class="relative top-0.5 ml-1.5 truncate">{{ item.fullName }}</span>
          </div>
        </Scroll>
      </template>
      <div :style="style" class="absolute -z-99 h-3 opacity-0">@</div>
    </Popover>
  </Teleport>
</template>
<style>
.rich-editor-popover .ant-popover-inner-content {
  padding: 8px 4px;
}
</style>

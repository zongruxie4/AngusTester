<script setup lang="ts">
import { computed, inject, onMounted, Ref, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { TabPane, Tabs, TypographyParagraph } from 'ant-design-vue';
import { Icon, Image } from '@xcan-angus/vue-ui';
import { localStore } from '@xcan-angus/infra';
import { ProjectInfo } from '@/layout/types';

type MenuItem = {
  key: string;
  name: string;
  icon: string;
  id?: string;
}

type Props = {
  menuItems: MenuItem[];
}

const props = withDefaults(defineProps<Props>(), {
  menuItems: () => []
});


const emit = defineEmits<{
  (e: 'update:activeKey', value: string): void;
}>();

const defaultImg = new URL('./images/default.png', import.meta.url).href;
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

const router = useRouter();
const route = useRoute();
const localKey = ref();

const menuList = computed(() => {
  return props.menuItems?.map(item => {
    return {
      ...item
    };
  }) || [];
});

const activeKey = ref<string>('');

const click = (data: MenuItem) => {
  routerPush(data.key);
};

const routerPush = (hash: string) => {
  localStore.set(localKey.value, hash);

  const url = new URL(location.href);
  url.hash = '#' + hash;
  const _url = url.href.split(url.origin)[1];
  router.push(_url);
};

onMounted(() => {
  watch([() => route.hash, () => route.path], ([newValue, newpath], [oldRoute, oldPath]) => {
    const hash = newValue?.split('?')?.[0]?.slice(1);
    const defaultKey = menuList.value[0]?.key;
    localKey.value = btoa(route.path);
    if (!hash || (!menuList.value.find(i => i.key === hash) && newpath === oldPath)) {
      // 优先从本地获取
      const localHash = localStore.get(localKey.value);
      if (menuList.value.find(i => i.key === localHash)) {
        routerPush(localHash || defaultKey);
      } else {
        routerPush(localHash);
      }
      return;
    }

    const id = menuList.value.find(item => item.key === hash)?.key;
    if (id) {
      activeKey.value = id;
      localStore.set(localKey.value, hash);
    } else {
      if (newpath !== oldPath) {
        return;
      }
      routerPush(defaultKey);
    }
  }, {
    immediate: true
  });

  watch(() => activeKey.value, (newValue) => {
    if (!newValue) {
      return;
    }
    const key = menuList.value.find(item => item.key === newValue)?.key || '';
    emit('update:activeKey', key);
  }, {
    immediate: true
  });

  watch(() => menuList.value, () => {
    const hash = route.hash?.split('?')?.[0]?.slice(1);
    const key = menuList.value.find(item => item.key === hash)?.key;
    if (!key) {
      activeKey.value = menuList.value[0]?.key;
    }
  });
});

defineExpose({
  changeMenu: (key: string) => {
    const targetMenu = props.menuItems.find(i => i.key === key);
    targetMenu && click(targetMenu);
  }
});
</script>
<template>
  <div class="flex h-full text-3 text-theme-content leading-5">
    <div class="h-full flex-shrink-0 w-16 bg-gray-container pt-2.5">
      <div class="mx-2.5 mb-2.5">
        <div class="flex items-center justify-center mx-auto w-10 h-10 rounded-full overflow-hidden">
          <Image
            :src="projectInfo?.avatar"
            :defaultImg="defaultImg"
            type="image"
            class="w-full" />
        </div>
        <div v-if="false" class="h-8">
          <TypographyParagraph
            :ellipsis="{ rows: 2, expandable: false, tooltip: true }"
            :content="projectInfo?.name"
            class="text-center leading-4" />
        </div>
      </div>
      <div v-if="false" class="h-0.25 bg-border-divider mx-2.5 mt-2 mb-2"></div>
      <div class="space-y-1">
        <div
          v-for="item in menuList"
          :key="item.key"
          :class="{ checked: activeKey === item.key }"
          class="text-center cursor-pointer py-2 menu-item"
          @click="click(item)">
          <Icon :icon="item.icon" class="text-4 mb-1" />
          <div>{{ item.name }}</div>
        </div>
      </div>
    </div>
    <Tabs :activeKey="activeKey" class="h-full flex-1 ghost-tab">
      <TabPane v-for="item in menuList" :key="item.key">
        <slot :name="item.key" v-bind="item"></slot>
      </TabPane>
    </Tabs>
  </div>
</template>
<style scoped>
.bg-gray-container {
  background-color: rgba(247, 248, 251, 100%);
}

.menu-item:hover,
.menu-item.checked {
  background-color: rgba(239, 240, 243, 100%);
}

.ghost-tab>:deep(.ant-tabs-nav) {
  display: none;
}
</style>

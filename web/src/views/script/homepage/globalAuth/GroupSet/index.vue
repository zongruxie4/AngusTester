<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { debounce } from 'throttle-debounce';
import { GM, duration } from '@xcan-angus/infra';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface ListInfo {
  [key: string]: string;
  avatar: string;
}

interface Props {
  type: 'user' | 'dept' | 'group', // 策略id
  loaded: boolean;
  visible: boolean;
  checkedId?: string, // 选中的id
  appId?: string,
}

const props = withDefaults(defineProps<Props>(), {
  type: 'user',
  loaded: false,
  visible: false,
  checkedId: undefined,
  appId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:checkedId', id: string | undefined): void;
  (e: 'update:loaded', value:boolean): void;
}>();

const dataSource = ref<ListInfo[]>([]);
const inputValue = ref<string>();
const activeId = ref<string>();
const notify = ref(0);

const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
const placeholder = ref<string>();
const apiPath = ref<string>();

const params = computed<{ filters?: [{ key: 'fullName' | 'name'; op: 'MATCH_END'; value: string }] }>(() => {
  const value = inputValue.value?.trim();
  if (value) {
    return {
      filters: [{ key: props.type === 'user' ? 'fullName' : 'name', op: 'MATCH_END', value }]
    };
  }

  return {};
});

const scrollChange = (data) => {
  dataSource.value = data;
  if (!activeId.value) {
    const id = data[0]?.id;
    activeId.value = id;
    emit('update:checkedId', id);
  }

  if (!props.loaded) {
    emit('update:loaded', true);
  }
};

onMounted(() => {
  watch([() => props.appId, () => props.type], ([_appId, _type]) => {
    if (!_appId) {
      return;
    }

    switch (_type) {
      case 'dept':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.deptPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.groupPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = t('scriptHome.globalAuth.groupSet.userPlaceholder');
        apiPath.value = `${GM}/app/${_appId}/auth/user`;
    }
  }, { immediate: true });

  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    activeId.value = undefined;
    inputValue.value = undefined;
    notify.value++;
  }, { immediate: true });
});

const inputChange = debounce(duration.search, (event: {target:{value:string}}) => {
  inputValue.value = event.target.value?.trim();
});

const checkedHandler = (id: string) => {
  activeId.value = id;
  emit('update:checkedId', id);
};
</script>
<template>
  <div class="h-full text-3 text-theme-title">
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-2"
      @change="inputChange" />
    <Scroll
      :lineHeight="44"
      :params="params"
      :action="apiPath"
      :notify="notify"
      style="height: calc(100% - 36px);"
      @change="scrollChange">
      <div
        v-for="item in dataSource"
        :key="item[idKey]"
        :class="{ 'active-item': activeId === item[idKey] }"
        class="flex items-center justify-between h-11 py-1.5 px-3 rounded cursor-pointer hover:bg-gray-hover"
        @click.stop="checkedHandler(item[idKey])">
        <div class="flex items-center flex-nowrap">
          <Icon
            v-if="type === 'group'"
            class="mr-3 text-7"
            icon="icon-zu" />
          <Icon
            v-else-if="type === 'dept'"
            class="mr-3 text-7"
            icon="icon-bumen" />
          <Image
            v-else
            class="w-7 h-7 rounded-2xl mr-3"
            type="avatar"
            :src="item.avatar" />
          <span :title="item[nameKey]" class="leading-5 truncate">{{ item[nameKey] }}</span>
        </div>
      </div>
    </Scroll>
  </div>
</template>
<style scoped>
.active-item {
  background-color: rgba(239, 240, 243, 100%);
  color: #1890ff;
}
</style>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { debounce } from 'throttle-debounce';
import { GM, duration } from '@xcan-angus/infra';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';

interface ListInfo {
  [key: string]: string;
  avatar: string;
}

interface Props {
  type: 'user' | 'dept' | 'group', // 策略id
  loaded: boolean;
  checkedId?: string, // 选中的id
  appId?: string,
}

const props = withDefaults(defineProps<Props>(), {
  type: 'user',
  loaded: false,
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

const scrollChange = (data:ListInfo[]) => {
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

const inputChange = debounce(duration.search, (event: {target:{value:string}}) => {
  inputValue.value = event.target.value?.trim();
});

const checkedHandler = (id: string) => {
  activeId.value = id;
  emit('update:checkedId', id);
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
        placeholder.value = '查询部门';
        apiPath.value = `${GM}/app/${_appId}/auth/dept`;
        break;
      case 'group':
        nameKey.value = 'name';
        idKey.value = 'id';
        placeholder.value = '查询组';
        apiPath.value = `${GM}/app/${_appId}/auth/group`;
        break;
      case 'user':
        nameKey.value = 'fullName';
        idKey.value = 'id';
        placeholder.value = '查询用户';
        apiPath.value = `${GM}/app/${_appId}/auth/user`;
    }
  }, { immediate: true });
});
</script>
<template>
  <div class="h-full text-3 text-theme-title">
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-3.5"
      @change="inputChange">
      <template #suffix>
        <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
      </template>
    </Input>

    <Scroll
      :lineHeight="44"
      :params="params"
      :action="apiPath"
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

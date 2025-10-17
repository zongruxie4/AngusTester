<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
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

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  type: 'user',
  checkedId: undefined,
  appId: undefined
});


const emit = defineEmits<{
  (e: 'update:checkedId', id: string | undefined): void;
  (e: 'update:loaded', value:boolean): void;
}>();

const inputValue = ref<string>();
const params = computed<{ filters?: [{ key: 'fullName' | 'name'; op: 'MATCH'; value: string }] }>(() => {
  const value = inputValue.value?.trim();
  if (value) {
    return {
      filters: [{ key: props.type === 'user' ? 'fullName' : 'name', op: 'MATCH', value }]
    };
  }

  return {};
});

const dataSource = ref<ListInfo[]>([]);

const handleData = (val) => {
  dataSource.value = val;
  if (!activeId.value) {
    activeId.value = val[0]?.id;
  }

  if (!props.loaded) {
    emit('update:loaded', true);
  }
};

const nameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
const idKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
const placeholder = ref<string>();
const apiPath = ref<string>();
watch([() => props.appId, () => props.type], ([_appId, _type]) => {
  if (!_appId) {
    return;
  }

  switch (_type) {
    case 'dept':
      nameKey.value = 'name';
      idKey.value = 'id';
      placeholder.value = t('service.authSetting.placeholder.searchDept');
      apiPath.value = `${GM}/app/${_appId}/auth/dept`;
      break;
    case 'group':
      nameKey.value = 'name';
      idKey.value = 'id';
      placeholder.value = t('service.authSetting.placeholder.searchGroup');
      apiPath.value = `${GM}/app/${_appId}/auth/group`;
      break;
    case 'user':
      nameKey.value = 'fullName';
      idKey.value = 'id';
      placeholder.value = t('service.authSetting.placeholder.searchUser');
      apiPath.value = `${GM}/app/${_appId}/auth/user`;
  }
}, { immediate: true });

const changeHandler = debounce(duration.search, (event: {target:{value:string}}) => {
  inputValue.value = event.target.value?.trim();
});

const activeId = ref<string>();
const checkedHandler = (id: string) => {
  activeId.value = id;
};

watch(() => activeId.value, (newValue) => {
  emit('update:checkedId', newValue);
});
</script>
<template>
  <div class="h-full text-3 text-theme-title">
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-2"
      @change="changeHandler" />
    <Scroll
      :lineHeight="44"
      :params="params"
      :action="apiPath"
      style="height: calc(100% - 36px);"
      @change="handleData">
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

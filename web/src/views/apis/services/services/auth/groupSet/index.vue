<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { GM, duration, AuthObjectType, SearchCriteria } from '@xcan-angus/infra';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';

interface Props {
  type: AuthObjectType,
  loaded: boolean;
  checkedId?: string,
  appId?: string,
}

interface ListItemInfo {
  [key: string]: string;
  avatar: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: AuthObjectType.USER,
  checkedId: undefined,
  appId: undefined
});

const emit = defineEmits<{
  (e: 'update:checkedId', id: string | undefined): void;
  (e: 'update:loaded', value: boolean): void;
}>();

const { t } = useI18n();

const searchInputValue = ref<string>();
const listDataSource = ref<ListItemInfo[]>([]);
const selectedItemId = ref<string>();

const searchParams = computed<{ filters?: [SearchCriteria] }>(() => {
  const searchValue = searchInputValue.value?.trim();
  if (searchValue) {
    return {
      filters: [{
        key: props.type === AuthObjectType.USER ? 'fullName' : 'name',
        op: SearchCriteria.OpEnum.Match,
        value: searchValue
      }]
    };
  }
  return {};
});

const displayNameKey = ref<'deptName' | 'groupName' | 'fullName' | 'name'>('name');
const itemIdKey = ref<'deptId' | 'groupId' | 'userId' | 'id'>('id');
const searchPlaceholder = ref<string>();
const apiEndpoint = ref<string>();

/**
 * Handle search input change with debounce
 * Updates search value and triggers data reload
 */
const handleSearchInputChange = debounce(duration.search, (event: any) => {
  searchInputValue.value = event.target.value?.trim();
});

/**
 * Handle item selection
 * Updates the selected item ID and emits change event
 */
const handleItemSelection = (itemId: string) => {
  selectedItemId.value = itemId;
};

/**
 * Handle data loading completion
 * Sets initial selection and notifies parent component
 */
const handleDataLoaded = (loadedData: ListItemInfo[]) => {
  listDataSource.value = loadedData;

  if (!selectedItemId.value && loadedData.length > 0) {
    selectedItemId.value = loadedData[0]?.id;
  }

  if (!props.loaded) {
    emit('update:loaded', true);
  }
};

/**
 * Watch for app ID and type changes to configure API endpoints
 * Sets up appropriate field mappings and API paths based on object type
 */
watch([() => props.appId, () => props.type], ([appId, objectType]) => {
  if (!appId) {
    return;
  }

  switch (objectType) {
    case AuthObjectType.DEPT:
      displayNameKey.value = 'name';
      itemIdKey.value = 'id';
      searchPlaceholder.value = t('service.authSetting.placeholder.searchDept');
      apiEndpoint.value = `${GM}/app/${appId}/auth/dept`;
      break;
    case AuthObjectType.GROUP:
      displayNameKey.value = 'name';
      itemIdKey.value = 'id';
      searchPlaceholder.value = t('service.authSetting.placeholder.searchGroup');
      apiEndpoint.value = `${GM}/app/${appId}/auth/group`;
      break;
    case AuthObjectType.USER:
      displayNameKey.value = 'fullName';
      itemIdKey.value = 'id';
      searchPlaceholder.value = t('service.authSetting.placeholder.searchUser');
      apiEndpoint.value = `${GM}/app/${appId}/auth/user`;
  }
}, { immediate: true });

/**
 * Watch for selected item changes and emit to parent
 * Notifies parent component when selection changes
 */
watch(() => selectedItemId.value, (newSelectedId) => {
  emit('update:checkedId', newSelectedId);
});
</script>
<template>
  <div class="h-full text-3 text-theme-title">
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="searchPlaceholder"
      size="small"
      class="mb-2"
      @change="handleSearchInputChange" />
    <Scroll
      :lineHeight="44"
      :params="searchParams"
      :action="apiEndpoint"
      style="height: calc(100% - 36px);"
      @change="handleDataLoaded">
      <div
        v-for="item in listDataSource"
        :key="item[itemIdKey]"
        :class="{ 'active-item': selectedItemId === item[itemIdKey] }"
        class="flex items-center justify-between h-11 py-1.5 px-3 rounded cursor-pointer hover:bg-gray-hover"
        @click.stop="handleItemSelection(item[itemIdKey])">
        <div class="flex items-center flex-nowrap">
          <Icon
            v-if="type === AuthObjectType.GROUP"
            class="mr-3 text-7"
            icon="icon-zu" />
          <Icon
            v-else-if="type === AuthObjectType.DEPT"
            class="mr-3 text-7"
            icon="icon-bumen" />
          <Image
            v-else
            class="w-7 h-7 rounded-2xl mr-3"
            type="avatar"
            :src="item.avatar" />
          <span :title="item[displayNameKey]" class="leading-5 truncate">{{ item[displayNameKey] }}</span>
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

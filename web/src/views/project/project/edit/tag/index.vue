<script setup lang="ts">
// Vue composition API imports
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// Utilities
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

// Custom UI components
import { AsyncComponent, Hints, Icon, IconRefresh, Input, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';

// Ant Design components
import { Button } from 'ant-design-vue';

// API imports and utils
import { tag } from '@/api/tester';
import { TagProps, TagItem } from '@/views/project/project/types';

// Initialize i18n
const { t } = useI18n();

// Props
const props = withDefaults(defineProps<TagProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  disabled: false
});

// Async component definitions
const CreateModal = defineAsyncComponent(() => import('./add.vue'));

// Constants
const MAX_LENGTH = 20;

// Reactive data
const pageNo = ref(1);
const pageSize = ref(200);
const inputValue = ref<string>();
const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const dataList = ref<TagItem[]>([]);
const total = ref(0);
const editId = ref<string>();
const modalVisible = ref(false);

// Computed properties
const showLoadMore = computed(() => {
  return dataList.value.length < total.value;
});

// Utility functions
const format = (data: { id: string; name: string; hasEditPermission?: boolean }) => {
  let showTitle = '';
  let showName = data.name;
  if (data.name?.length > MAX_LENGTH) {
    showTitle = data.name;
    showName = showName.slice(0, MAX_LENGTH) + '...';
  }

  return {
    ...data,
    showTitle,
    showName,
    hasEditPermission: data.hasEditPermission ?? true
  };
};

const getParams = () => {
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    filters?: { key: 'name'; op: 'MATCH_END'; value: string }[],
  } = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    projectId: props.projectId
  };

  if (inputValue.value) {
    params.filters = [{ key: 'name', op: 'MATCH_END', value: inputValue.value }];
  }

  return params;
};

// Data loading function
const loadData = async (remainder = 0, _params?:{pageNo?:number;pageSize?:number;}) => {
  loading.value = true;
  let params = getParams();
  if (_params) {
    params = { ...params, ..._params };
  }

  const [error, res] = await tag.getTagList(params);
  loading.value = false;
  loaded.value = true;

  if (params.filters?.length) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }

  const list = ((data.list || []) as {id:string;name:string;hasEditPermission?:boolean}[]).slice(remainder);
  const pureList = list.map((item) => {
    return format(item);
  });

  if (params.pageNo > 1) {
    dataList.value.push(...pureList);
  } else {
    dataList.value = pureList;
  }

  total.value = +data.total;
};

// Event handlers
const toCreate = () => {
  modalVisible.value = true;
};

const createOk = (data: { id: string; name: string }) => {
  const _data = format(data);
  dataList.value.unshift(_data);
  total.value += 1;
};

const refresh = () => {
  pageNo.value = 1;
  loadData();
};

const toEdit = (data: TagItem) => {
  if (props.disabled || !data.hasEditPermission) {
    return;
  }
  editId.value = data.id;
};

const cancelEdit = () => {
  editId.value = undefined;
};

const pressEnter = async (id: string, index: number, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  if (value === dataList.value[index].showName) {
    editId.value = undefined;
    return;
  }

  loading.value = true;
  const [error] = await tag.updateTag([{ id, name: value }]);
  loading.value = false;
  if (error) {
    return;
  }

  dataList.value[index] = format({ id: dataList.value[index].id, name: value, hasEditPermission: dataList.value[index].hasEditPermission });
  notification.success(t('project.projectEdit.tag.updateSuccess'));
  editId.value = undefined;
};

const hadleblur = (id: string, index: number, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editId.value === id) {
      pressEnter(id, index, event);
    }
  }, 300);
};

const toDelete = (data: TagItem, index:number) => {
  modal.confirm({
    content: t('project.projectEdit.tag.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await tag.deleteTag([id]);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('project.projectEdit.tag.deleteSuccess'));
      dataList.value.splice(index, 1);
      total.value -= 1;

      // Load additional data if needed after deletion
      if (index < total.value) {
        const params = {
          pageNo: dataList.value.length + 1,
          pageSize: 1
        };
        loadData(0, params);
      }
    }
  });
};

const loadMore = () => {
  // Fix pageNo after add/delete operations
  const remainder = dataList.value.length % pageSize.value;
  const current = Math.floor(dataList.value.length / pageSize.value);
  pageNo.value = current + 1;
  loadData(remainder);
};

const reset = () => {
  pageNo.value = 1;
  inputValue.value = undefined;
  loaded.value = false;
  loading.value = false;
  searchedFlag.value = false;
  dataList.value = [];
  total.value = 0;
  editId.value = undefined;
  modalVisible.value = false;
};

// Debounced search handler
const inputChange = debounce(duration.search, (event: any) => {
  inputValue.value = event.target.value;
  pageNo.value = 1;
  loadData();
});

// Lifecycle hooks
onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }
    loadData();
  }, { immediate: true });
});
</script>
<template>
  <div class="w-full h-full leading-5 text-xs overflow-auto">
    <!-- Tag description section -->
    <div class="space-y-4">
      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
          <span class="text-xs font-semibold text-gray-600">{{ t('project.projectEdit.tag.about') }}</span>
        </div>
        <div class="text-xs text-gray-700 ml-3">{{ t('project.projectEdit.tag.aboutDescription') }}</div>
      </div>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <div class="w-1 h-4 bg-gradient-to-b from-purple-500 to-purple-600 rounded-full"></div>
            <span class="text-xs font-semibold text-gray-600">{{ t('project.projectEdit.tag.addedTags') }}</span>
          </div>
          <Hints
            v-if="!props.disabled"
            :text="t('project.projectEdit.tag.addTagHint')"
            class="text-xs text-gray-500" />
        </div>

        <Spin
          :spinning="loading"
          class="flex flex-col min-h-96">
          <template v-if="loaded">
            <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center py-12">
              <img src="../../../../../assets/images/nodata.png" class="w-32 h-32 mb-4">
              <div v-if="!props.disabled" class="flex items-center text-gray-500 text-xs">
                <span>{{ t('project.projectEdit.tag.noTags') }}</span>
                <Button
                  type="link"
                  size="small"
                  class="text-xs py-0 px-1 text-blue-600"
                  @click="toCreate">
                  {{ t('project.projectEdit.tag.addTag') }}
                </Button>
              </div>
              <div v-else class="text-gray-500 text-xs">
                {{ t('project.projectEdit.tag.noTagsDescription') }}
              </div>
            </div>
            <template v-else>
              <div class="flex items-center justify-between mt-4 mb-4">
                <div class="flex items-center">
                  <Input
                    v-model:value="inputValue"
                    :placeholder="t('project.projectEdit.tag.tagNamePlaceholder')"
                    class="w-64 mr-3"
                    trimAll
                    :allowClear="true"
                    :maxlength="50"
                    @change="inputChange">
                    <template #suffix>
                      <Icon class="text-xs cursor-pointer text-gray-400" icon="icon-sousuo" />
                    </template>
                  </Input>
                </div>

                <div class="flex items-center space-x-3">
                  <Button
                    v-if="!props.disabled"
                    type="primary"
                    size="small"
                    class="flex items-center space-x-1 text-xs"
                    @click="toCreate">
                    <Icon icon="icon-jia" class="text-xs" />
                    <span>{{ t('project.projectEdit.tag.addTag') }}</span>
                  </Button>

                  <IconRefresh @click="refresh">
                    <template #default>
                      <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                        <Icon icon="icon-shuaxin" class="text-xs" />
                        <span>{{ t('project.projectEdit.tag.refresh') }}</span>
                      </div>
                    </template>
                  </IconRefresh>
                </div>
              </div>

              <NoData v-if="dataList.length === 0" class="flex-1 py-12" />

              <div v-else class="flex items-center flex-wrap gap-2 mt-2">
                <div
                  v-for="(item, index) in dataList"
                  :key="item.id"
                  class="">
                  <div v-if="editId === item.id" class="flex items-center">
                    <Input
                      :placeholder="t('project.projectEdit.tag.tagNamePlaceholder')"
                      class="w-40 mr-2 text-xs"
                      trim
                      :value="item.name"
                      :allowClear="false"
                      :maxlength="50"
                      @blur="hadleblur(item.id, index, $event)"
                      @pressEnter="pressEnter(item.id, index, $event)" />
                    <Button
                      type="link"
                      size="small"
                      class="px-0 py-0 text-xs"
                      @click="cancelEdit">
                      {{ t('actions.cancel') }}
                    </Button>
                  </div>

                  <div
                    v-else
                    class="flex items-center bg-gray-50 hover:bg-gray-100 rounded-lg px-3 py-1.5 cursor-pointer transition-colors group"
                    @dblclick="toEdit(item)">
                    <Icon icon="icon-biaoqian1" class="mr-2 text-xs text-gray-500" />
                    <span :title="item.showTitle" class="truncate mr-2 text-xs text-gray-700">{{ item.showName }}</span>
                    <Icon
                      v-if="!props.disabled && item.hasEditPermission"
                      class="text-gray-400 hover:text-red-500 transition-colors opacity-0 group-hover:opacity-100 text-xs"
                      icon="icon-shanchuguanbi"
                      @click="toDelete(item,index)" />
                  </div>
                </div>

                <Button
                  v-if="showLoadMore"
                  size="small"
                  type="link"
                  class="px-0 py-0 h-6 leading-6 text-xs text-blue-600"
                  @click="loadMore">
                  加载更多
                </Button>
              </div>
            </template>
          </template>
        </Spin>
      </div>
    </div>

    <AsyncComponent :visible="modalVisible">
      <CreateModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        @ok="createOk" />
    </AsyncComponent>
  </div>
</template>

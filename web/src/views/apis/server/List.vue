<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button, Popconfirm, Tag, TypographyParagraph } from 'ant-design-vue';
import { Colon, Icon, IconRefresh, Image, NoData, notification, SearchPanel, Spin } from '@xcan-angus/vue-ui';
import { TESTER, SearchCriteria } from '@xcan-angus/infra';
import { services } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ApiMenuKey } from '@/views/apis/menu';
import { cloneDeep } from 'lodash-es';
import { BasicProps } from '@/types/types';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { OpenAPIV3_1 } from '@/types/openapi-types';

const Introduce = defineAsyncComponent(() => import('@/views/apis/server/Introduce.vue'));

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const { t } = useI18n();
const router = useRouter();

// Maximum number of servers allowed to be created
const MAX_NUM = 200;
// Page data has been loaded at least once
const loaded = ref(false);
// Global loading state while fetching or mutating data
const loading = ref(false);
// Whether a search has been performed (affects empty-state rendering)
const searchedFlag = ref(false);

// Strongly typed record describing one server entry bound to a service
type ServerRecord = {
  serviceId: string;
  serviceName: string;
  server: OpenAPIV3_1.ServerObject;
  // optional presentational fields from backend or derived locally
  avatar?: string;
  creator?: string;
  createdDate?: string;
  editLinkUrl?: string;
};
// Full dataset fetched from backend
const dataList = ref<ServerRecord[]>([]);
// Currently displayed dataset (may be filtered by search)
const showList = ref<ServerRecord[]>([]);

// Check whether the server has defined variables (used to style the tag)
const getVariableLength = (data: ServerRecord): boolean => {
  return !!Object.keys(data.server?.variables || {}).length;
};

// Navigate to create server page within current project
const toCreateServer = () => {
  router.push(`/apis#${ApiMenuKey.SERVER}?source=add`);
};

// Push current server definition into API definitions of the service
const toUpdate = async (data: ServerRecord) => {
  const [error] = await services.updateServiceApisServer(data.serviceId, data.server?.extensions?.[API_EXTENSION_KEY.idKey]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.updateSuccess'));
};

// Clone (duplicate) current server to the service with same url/variables/description
const toClone = async (data: ServerRecord) => {
  const params:{
    description?:string;
    url:string;
    variables: OpenAPIV3_1.ServerVariableObject;
  } = {
    description: data.server?.description,
    url: data.server?.url,
    variables: data.server?.variables
  };

  loading.value = true;
  const [error] = await services.putServicesServerUrl(data.serviceId, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cloneSuccess'));
  searchedFlag.value = false;
  await loadData();
};

// Delete current server by id in extensions.x-xc-id
const toDelete = async (data: ServerRecord) => {
  loading.value = true;
  const [error] = await services.delServicesServerUrl(data.serviceId, [data.server?.extensions?.[API_EXTENSION_KEY.idKey]]);
  loading.value = false;
  if (error) {
    return;
  }

  searchedFlag.value = false;
  await loadData();
};

// Update filtered list based on search criteria from SearchPanel
const searchChange = (value: SearchCriteria[]): void => {
  searchedFlag.value = true;
  if (value.length) {
    const description = value.find(item => item.key === 'description');
    const serviceId = value.find(item => item.key === 'serviceId');

    showList.value = dataList.value.filter((item) => {
      let flag = false;
      if (description && serviceId) {
        flag = item.server?.description?.includes(description.value) && item.serviceId === serviceId.value;
      } else if (description) {
        flag = item.server?.description?.includes(description.value);
      } else if (serviceId) {
        flag = item.serviceId === serviceId.value;
      }
      return flag;
    });
    return;
  }
  showList.value = cloneDeep(dataList.value);
};

// Reset search state and reload from backend
const refresh = () => {
  searchedFlag.value = false;
  loadData();
};

// Fetch servers by current project id; also derive edit links for each record
const loadData = async () => {
  const params: {
    projectId: string;
  } = {
    projectId: String(props.projectId || '')
  };

  loading.value = true;
  const [error, res] = await services.getProjectServers(params);
  loaded.value = true;
  loading.value = false;

  if (error) {
    dataList.value = [];
    return;
  }

  const data = res?.data || [];
  dataList.value = data.map((item) => {
    return {
      ...item,
      editLinkUrl: `/apis#${ApiMenuKey.SERVER}?serviceId=${item.serviceId}&serverId=${item.server?.extensions?.[API_EXTENSION_KEY.idKey]}`
    };
  });
  showList.value = cloneDeep(dataList.value);
};

onMounted(() => {
  // Reload list when project changes
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    searchedFlag.value = false;
    loadData();
  }, { immediate: true });

  // External notify trigger: e.g., when other actions affect this list
  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    searchedFlag.value = false;
    loadData();
  }, { immediate: true });
});

// Options for top SearchPanel: supports fuzzy description and service selection
const searchOptions = [
  {
    type: 'input',
    placeholder: t('common.placeholders.searchKeyword'),
    valueKey: 'description',
    allowClear: true,
    maxlength: 150,
    trim: true
  },
  {
    type: 'select',
    placeholder: t('common.placeholders.selectService'),
    valueKey: 'serviceId',
    allowClear: true,
    fieldNames: { label: 'name', value: 'id' },
    action: `${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`
  }
] as any;
</script>
<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce />

    <div class="flex items-center text-3.5 font-semibold mb-1">
      <span class="flex-shrink-0 mr-1">{{ t('apiServer.addedServers') }}</span>
    </div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>{{ t('apiServer.notAddedYet') }}</span>
            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="toCreateServer">
              <span>{{ t('apiServer.actions.addServer') }}</span>
            </Button>
          </div>
        </div>

        <template v-else>
          <div class="flex items-center justify-between mt-2.5 mb-3.5 space-x-5">
            <div class="flex items-center flex-1 space-x-2">
              <SearchPanel
                :options="searchOptions"
                @change="searchChange" />
              <Icon icon="icon-tishi1" class="flex-shrink-0 text-tips text-3.5 cursor-pointer" />
              <div class="flex-shrink-0 break-all whitespace-pre-wrap text-3 font-semibold text-theme-sub-content">
                <span>{{ t('apiServer.messages.addQuotaTip', { add: String(dataList.length), max: String(MAX_NUM) }) }}</span>
              </div>
            </div>

            <div class="flex-shrink-0 flex items-center space-x-3">
              <Button
                :disabled="dataList.length>=MAX_NUM"
                type="primary"
                size="small"
                class="flex items-center flex-shrink-0"
                @click="toCreateServer">
                <div class="flex items-center">
                  <Icon icon="icon-jia" class="text-3.5" />
                  <span class="ml-1">{{ t('apiServer.actions.addServer') }}</span>
                </div>
              </Button>

              <IconRefresh
                @click="refresh">
                <template #default>
                  <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                    <Icon icon="icon-shuaxin" class="text-3.5" />
                    <span class="ml-1">{{ t('actions.refresh') }}</span>
                  </div>
                </template>
              </IconRefresh>
            </div>
          </div>

          <NoData v-if="showList.length === 0" class="flex-1" />

          <div class="flex flex-wrap">
            <div
              v-for="record in showList"
              :key="record.server?.extensions?.[API_EXTENSION_KEY.idKey]"
              class="h-35.5 w-70 mb-2 mr-3 px-3 py-2.5 border rounded-lg border-theme-text-box transition-shadow transition-colors duration-200 hover:shadow-sm hover:bg-gray-50">
              <div class="flex items-center mb-2">
                <div class="flex-shrink-0 flex items-center mr-1.5 flex-1 min-w-0">
                  <span class="font-semibold">ID</span>
                  <Colon />
                  <div class="flex-1 truncate min-w-0 " :title="record.server?.extensions?.[API_EXTENSION_KEY.idKey]">
                    {{ record.server?.extensions?.[API_EXTENSION_KEY.idKey] }}
                  </div>
                </div>
                <Tag
                  class="relative -top-1 mr-0 px-0.5 h-5"
                  :color="getVariableLength(record) ? 'processing' : 'default'">
                  {{ t('common.variables') }}
                </Tag>
              </div>
              <div v-if="!record.server?.description" class="h-9 leading-4.5 mb-2.5 text-theme-sub-content">
                {{ t('common.noDescription') }}
              </div>
              <TypographyParagraph
                v-else
                class="h-9 leading-4.5 mb-2.5 text-theme-sub-content"
                :content="record.server?.description"
                :ellipsis="{ tooltip: record.server?.description, rows: 2 }" />

              <div class="flex items-center mb-2">
                <div class="flex-shrink-0 flex items-center mr-1.5">
                  <span class="font-semibold">{{ t('common.service') }}</span>
                  <Colon />
                </div>
                <div class="flex-1 truncate" :title="record.serviceName">{{ record.serviceName }}</div>
              </div>

              <div v-if="false" class="flex items-center space-x-3 mb-2.5 text-theme-sub-content">
                <Image
                  :src="record.avatar"
                  type="avatar"
                  class="flex-shrink-0 w-6 h-6 rounded-xl" />
                <div class="flex items-center space-x-3">
                  <span>{{ record.creator }}</span>
                  <span>{{ record.createdDate }}</span>
                </div>
              </div>

              <div class="flex items-center justify-end space-x-1.75">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  :title="t('actions.edit')">
                  <RouterLink :to="record.editLinkUrl || ''" class="w-full h-full flex items-center justify-center">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </RouterLink>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  :title="t('apiServer.actions.updateToApi')">
                  <Icon
                    icon="icon-shoudongtuisong"
                    class="text-theme-text-hover cursor-pointer text-3.5"
                    @click="toUpdate(record)" />
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  :title="t('actions.clone')"
                  @click="toClone(record)">
                  <Icon icon="icon-fuzhizujian2" class="text-3.5" />
                </Button>

                <Popconfirm
                  placement="topRight"
                  :title="t('apiServer.messages.deleteConfirmTip')"
                  @confirm="toDelete(record)">
                  <Button
                    type="text"
                    size="small"
                    class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                    :title="t('actions.delete')">
                    <Icon icon="icon-qingchu" class="text-3.5" />
                  </Button>
                </Popconfirm>
              </div>
            </div>
          </div>
        </template>
      </template>
    </Spin>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Arrow, Icon, Input, NoData, notification } from '@xcan-angus/vue-ui';
import { Button, Popover } from 'ant-design-vue';
import { services } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import Main from './main.vue';
import ApiModel from '@/views/apis/services/dataModel/apisModel/index.vue';
import Overview from '@/views/apis/services/dataModel/overview/index.vue';

interface Props {
  id: string;
  pid: string;
  type: string;
  name: string;
  data?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  pid: undefined
});
const { t } = useI18n();
const loading = ref(false);
const selectedOverview = ref(false);
const selectedComp = ref();
const selectedApi = ref();
const selectedApiObj = ref({});
const newCompName = ref();
const apiObj = ref({});
// const compList = ref<{type: string, list: any[], expand: boolean}>([]);

const compType = ['schemas', 'parameters', 'responses', 'requestBodies'];
const compExpandMap = ref<{[key: string]: boolean}>({ paths: true });
const compPopVisibleMap = ref({});

const compListMap = ref<{[key: string]: Array<any>}>({ paths: [] });

const apiPaths = ref<{[key: string]: Array<any>}>({});

const loadProjectOpenapi = async () => {
  const [error, { data }] = await services.getOpenapi(props.id, { gzipCompression: false, format: 'json' });
  if (error) {
    return;
  }
  apiObj.value = JSON.parse(data || '{}');
  apiPaths.value = apiObj.value.paths;
  Object.keys(apiPaths.value).forEach(key => {
    compExpandMap.value[key] = true;
  });
};

const resetCompData = () => {
  compType.forEach(type => {
    compListMap.value[type] = [];
  });
};
compType.forEach(type => {
  compExpandMap.value[type] = true;
  compListMap.value[type] = [];
});
const getProjectCompList = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data }] = await services.getServicesCompList(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  resetCompData();
  (data || []).forEach(item => {
    compType.includes(item.type.value) && compListMap.value[item.type.value].push(item);
  });
};

const toggleOpenValue = (key) => {
  compExpandMap.value[key] = !compExpandMap.value[key];
};

const selectOverview = () => {
  selectedOverview.value = true;
  selectedComp.value = undefined;
  selectedApi.value = undefined;
  selectedApiObj.value = {};
};

const selectComp = (comp) => {
  selectedApi.value = undefined;
  selectedApiObj.value = {};
  selectedComp.value = comp;
  selectedOverview.value = false;
};

const selectApi = (endpoint, method, api) => {
  selectedComp.value = undefined;
  selectedOverview.value = false;
  selectedApi.value = `${endpoint}_${method}`;
  selectedApiObj.value = {
    ...api,
    endpoint,
    method
  };
};

const cancelEditComp = () => {
  selectedComp.value = undefined;
  selectedApi.value = undefined;
  selectedOverview.value = false;
  selectedApiObj.value = {};
};

const submitComp = () => {
  getProjectCompList();
};

const submitOpenapi = async (data) => {
  const { method, endpoint, ...api } = data;
  apiPaths.value[endpoint][method] = { ...api };
  apiObj.value.paths = apiPaths.value;
  const [error] = await services.putOpenapi(props.id, {
    ...apiObj.value
  });
  if (error) {
    return;
  }
  notification.success(t('actions.tips.updateSuccess'));
  loadProjectOpenapi();
};

const submitOverview = async (data) => {
  const target = { ...apiObj.value, ...data };
  const [error] = await services.putOpenapi(props.id, {
    ...target
  });
  if (error) {
    return;
  }
  notification.success(t('actions.tips.updateSuccess'));
  loadProjectOpenapi();
};

const onPopoverVisibleChange = () => {
  newCompName.value = undefined;
};

const copyComp = async (comp, key) => {
  compPopVisibleMap.value[`${key}-${comp.key}`] = false;
  const [error, { data }] = await services.getRefInfo(props.id, comp.ref);
  if (error) {
    notification.error(t('service.dataModel.loadCompFail'));
    return;
  }
  const [error1] = await services.addComponent(props.id, comp.type.value, newCompName.value, data.model);
  if (error1) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
  getProjectCompList();
};

const delComp = async (comp) => {
  const [error] = await services.delComponent(props.id, [comp.ref]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  getProjectCompList();
};

onMounted(() => {
  loadProjectOpenapi();
  getProjectCompList();
});

</script>
<template>
  <div class="flex h-full">
    <div class="w-80 border-r h-full p-2 space-y-1 overflow-y-scroll">
      <div class="px-2 flex justify-between items-center">
        <span class="text-3.5 font-semibold">{{ props.name }}</span>
        <Button
          type="primary"
          size="small"
          @click="selectComp({})">
          {{ t('service.dataModel.addComp') }}
        </Button>
      </div>
      <div
        class="text-3.5 h-8 leading-8 font-medium px-2 flex items-center space-x-2 cursor-pointer hover:bg-bg-hover"
        :class="{'!bg-bg-blue': selectedOverview}"
        @click="selectOverview">
        <Icon icon="icon-shoucang" class="text-4" /> <span>API overview</span>
      </div>
      <div v-for="(value,key) in compListMap" :key="key">
        <div
          class="h-8 leading-8 px-2 cursor-pointer flex items-center justify-between font-medium bg-bg-content"
          @click="toggleOpenValue(key)">
          <div>
            <Icon :icon="compExpandMap[key] ? 'icon-folderzhankai' : 'icon-wenjianjia'" />
            {{ key }} <template v-if="key !== 'paths'">{{ `{${value.length}\}` }}</template>
          </div>
          <Arrow
            v-model:open="compExpandMap[key]" />
        </div>
        <template v-if="key !== 'paths'">
          <div
            v-show="compExpandMap[key]"
            class="">
            <div
              v-for="item in value"
              :key="item.key"
              class="h-8 leading-8 cursor-pointer px-3 hover:bg-bg-hover  flex"
              :class="{'!bg-bg-blue': item.ref === selectedComp?.ref}"
              @click="selectComp(item)">
              <div class="flex-1 truncate" :title="item.key">
                <Icon icon="icon-zujian" />
                {{ item.key }}
              </div>
              <Popover
                v-model:visible="compPopVisibleMap[`${key}-${item.key}`]"
                trigger="click"
                placement="right"
                @openChange="onPopoverVisibleChange">
                <template #title>
                  {{ t('service.dataModel.cloneComp') }}
                </template>
                <template #content>
                  <div>
                    {{ t('service.dataModel.compNameLabel') }}：
                    <Input
                      v-model:value="newCompName"
                      dataType="mixin-en"
                      :maxlength="80" />
                    <Button
                      :disabled="!newCompName"
                      type="primary"
                      size="small"
                      class="mt-1"
                      @click.stop="copyComp(item, key)">
                      {{ t('actions.confirm') }}
                    </Button>
                  </div>
                </template>
                <Button
                  size="small"
                  type="link"
                  @click.stop>
                  <Icon icon="icon-fuzhi" />
                </Button>
              </Popover>
              <Button
                size="small"
                type="link"
                @click.stop="delComp(item)">
                <Icon icon="icon-qingchu" />
              </Button>
            </div>
          </div>
        </template>
        <template v-else>
          <div
            v-for="apis, path in apiPaths"
            v-show="compExpandMap[key]"
            :key="path">
            <div
              class="h-8 leading-8 pl-2 cursor-pointer bg hover:bg-bg-hover truncate"
              @click="toggleOpenValue(path)">
              <Arrow
                v-model:open="compExpandMap[path]" />
              {{ path }}
            </div>
            <div
              v-for="api,method in apis"
              v-show="compExpandMap[path]"
              :key="method"
              :class="{'!bg-bg-blue': `${path}_${method}` === selectedApi}"
              class="h-7 leading-7 pl-6 pr-1 cursor-pointer hover:bg-bg-hover flex justify-between items-center"
              @click="selectApi(path, method, api)">
              <span class="flex-1 truncate min-w-0"> <Icon icon="icon-apimoren" class="text-4 mr-1" />{{ api.summary }}</span>
              <span :class="`text-http-${method}`">{{ method.toUpperCase() }}</span>
            </div>
          </div>
        </template>
      </div>
    </div>
    <div class="flex-1 overflow-y-scroll">
      <Main
        v-if="selectedComp"
        :id="props.id"
        :pid="props.pid"
        :data="selectedComp"
        @cancel="cancelEditComp"
        @ok="submitComp" />
      <ApiModel
        v-else-if="selectedApi"
        :id="props.id"
        :openapiDoc="apiObj"
        :dataSource="selectedApiObj"
        @cancel="cancelEditComp"
        @ok="submitOpenapi">
      </ApiModel>
      <Overview
        v-else-if="selectedOverview"
        :dataSource="apiObj"
        @cancel="cancelEditComp"
        @ok="submitOverview" />
      <NoData v-else />
    </div>
  </div>
</template>

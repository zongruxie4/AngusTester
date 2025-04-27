<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Collapse, CollapsePanel, Tag, TypographyParagraph } from 'ant-design-vue';
import { http, utils, TESTER } from '@xcan-angus/tools';
import { Arrow, Colon, HttpMethodTag, Icon, Spin } from '@xcan-angus/vue-ui';
import YAML from 'yaml';

import { ComponentsKey, OpenApiInfo, PathItemInfo } from './PropsType';
import { getArraySchema, getObjectShcema } from './utils';
import { componentTextMap } from './data';

interface Props {
  serviceId: string;
  mode:'UI'|'code'
}

const props = withDefaults(defineProps<Props>(), {
  serviceId: undefined,
  mode: 'UI'
});

const ApiStatus = defineAsyncComponent(() => import('./ApiStatus.vue'));
const Summary = defineAsyncComponent(() => import('./Summary.vue'));
const RequestParametersTable = defineAsyncComponent(() => import('./RequestParametersTable/index.vue'));
const RequestBodyTable = defineAsyncComponent(() => import('./RequestBodyTable/index.vue'));
const RequestDemo = defineAsyncComponent(() => import('./RequestDemo.vue'));
const PathSecurity = defineAsyncComponent(() => import('./PathSecurity.vue'));
const Response = defineAsyncComponent(() => import('./Response/index.vue'));
const ComponentSchemaTable = defineAsyncComponent(() => import('./ComponentSchemaTable/index.vue'));
const ComponentParameterTable = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/oas/ComponentParameterTable/index.vue'));
const ComponentRequestBodyTable = defineAsyncComponent(() => import('./ComponentRequestBodyTable/index.vue'));
const ComponentExample = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/oas/ComponentExample/index.vue'));
const ComponentSecurity = defineAsyncComponent(() => import('./ComponentSecurity/index.vue'));
const ServerVariables = defineAsyncComponent(() => import('./ServerVariables/index.vue'));
const ComponentTag = defineAsyncComponent(() => import('./ComponentTag.vue'));
const GlobalSecurity = defineAsyncComponent(() => import('./GlobalSecurity/index.vue'));
const CodeView = defineAsyncComponent(() => import('./CodeView.vue'));

const loading = ref(false);
const openapiMetaData = ref<OpenApiInfo>();
const openapiYamlData = ref<string>('');
const pathTags = ref<OpenApiInfo['tags']>([]);
const tags = ref<OpenApiInfo['tags']>([]);
const docItemElementMap = ref<{ [key: string]: PathItemInfo[] }>({});
const components = ref<{ _id: string; _name: ComponentsKey; _navs: { _id: string; _name: string, description: string; summary?: string; }[] }[]>([]);
const servers = ref<OpenApiInfo['servers']>([]);
const securitys = ref<OpenApiInfo['security']>([]);

const selectedLinkId = ref<string>();
const openKeys = ref<Array<'doc' | 'components' | 'tags' | 'servers' | 'security'>>(['doc', 'components', 'tags', 'servers', 'security']);// 默认打开文档
const docItemOpenKeys = ref<string[]>([]);// 默认全部展开
const docItemElementOpenKeys = ref<string[]>([]);// 默认全部展开
const componentItemOpenKeys = ref<string[]>([]);
const documentContentOpenkeys = ref<string[]>([]);// 文档内容折叠面板展开收起keys
const documentContentItemOpenkeys = ref<string[]>([]);

const loadData = async () => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, { data }] = await http.get(`${TESTER}/services/${props.serviceId}/openapi`, { gzipCompression: false, format: 'json' });
  if (error) {
    loading.value = false;
    return;
  }

  openapiMetaData.value = JSON.parse(data || '{}');
  openapiYamlData.value = YAML.stringify(openapiMetaData.value, null, 2);
  // const _tags = openapiMetaData.value?.tags || [];
  // tags.value = _tags.map(item => {
  //   return {
  //     ...item,
  //     _id: utils.uuid('_')
  //   };
  // }) || [];

  // pathTags.value = _tags.map(item => {
  //   return {
  //     ...item,
  //     _id: utils.uuid('_')
  //   };
  // }) || [];

  // const pathTagIds = pathTags.value.map(item => item._id);
  // // 默认全部展开左侧导航;
  // docItemOpenKeys.value = pathTagIds;
  // // 默认全部展开右侧接口文档;
  // documentContentOpenkeys.value = [...pathTagIds, 'components', 'tags', 'servers', 'security'];

  // if (openapiMetaData.value?.paths) {
  //   setPathsMap(openapiMetaData.value?.paths);
  // }

  // if (openapiMetaData.value?.components) {
  //   setComponents(openapiMetaData.value.components);
  // }

  // if (openapiMetaData.value?.servers) {
  //   setServers(openapiMetaData.value.servers);
  // }

  // if (openapiMetaData.value?.security) {
  //   setSecurity(openapiMetaData.value.security);
  // }

  loading.value = false;
};

const setPathsMap = (data: OpenApiInfo['paths']) => {
  docItemElementMap.value = {};
  docItemElementOpenKeys.value = [];
  selectedLinkId.value = undefined;
  documentContentItemOpenkeys.value = [];
  for (const key in data) {
    const item = data[key];
    if (item) {
      for (const _key in item) {
        const itemData: PathItemInfo = {
          ...item[_key],
          _id: utils.uuid('_'),
          _uri: key,
          _method: _key,
          _navs: []
        };

        // 默认全部展开
        const _id = itemData._id;
        // 默认全部展开左侧接口导航
        docItemElementOpenKeys.value.push(_id);
        // 默认全部展开右侧接口文档
        documentContentItemOpenkeys.value.push(_id);

        if (itemData.parameters) {
          itemData.parameters = getArraySchema(itemData.parameters, openapiMetaData.value?.components);
          itemData._navs.push({
            _id: utils.uuid('_'),
            _name: '请求参数',
            _key: 'parameters'
          });
        }

        if (itemData.requestBody) {
          itemData.requestBody = getObjectShcema(itemData.requestBody, openapiMetaData.value?.components);
          itemData._navs.push({
            _id: utils.uuid('_'),
            _name: '请求体',
            _key: 'requestBody'
          });
        }

        itemData._navs.push({
          _id: utils.uuid('_'),
          _name: '请求示例',
          _key: 'requestDemo'
        });

        if (itemData.security?.length) {
          itemData._navs.push({
            _id: utils.uuid('_'),
            _name: '安全需求',
            _key: 'security'
          });
        }

        const keys = Object.keys(itemData.responses || {});
        if (keys.length) {
          itemData.responses = getObjectShcema(itemData.responses, openapiMetaData.value?.components);
          keys.every(item => {
            itemData._navs.push({
              _id: utils.uuid('_'),
              _name: item,
              _key: item
            });
            return true;
          });
        }

        const tags = itemData?.tags;
        if (tags?.length) {
          for (let i = 0, len = tags.length; i < len; i++) {
            const tag = tags[i];
            if (docItemElementMap.value[tag]) {
              docItemElementMap.value[tag].push(itemData);
            } else {
              docItemElementMap.value[tag] = [itemData];
            }
          }
        }
      }
    }
  }
};

const setComponents = (data: OpenApiInfo['components']) => {
  components.value = [];
  const ids: string[] = [];
  for (const key in data) {
    const id = utils.uuid('_');
    const itemInfo: {
      _id: string;
      _name: string;
      _navs: { _id: string; _name: string }[];
    } = {
      _navs: [],
      _name: key,
      _id: id
    };

    ids.push(id);

    for (const _key in data[key]) {
      itemInfo._navs.push({
        ...data[key][_key],
        _name: _key,
        _id: utils.uuid('_')
      });
    }

    components.value.push(itemInfo);
  }

  componentItemOpenKeys.value = ids;
};

const setServers = (data: OpenApiInfo['servers']) => {
  servers.value = data.map(item => {
    return {
      ...item,
      _id: utils.uuid('_')
    };
  });
};

const serverUrl = computed(() => {
  return servers.value?.[0]?.url;
});

const securitySchemes = computed(() => {
  const schemes = openapiMetaData.value?.components?.securitySchemes;
  if (!schemes) {
    return {};
  }

  for (const key in schemes) {
    const item = schemes[key];
    const type = item.type;
    let typeName = '';
    if (type === 'http') {
      const _scheme = item.scheme;
      if (_scheme) {
        if (_scheme === 'basic') {
          typeName = 'basic Auth';
        } else if (_scheme === 'bearer') {
          typeName = 'Bearer Auth';
        }
      } else {
        typeName = 'http';
      }
    } else if (type === 'apiKey') {
      typeName = 'API Key';
    } else if (type === 'oauth2') {
      typeName = 'OAuth2';
    } else if (type === 'openIdConnect') {
      typeName = 'OpenID Connect';
    }

    item.typeName = typeName;
  }

  return schemes;
});

const setSecurity = (data: OpenApiInfo['security']) => {
  securitys.value = data.map(item => {
    const entries = Object.entries(item)[0];
    return {
      _name: entries[0],
      _value: entries[1] as string[],
      _id: utils.uuid('_')
    };
  });
};

const arrowChange = (open: boolean, key: string, type: 'top' | 'docItem' | 'docItemElement' | 'componentItem' | 'documentContent' | 'documentContentItem') => {
  let target = ref<string[]>([]);
  if (type === 'top') {
    target = openKeys;
  } else if (type === 'docItem') {
    target = docItemOpenKeys;
  } else if (type === 'docItemElement') {
    target = docItemElementOpenKeys;
  } else if (type === 'componentItem') {
    target = componentItemOpenKeys;
  } else if (type === 'documentContent') {
    target = documentContentOpenkeys;
  } else if (type === 'documentContentItem') {
    target = documentContentItemOpenkeys;
  }

  if (open) {
    if (!target.value.includes(key)) {
      target.value.push(key);
    }

    return;
  }

  target.value = target.value.filter(item => item !== key);
};

const selectStr = ref();
const startKey = ref<string[]>([]);
const linkClick = (topId: string | null, pid: string | null, id: string, part: {path: string; method: string; key?: string}|undefined = undefined) => {
  selectedLinkId.value = id;
  let timeout = 0;
  if (props.mode === 'code') {
    if (part) {
      const { path, method, key } = part;
      if (key && ['parameters', 'requestBody', 'security'].includes(key)) {
        selectStr.value = YAML.stringify({ [key]: openapiMetaData.value.paths['/' + path][method][key] });
        startKey.value = ['/' + path, method];
      } else if (key && +key >= 100 && +key <= 600) {
        selectStr.value = YAML.stringify({ [key]: openapiMetaData.value.paths['/' + path][method].responses[key] });
        startKey.value = ['/' + path, method, 'responses'];
      } else {
        selectStr.value = YAML.stringify({ [method]: openapiMetaData.value.paths['/' + path][method] });
        startKey.value = ['/' + path];
      }
    } else {
      selectStr.value = undefined;
      startKey.value = [];
    }
    return;
  }
  if (topId) {
    if (!documentContentOpenkeys.value.includes(topId)) {
      documentContentOpenkeys.value.push(topId);
      timeout = 1200;
    }
  }

  if (pid) {
    if (!documentContentItemOpenkeys.value.includes(pid)) {
      documentContentItemOpenkeys.value.push(pid);
      timeout = 1200;
    }
  }

  setTimeout(() => {
    const dom = document.getElementById(id);
    if (dom) {
      dom.scrollIntoView({ block: 'start', inline: 'nearest', behavior: 'smooth' });
    }
  }, timeout);
};

onMounted(() => {
  loadData();
});

defineExpose({ loadData });
</script>

<template>
  <Spin :spinning="loading" class="flex h-full flex-nowrap text-3">
    <!-- <div class="h-full flex-shrink-0 w-80">
      <div style="height: calc(100% - 40px);" class="overflow-y-auto overflow-x-hidden pr-5">
        <Collapse
          v-model:activeKey="openKeys"
          :bordered="false"
          style="background-color: #fff;"
          class="space-y-3"
          collapsible="header">
          <CollapsePanel
            key="doc"
            class="border-01 header-p-21 header-bg-blue-1"
            :showArrow="false">
            <template #header>
              <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                <div class="flex-1 flex items-center mr-3">
                  <Icon icon="icon-wendangxinxi" class="text-4 mr-1.5" />
                  <span>文档</span>
                </div>
                <Arrow
                  class="flex-shrink-0"
                  :open="openKeys.includes('doc')"
                  @change="arrowChange($event, 'doc', 'top')" />
              </div>
            </template>
            <Collapse
              v-model:activeKey="docItemOpenKeys"
              :bordered="false"
              style="background-color: #fff;"
              class="space-y-2 content-px-0"
              collapsible="header">
              <CollapsePanel
                v-for="item in pathTags"
                :key="item._id"
                :showArrow="false">
                <template #header>
                  <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                    <div :title="item.name" class="flex-1 flex items-center mr-3 truncate">
                      {{ item.name }}
                    </div>
                    <Arrow
                      class="flex-shrink-0"
                      :open="docItemOpenKeys.includes(item._id)"
                      @change="arrowChange($event, item._id, 'docItem')" />
                  </div>
                </template>

                <Collapse
                  v-model:activeKey="docItemElementOpenKeys"
                  :bordered="false"
                  style="background-color: #fff;"
                  class="space-y-2"
                  collapsible="disabled">
                  <CollapsePanel
                    v-for="element in docItemElementMap[item.name]"
                    :key="element._id"
                    :showArrow="false"
                    class="border-0 header-px-2 header-py-1.5 header-bg-gray">
                    <template #header>
                      <div
                        class="w-full flex justify-between items-center leading-5 text-3 cursor-pointer text-theme-title"
                        @click.prevent="linkClick(item._id, element._id, element._id, {path: item.name, method: element._method })">
                        <div class="flex-1 flex items-center mr-3 overflow-hidden">
                          <HttpMethodTag class="mr-3 flex-shrink-0" :value="element._method" />
                          <span :title="element.summary || element._uri" class="truncate">
                            {{ element.summary || element._uri }}</span>
                        </div>
                        <Arrow
                          class="flex-shrink-0"
                          :open="docItemElementOpenKeys.includes(element._id)"
                          @change="arrowChange($event, element._id, 'docItemElement')" />
                      </div>
                    </template>
                    <div class="space-y-1 leading-7 text-theme-content text-3">
                      <a
                        v-for="nav in element._navs"
                        :key="nav._id"
                        :title="nav._name"
                        :class="{ 'selected-link': selectedLinkId === nav._id }"
                        class="hash-link rounded px-2.5 block truncate text-theme-title"
                        @click.prevent="linkClick(item._id, element._id, nav._id, {path: item.name, method: element._method, key: nav._key })">
                        {{ nav._name }}
                      </a>
                    </div>
                  </CollapsePanel>
                </Collapse>
              </CollapsePanel>
            </Collapse>
          </CollapsePanel>

          <CollapsePanel key="components" :showArrow="false">
            <template #header>
              <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                <div class="flex-1 flex items-center mr-3">
                  <Icon icon="icon-zujian" class="text-4 mr-1.5" />
                  <span>组件</span>
                </div>
                <Arrow
                  class="flex-shrink-0"
                  :open="openKeys.includes('components')"
                  @change="arrowChange($event, 'components', 'top')" />
              </div>
            </template>
            <Collapse
              v-model:activeKey="componentItemOpenKeys"
              :bordered="false"
              style="background-color: #fff;"
              class="space-y-2 content-px-0"
              collapsible="header">
              <CollapsePanel
                v-for="item in components"
                :key="item._id"
                :showArrow="false">
                <template #header>
                  <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                    <div :title="componentTextMap[item._name]" class="flex-1 flex items-center mr-3 truncate">
                      {{ componentTextMap[item._name] }}
                    </div>
                    <Arrow
                      class="flex-shrink-0"
                      :open="componentItemOpenKeys.includes(item._id)"
                      @change="arrowChange($event, item._id, 'componentItem')" />
                  </div>
                </template>
                <div class="space-y-1 leading-7 text-theme-content text-3">
                  <a
                    v-for="nav in item._navs"
                    :key="nav._id"
                    :title="nav._name"
                    :class="{ 'selected-link': selectedLinkId === nav._id }"
                    class="hash-link rounded px-2.5 block truncate text-theme-title"
                    @click.prevent="linkClick('components', null, nav._id)">
                    {{ nav._name }}
                  </a>
                </div>
              </CollapsePanel>
            </Collapse>
          </CollapsePanel>

          <CollapsePanel key="tags" :showArrow="false">
            <template #header>
              <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                <div class="flex-1 flex items-center mr-3">
                  <Icon icon="icon-biaoqian" class="text-4 mr-1.5" />
                  <span>标签</span>
                </div>
                <Arrow
                  class="flex-shrink-0"
                  :open="openKeys.includes('tags')"
                  @change="arrowChange($event, 'tags', 'top')" />
              </div>
            </template>
            <div class="space-y-1 leading-7 text-theme-content text-3">
              <a
                v-for="nav in tags"
                :key="nav._id"
                :title="nav.name"
                :class="{ 'selected-link': selectedLinkId === nav._id }"
                class="hash-link rounded px-2.5 block truncate text-theme-title"
                @click.prevent="linkClick('tags', null, nav._id)">
                {{ nav.name }}
              </a>
            </div>
          </CollapsePanel>

          <CollapsePanel key="servers" :showArrow="false">
            <template #header>
              <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                <div class="flex-1 flex items-center mr-3">
                  <Icon icon="icon-host" class="text-4 mr-1.5" />
                  <span>服务器</span>
                </div>
                <Arrow
                  class="flex-shrink-0"
                  :open="openKeys.includes('servers')"
                  @change="arrowChange($event, 'servers', 'top')" />
              </div>
            </template>
            <div class="space-y-1 leading-7 text-theme-content text-3">
              <a
                v-for="nav in servers"
                :key="nav._id"
                :title="nav.url"
                :class="{ 'selected-link': selectedLinkId === nav._id }"
                class="hash-link rounded px-2.5 block truncate text-theme-title"
                @click.prevent="linkClick('servers', null, nav._id)">
                {{ nav.url }}
              </a>
            </div>
          </CollapsePanel>

          <CollapsePanel key="security" :showArrow="false">
            <template #header>
              <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
                <div class="flex-1 flex items-center mr-3">
                  <Icon icon="icon-anquan" class="text-4 mr-1.5" />
                  <span>安全</span>
                </div>
                <Arrow
                  class="flex-shrink-0"
                  :open="openKeys.includes('security')"
                  @change="arrowChange($event, 'security', 'top')" />
              </div>
            </template>
            <div class="space-y-1 leading-7 text-theme-content text-3">
              <a
                v-for="nav in securitys"
                :key="nav._id"
                :title="nav._name"
                :class="{ 'selected-link': selectedLinkId === nav._id }"
                class="hash-link rounded px-2.5 block truncate text-theme-title"
                @click.prevent="linkClick('security', null, nav._id)">
                {{ nav._name }}
              </a>
            </div>
          </CollapsePanel>
        </Collapse>
      </div>
    </div>

    <div class="w-0.25 h-full bg-border-divider flex-shrink-0"></div>

    <div v-show="props.mode==='UI'" class="h-full flex-1 px-5 overflow-y-auto overflow-x-hidden leading-5 text-theme-content">
      <Summary :value="openapiMetaData" />
      <Collapse
        v-model:activeKey="documentContentOpenkeys"
        :bordered="false"
        style="background-color: #fff;"
        class="space-y-4"
        collapsible="header">
        <CollapsePanel
          v-for="item in pathTags"
          :key="item._id"
          :showArrow="false">
          <template #header>
            <div class="w-full flex justify-between items-center leading-5 text-3.5 text-theme-title">
              <div
                :id="item._id"
                :title="item.name"
                class="flex-1 flex items-center mr-3 truncate">
                {{ item.name }}
              </div>
              <Arrow
                class="flex-shrink-0"
                :open="documentContentOpenkeys.includes(item._id)"
                @change="arrowChange($event, item._id, 'documentContent')" />
            </div>
          </template>

          <Collapse
            v-model:activeKey="documentContentItemOpenkeys"
            :bordered="false"
            style="background-color: #fff;"
            class="space-y-4"
            collapsible="header">
            <CollapsePanel
              v-for="element in docItemElementMap[item.name]"
              :key="element._id"
              :showArrow="false"
              :class="{ 'block-deprecated': element.deprecated }">
              <template #header>
                <div class="w-full flex justify-between items-center leading-5 text-3">
                  <div class="space-y-2.5">
                    <div :id="element._id" class="flex items-start flex-nowrap">
                      <div class="text-3.5 text-theme-title mr-3 whitespace-pre-wrap break-all">
                        {{ element.summary }}
                      </div>
                      <ApiStatus
                        v-if="element['x-xc-status']"
                        :status="element['x-xc-status']"
                        class="flex-shrink-0" />
                    </div>
                    <div class="flex items-center overflow-hidden">
                      <HttpMethodTag class="flex-shrink-0 mr-3" :value="element._method" />
                      <span
                        :title="element._uri"
                        :class="{ 'line-through': element.deprecated }"
                        class="truncate">{{
                          element._uri }}</span>
                      <div
                        v-if="element.deprecated"
                        style="background-color:rgba(255,167,38,85%);"
                        class="px-2 leading-5 ml-5 select-none rounded text-white text-center flex-shrink-0">
                        <span class="block" style="transform: scale(0.95);">已废弃</span>
                      </div>
                    </div>
                    <TypographyParagraph
                      class="text-theme-sub-content break-all"
                      :content="element.description"
                      :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
                  </div>
                  <Arrow
                    class="flex-shrink-0"
                    :open="documentContentItemOpenkeys.includes(element._id)"
                    @change="arrowChange($event, element._id, 'documentContentItem')" />
                </div>
              </template>
              <div class="mt-1">
                <div class="flex items-center text-3.5 text-theme-title mb-3">
                  <div class="w-1.25 h-3 rounded mr-2" style="background-color: #1e88e5;"></div>
                  <div>请求</div>
                </div>
                <RequestParametersTable
                  v-if="!!element.parameters?.length"
                  class="mb-6"
                  :navs="element._navs"
                  :value="element.parameters" />
                <RequestBodyTable
                  v-if="!!element.requestBody"
                  class="mb-6"
                  :navs="element._navs"
                  :value="element.requestBody" />
                <RequestDemo
                  class="mb-6"
                  :serverUrl="serverUrl"
                  :navs="element._navs"
                  :value="element" />
                <PathSecurity
                  class="mb-6"
                  :navs="element._navs"
                  :value="element.security"
                  :securitySchemes="securitySchemes" />
              </div>

              <div class="mt-1">
                <div class="flex items-center text-3.5 text-theme-title mb-3">
                  <div class="w-1.25 h-3 rounded mr-2" style="background-color: #1e88e5;"></div>
                  <div>返回响应</div>
                </div>
                <Response :navs="element._navs" :value="element.responses" />
              </div>
            </CollapsePanel>
          </Collapse>
        </CollapsePanel>

        <CollapsePanel key="components" :showArrow="false">
          <template #header>
            <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
              <div class="flex-1 flex items-center mr-3">
                <Icon icon="icon-zujian" class="text-4 mr-1.5" />
                <span>组件</span>
              </div>
              <Arrow
                class="flex-shrink-0"
                :open="documentContentOpenkeys.includes('components')"
                @change="arrowChange($event, 'components', 'documentContent')" />
            </div>
          </template>
          <div
            v-for="item in components"
            :key="item._id"
            class="py-2.5 space-y-8">
            <div v-for="nav in item._navs" :key="nav._id">
              <div :id="nav._id" class="text-3.5 text-theme-title mr-3 whitespace-pre-wrap break-all">
                <span>{{ nav._name }}</span>
              </div>
              <div class="flex items-center space-x-2 mt-1.5">
                <ComponentTag :value="item._name" />
                <Tag
                  v-if="securitySchemes[nav._name]?.typeName"
                  class="flex-shrink-0"
                  color="rgba(0, 213, 158, 1)">
                  {{ securitySchemes[nav._name].typeName }}
                </Tag>
              </div>
              <div v-if="nav.summary" class="flex items-center mt-1.5">
                <div class="flex items-center w-9">
                  <span class="text-theme-title">摘要</span>
                  <Colon />
                </div>
                <TypographyParagraph :content="nav.summary" :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
              </div>
              <TypographyParagraph
                v-if="nav.description"
                class="text-theme-sub-content mt-1.5 break-all"
                :content="nav.description"
                :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
              <ComponentSchemaTable v-if="item._name === 'schemas'" :value="nav" />
              <ComponentParameterTable
                v-else-if="item._name === 'parameters' || item._name === 'headers'"
                :value="nav"
                class="mt-3" />
              <ComponentRequestBodyTable
                v-else-if="item._name === 'responses' || item._name === 'requestBodies'"
                :value="nav"
                class="mt-3" />
              <ComponentExample
                v-else-if="item._name === 'examples'"
                :value="nav"
                class="mt-3" />
              <ComponentSecurity
                v-else-if="item._name === 'securitySchemes'"
                :value="nav"
                class="mt-3" />
            </div>
          </div>
        </CollapsePanel>

        <CollapsePanel key="tags" :showArrow="false">
          <template #header>
            <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
              <div class="flex-1 flex items-center mr-3">
                <Icon icon="icon-biaoqian" class="text-4 mr-1.5" />
                <span>标签</span>
              </div>
              <Arrow
                class="flex-shrink-0"
                :open="documentContentOpenkeys.includes('tags')"
                @change="arrowChange($event, 'tags', 'documentContent')" />
            </div>
          </template>
          <div
            v-for="item in tags"
            :key="item._id"
            class="py-2.5">
            <div :id="item._id" class="text-3.5 text-theme-title whitespace-pre-wrap break-all">{{ item.name }}</div>
            <div v-if="item.externalDocs" class="flex items-center mt-1.5">
              <div class="flex items-center w-15">
                <span class="text-theme-title">外部文档</span>
                <Colon />
              </div>
              <a
                :href="item.externalDocs.url"
                target="_blank"
                style="color:#1890ff"
                class="truncate">{{
                  item.externalDocs.url }}</a>
            </div>
            <TypographyParagraph
              v-if="item.description"
              class="text-theme-sub-content mt-1.5 break-all"
              :content="item.description"
              :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
          </div>
        </CollapsePanel>

        <CollapsePanel key="servers" :showArrow="false">
          <template #header>
            <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
              <div class="flex-1 flex items-center mr-3">
                <Icon icon="icon-host" class="text-4 mr-1.5" />
                <span>服务器</span>
              </div>
              <Arrow
                class="flex-shrink-0"
                :open="documentContentOpenkeys.includes('servers')"
                @change="arrowChange($event, 'servers', 'documentContent')" />
            </div>
          </template>
          <div
            v-for="item in servers"
            :key="item._id"
            class="py-2.5 mb-2">
            <div :id="item._id" class="text-3.5 text-theme-title whitespace-pre-wrap break-all">{{ item.url }}</div>
            <TypographyParagraph
              v-if="item.description"
              class="text-theme-sub-content mt-1.5 break-all"
              :content="item.description"
              :ellipsis="{ rows: 3, expandable: true, symbol: '更多' }" />
            <ServerVariables :value="item.variables" class="mt-1.5" />
          </div>
        </CollapsePanel>

        <CollapsePanel key="security" :showArrow="false">
          <template #header>
            <div class="w-full flex justify-between items-center leading-5 text-3 text-theme-title">
              <div class="flex-1 flex items-center mr-3">
                <Icon icon="icon-anquan" class="text-4 mr-1.5" />
                <span>安全</span>
              </div>
              <Arrow
                class="flex-shrink-0"
                :open="documentContentOpenkeys.includes('security')"
                @change="arrowChange($event, 'security', 'documentContent')" />
            </div>
          </template>

          <div
            v-for="item in securitys"
            :key="item._id"
            class="py-2.5 mb-2 space-y-1.5 last:mb-0">
            <div :id="item._id" class="text-3.5 text-theme-title whitespace-pre-wrap break-all">{{ item._name }}</div>
            <GlobalSecurity :value="securitySchemes[item._name]" :scopes="item._value" />
          </div>
        </CollapsePanel>
      </Collapse>
    </div> -->

    <CodeView
      :value="openapiYamlData"
      :selectStr="selectStr"
      :startKey="startKey">
    </codeview>
  </Spin>
</template>
<style scoped>
.ant-tag {
  margin-right: 0;
  line-height: 18px;
}

.block-deprecated {
  opacity: 0.75;
}

.block-deprecated .api-status-div,
.block-deprecated .http-method-div {
  border-color: #ebebeb;
  background-color: #ebebeb;
  color: #fff;
}

.hash-link:hover {
  background-color: var(--content-tabs-bg-hover);
}

.hash-link.selected-link {
  background-color: rgba(239, 240, 243, 100%);
  color: #1890ff;
}

.ant-collapse {
  font-size: 12px;
  line-height: 20px;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  position: relative;
  padding: 10px 0;
  border-radius: 4px;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.ant-collapse> :deep(.ant-collapse-item.header-p-2)>.ant-collapse-header {
  padding: 8px;
}

.ant-collapse> :deep(.ant-collapse-item.header-px-2)>.ant-collapse-header {
  padding-right: 8px;
  padding-left: 8px;
}

.ant-collapse> :deep(.ant-collapse-item.header-py-1\.5)>.ant-collapse-header {
  padding-top: 6px;
  padding-bottom: 6px;
}

.ant-collapse> :deep(.ant-collapse-item) .ant-collapse-header .ant-collapse-header-text {
  width: 100%;
}

.ant-collapse> :deep(.ant-collapse-item.border-0)>.ant-collapse-header::after {
  display: none;
}

.ant-collapse-borderless>:deep(.ant-collapse-item)>.ant-collapse-content>.ant-collapse-content-box {
  padding: 8px 10px 0;
}

.ant-collapse-borderless> :deep(.ant-collapse-item) {
  border-bottom: 0;
}

.ant-collapse> :deep(.ant-collapse-item.header-bg-gray)>.ant-collapse-header {
  transition: all 300ms linear 0ms;
  background: rgba(247, 248, 251, 100%);
}

.ant-collapse> :deep(.ant-collapse-item.header-bg-blue)>.ant-collapse-header {
  background: rgba(0, 119, 255, 6%);
}

.ant-collapse> :deep(.ant-collapse-item.header-bg-gray)>.ant-collapse-header:hover {
  background: rgba(0, 119, 255, 6%);
}

.ant-collapse.content-p-0>:deep(.ant-collapse-item)>.ant-collapse-content>.ant-collapse-content-box {
  padding: 0;
}

.ant-collapse.content-px-0>:deep(.ant-collapse-item)>.ant-collapse-content>.ant-collapse-content-box {
  padding-right: 0;
  padding-left: 0;
}
</style>

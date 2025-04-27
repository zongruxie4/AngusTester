<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Composite, Icon, Input, Select } from '@xcan-angus/vue-ui';
import { Button, CheckableTag, Popover, Tag } from 'ant-design-vue';
import { ApiAuthencation } from 'angus-design';

interface Props {
    dataSource: {[key: string]: any}; // openapi json
    selectStr?: string;
    startKey?: string;
}

const info = ref<{[key: string]: string}>({});
const contact = ref<{[key: string]: string}>({});
const license = ref<{[key: string]: string}>({});
const authorizations = ref<{[key: string]: any}[]>([]);
const servers = ref<{url: string, description: string, variables: {[Key: string]: any}[], showVariables: boolean}[]>([]);
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}),
  selectStr: undefined
});

const authType = [
  'http',
  'apiKey',
  'oauth2'
];

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    info.value = JSON.parse(JSON.stringify(newValue.info));
    contact.value = info.value?.contact || {};
    license.value = info.value.license || {};
    servers.value = JSON.parse(JSON.stringify(newValue.servers || [])).map(item => {
      return {
        ...item,
        showVariables: false,
        variables: Object.keys(item.variables || {}).map(key => {
          return {
            name: key,
            ...item.variables[key],
            default: item.variables[key].default ? item.variables[key].enum.findIndex(i => i === item.variables[key].default) : ''
          };
        })
      };
    });
    authorizations.value = Object.keys(newValue?.components?.securitySchemes || {}).map(key => {
      return {
        key,
        showDetail: false,
        ...newValue.components.securitySchemes[key]
      };
    });
  }, {
    immediate: true,
    deep: true
  });
});

const variableReg = /\{[a-zA-Z0-9_]+\}/g; // 匹配任意字母数字（大小写均可）
const onServerBlur = (serverIdx, e) => {
  const url = e.target.value;
  servers.value[serverIdx].url = url;
  const keys = url.match(variableReg);
  (keys || []).forEach(key => {
    const target = key.replace('{', '').replace('}', '');
    if (!servers.value[serverIdx].variables?.some(vari => vari.name === target)) {
      servers.value[serverIdx].variables.push({ name: target, enum: [], defualt: '' });
    }
  });
};

const addAllowedValues = (serverIdx, idx) => {
  servers.value[serverIdx].variables[idx].enum.push('');
};

const delAllowedValues = (serverIndex, variableIdx, idx) => {
  servers.value[serverIndex].variables[variableIdx].enum.splice(idx, 1);
};

const delVaraible = (serverIndex, variableIdx) => {
  const variableKey = servers.value[serverIndex].variables[variableIdx].name;
  servers.value[serverIndex].variables.splice(variableIdx, 1);
  servers.value[serverIndex].url = servers.value[serverIndex].url.replace(`/{${variableKey}}`, '');
};

const addVaraibled = (serverIndex) => {
  servers.value[serverIndex].variables.push({ name: '', enum: [], defualt: '' });
};

const addServer = () => {
  servers.value.push({ url: '', description: '', variables: [], showVariables: false });
};

const delSever = (serverIdx) => {
  servers.value.splice(serverIdx, 1);
};

const delAuthoriz = (idx: number) => {
  authorizations.value.splice(idx, 1);
};

const changeAuthentication = (data, idx: number) => {
  authorizations.value[idx] = {
    ...authorizations.value[idx],
    ...data
  };
};

const addAuthenticaton = () => {
  authorizations.value.push({
    type: 'http',
    scheme: 'basic',
    key: ''
  });
};

const onAuthTypeChange = (idx) => {
  const { type, key, showDetail } = authorizations.value[idx];
  if (type === 'http') {
    authorizations.value[idx] = {
      key,
      type,
      showDetail,
      scheme: 'basic'
    };
    return;
  }
  if (type === 'apiKey') {
    authorizations.value[idx] = {
      key,
      type,
      showDetail,
      name: '',
      in: 'header'
    };
    return;
  }
  if (type === 'oauth2') {
    authorizations.value[idx] = {
      key,
      type,
      showDetail
    };
  }
};

const getData = () => {
  const securitySchemes = {};
  authorizations.value.forEach(i => {
    const key = i.key;
    if (key && !securitySchemes[key]) {
      delete i.key;
      securitySchemes[key] = {
        ...i
      };
    }
  });
  return {
    info: {
      ...info.value,
      contact: contact.value,
      license: license.value
    },
    servers: servers.value.map(item => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { showVariables, variables, ...server } = item;
      const variablesObj = {};
      (variables || []).forEach(item => {
        const { key, ...others } = item;
        variablesObj[key] = {
          ...others,
          default: (item.enum || [])[item.default]
        };
      });
      return {
        ...server,
        variables: variablesObj
      };
    }),
    components: {
      ...(props.dataSource.components || {}),
      securitySchemes
    }
  };
};
defineExpose({
  getData
});
</script>
<template>
  <div class="space-y-4 px-4">
    <!-- GENERAL -->
    <div class="flex space-x-2 mt-4">
      <Tag color="#87d068">
        {{ info.version }}
      </Tag>
      <Input
        v-model:value="info.title"
        :maxlength="800" />
    </div>
    <div>
      <span class="font-semibold">Summary</span>
      <Input
        v-model:value="info.summary"
        :maxlength="800" />
    </div>
    <div>
      <span class="font-semibold">Description</span>
      <Input
        v-model:value="info.description"
        type="textarea"
        :maxlength="800" />
    </div>
    <!-- SERVER -->
    <div>
      <div class="flex justify-between items-center pb-1 border-b">
        <div class=" flex items-center">
          <Icon icon="icon-quanjupeizhi" class="text-5" />
          <span class="font-semibold text-4 ml-2">Servers</span>
        </div>
        <Button
          type="primary"
          size="small"
          @click="addServer">
          Add
        </Button>
      </div>
      <div
        v-for="(server, serverIndex) in servers"
        :key="server.url"
        class="mt-2">
        <Composite>
          <Input
            :value="servers[serverIndex].url"
            :maxlength="200"
            dataType="mixin-en"
            includes=".-_"
            @blur="onServerBlur(serverIndex, $event)" />
          <div
            class="">
            <CheckableTag
              v-model:checked="servers[serverIndex].showVariables">
              show variables
            </CheckableTag>
          </div>
          <Button size="small" @click="delSever(serverIndex)"><Icon icon="icon-qingchu" /></Button>
        </Composite>
        <div v-if="servers[serverIndex].showVariables" class="px-4">
          <Button
            size="small"
            type="text"
            @click="addVaraibled(serverIndex)">
            <Icon icon="icon-jia" class="text-3 mr-1" />
            variables
          </Button>
          <Composite
            v-for="(variable, variableIdx) in server.variables"
            :key="variableIdx"
            class="mt-2">
            <Input
              v-model:value="variable.name"
              :maxlength="30"
              dataType="mixin-en"
              includes=".-_" />
            <Input
              v-model:value="variable.description"
              :maxlength="100" />
            <Select
              v-if="variable.enum"
              v-model:value="variable.default"
              class="w-100"
              :options="variable.enum.map((i, idx) => ({value: idx, label: i }))" />
            <Popover trigger="click" placement="top">
              <template #content>
                <div class="space-y-2 flex flex-col">
                  <Input
                    v-for="(_enumvalue, idx) in variable.enum"
                    :key="idx"
                    v-model:value="servers[serverIndex].variables[variableIdx].enum[idx]"
                    placeholder="required"
                    class="w-30">
                    <template #suffix>
                      <Button
                        type="text"
                        class="px-0"
                        @click="delAllowedValues(serverIndex, variableIdx, idx)">
                        <Icon icon="icon-qingchu" />
                      </Button>
                    </template>
                  </Input>
                  <Button
                    size="small"
                    @click="addAllowedValues(serverIndex, variableIdx)">
                    Add Allowed Values
                  </Button>
                </div>
              </template>
              <Button type="text" size="small"> Add variable</Button>
            </Popover>
            <Button
              type="text"
              size="small"
              @click="delVaraible(serverIndex, variableIdx)">
              <Icon icon="icon-qingchu" />
            </Button>
          </Composite>
        </div>
      </div>
    </div>
    <!-- SECURITY SCHEMA -->
    <div>
      <div class="flex justify-between items-center pb-1 border-b">
        <div class=" flex items-center">
          <Icon icon="icon-quanxian1" class="text-5" />
          <span class="font-semibold text-4 ml-2">Security scheme</span>
        </div>
        <Button
          type="primary"
          size="small"
          @click="addAuthenticaton">
          Add
        </Button>
      </div>
      <div
        v-for="(item, idx) in authorizations"
        :key="idx"
        class="my-2">
        <Composite>
          <template v-if="item.$ref">
            <Input
              v-model:value="item.key"
              placeholder="name"
              dataType="mixin-en"
              includes="-_.&*" />
            <Input
              v-model:value="item.$ref"
              readOnly />
          </template>
          <template v-else>
            <Select
              v-model:value="authorizations[idx].type"
              :options="authType.map(i => ({value: i, label: i}))"
              class="min-w-20"
              @change="onAuthTypeChange(idx)">
            </Select>
            <Input
              v-model:value="item.key"
              placeholder="name"
              dataType="mixin-en"
              includes="-_.&*" />
            <template v-if="item.type==='http'">
              <Select
                v-model:value="item.scheme"
                :options="[{value: 'basic', label: 'basic'}, {value: 'bearer', label: 'bearer'}]"
                @change="{}" />
            </template>
          </template>
          <CheckableTag
            v-show="!item.$ref"
            v-model:checked="authorizations[idx].showDetail">
            <Icon
              icon="icon-zidingyizhibiao1">
            </Icon>
          </CheckableTag>
          <Button
            @click="delAuthoriz(idx)">
            <Icon icon="icon-qingchu" />
          </Button>
        </Composite>
        <div v-show="item.showDetail" class="mt-5">
          <ApiAuthencation
            :defaultValue="item"
            @change="changeAuthentication($event, idx)" />
        </div>
      </div>
    </div>

    <!-- Contact -->
    <div>
      <div class=" flex items-center border-b">
        <Icon icon="icon-liuchengtu" class="text-5" />
        <span class="font-semibold text-4 ml-2">Contact</span>
      </div>
      <div class="flex my-2 space-x-2">
        <Input
          v-model:value="contact.name"
          placeholder="contact name"
          :maxlength="80" />
        <Input
          v-model:value="contact.url"
          placeholder="contact url"
          :maxlength="200" />
        <Input
          v-model:value="contact.email"
          placeholder="contact email"
          :maxlength="80" />
      </div>
      <Input
        v-model:value="info.termsOfService"
        :maxlength="100"
        placeholder="Terms of server url" />
    </div>
    <!-- License -->
    <div>
      <div class=" flex items-center border-b">
        <Icon icon="icon-lianjie1" class="text-5" />
        <span class="font-semibold text-4 ml-2">License</span>
      </div>
      <div class="flex my-2 space-x-2">
        <Input
          v-model:value="license.name"
          placeholder="contact name"
          :maxlength="80" />
        <Input
          v-model:value="license.url"
          placeholder="contact email"
          :maxlength="80" />
      </div>
    </div>
  </div>
</template>

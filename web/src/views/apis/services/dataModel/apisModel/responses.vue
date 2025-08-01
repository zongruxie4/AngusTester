<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Dropdown, Icon, Input } from '@xcan-angus/vue-ui';
import { enumUtils } from '@xcan-angus/infra';

import ResponseSchema from '../responseSchema.vue';

interface Props {
    dataSource: {[key: string]: {[key: string]: any}};
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const responseSchemaRef = ref([]);

const allStatus = ref<{ value:string, description:string }[]>([]);
const loadAllStatus = () => {
  const data = enumUtils.enumToMessages('HttpStatus');
  allStatus.value = data;
};

const data = ref({});
const respStatus = ref<string[]>([]);
const addStatus = (item) => {
  respStatus.value.push(item.key);
  data.value[item.key] = {
    content: {},
    description: item.name
  };
};

const editTab = (key:string) => {
  respStatus.value = respStatus.value.filter(i => i !== key);
};

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    data.value = JSON.parse(JSON.stringify(newValue));
    respStatus.value = Object.keys(data.value);
  }, {
    immediate: true,
    deep: true
  });
  loadAllStatus();
});

const getData = () => {
  const result = {};
  let hadErr = false;
  respStatus.value.forEach((status, idx) => {
    if (responseSchemaRef.value[idx]) {
      const reponse = responseSchemaRef.value[idx].getData();
      if (reponse === false) {
        hadErr = true;
      } else {
        result[status] = {
          ...reponse,
          description: data.value[status].description
        };
      }
    } else {
      result[status] = data.value[status];
    }
  });
  if (hadErr) {
    return false;
  }

  return result;
};

defineExpose({
  getData
});

</script>
<template>
  <div>
    <Tabs
      type="editable-card"
      hideAdd
      size="small"
      @edit="editTab">
      <template #rightExtra>
        <Dropdown
          :menuStyle="{maxHeight: '500px', overflowY: 'auto'}"
          placement="leftTop"
          :disabledKeys="respStatus"
          :menuItems="allStatus.map(i => ({ key: i.value, name: `${i.value} ${i.description}` }))"
          @click="addStatus">
          <Button
            size="small"
            type="primary">
            <Icon icon="icon-jia" />
          </Button>
        </Dropdown>
      </template>
      <TabPane
        v-for="(status, idx) in respStatus"
        :key="status"
        :tab="status"
        :closable="true"
        :disabled="props.viewType">
        <Input
          v-model:value="data[status].description"
          type="textarea" />
        <ResponseSchema
          :ref="dom => responseSchemaRef[idx] = dom"
          :data="data[status]" />
      </TabPane>
    </Tabs>
  </div>
</template>

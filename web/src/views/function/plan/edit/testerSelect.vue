<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { Input, Select } from '@xcan-angus/vue-ui';

interface Props {
    value: {[userId: string]: string};
    members: {fullname: string; id: string}[];
    membersOptions: {fullname: string; id: string, value: string; label: string;}[];
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  members: () => ([]),
  membersOptions: () => ([])
});

const emits = defineEmits<{(e: 'change', value: {[userId: string]: string})}>();

const data = ref<{id?: string; fullname?: string; description: string}[]>([]);

const change = () => {
  emits('change', getData());
};

const onUserChange = () => {
  if (data.value.every(i => i.id)) {
    data.value.push({ id: undefined, fullname: '', description: '' });
  }
  change();
};

const defaultUserOptions = ref({});

const repeatIds = computed(() => {
  const result: string[] = [];
  data.value.reduce((pre, current) => {
    if (current.id && pre.includes(current.id)) {
      result.push(current.id);
    } else {
      current.id && pre.push(current.id);
    }
    return pre;
  }, []);
  return result;
});

const getData = () => {
  const result = {};
  data.value.forEach(i => {
    if (i.id) {
      result[i.id] = i.description;
    }
  });
  return result;
};

onMounted(() => {
  watch(() => props.value, () => {
    if (props.value) {
      const propsValue = Object.keys(props.value).map(id => {
        const fullname = (props.members || []).find(user => user.id === id)?.fullname;
        defaultUserOptions.value[id] = { label: fullname, value: id, fullname, id };
        if (data.value.find(user => user.id === id)) {
          return null;
        }
        return {
          id: id,
          description: props.value[id],
          fullname
        };
      }).filter(Boolean);
      data.value.unshift(...propsValue);
    }

    if (data.value.find(user => !user.id)) {
      return;
    }
    data.value.push({ id: undefined, fullname: '', description: '' });
  }, {
    immediate: true
  });
});
defineExpose({
  getData,
  validate: () => {
    return !repeatIds.value.length;
  }
});

</script>
<template>
  <div class="text-3 rounded space-y-2">
    <div
      v-for="(item, idx) in data"
      :key="idx"
      class="flex space-x-2">
      <Select
        v-model:value="data[idx].id"
        :defaultOptions="defaultUserOptions"
        :options="props.membersOptions"
        :error="!!item.id && !!repeatIds.includes(item.id)"
        size="small"
        allowClear
        placeholder="选择人员"
        class="!w-50"
        @change="onUserChange" />
      <div class="flex-1">
        <Input
          v-model:value="item.description"
          :maxlength="1000"
          placeholder="工作职责"
          @blur="change" />
      </div>
    </div>
  </div>
</template>

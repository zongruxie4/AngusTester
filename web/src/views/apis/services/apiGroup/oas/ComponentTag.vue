<script setup lang="ts">
import { Tag } from 'ant-design-vue';

import { componentTextMap } from './data';
import { ComponentsKey } from './PropsType';

interface Props {
    value: ComponentsKey;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});
</script>
<template>
  <Tag color="rgba(78, 164, 255, 1)">{{ componentTextMap[props.value] }}</Tag>
</template>

<style scoped>
.ant-tag {
    margin-right: 0;
    line-height: 18px;
}
</style>

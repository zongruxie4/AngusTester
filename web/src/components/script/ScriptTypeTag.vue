<script setup lang="ts">
export interface Props{
  value:{message:string;value:string};
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});
</script>

<template>
  <div
    :class="props.value?.value"
    class="test-type-div inline-block h-5 leading-5 px-2 text-3 rounded select-none text-center">
    {{ props.value?.message }}
  </div>
</template>

<style scoped>
.test-type-div.FUNCTIONAL,
.test-type-div.TEST_FUNCTIONALITY {
  background-color: rgba(82, 196, 26, 70%);
}

.test-type-div.STABILITY,
.test-type-div.TEST_PERFORMANCE {
  background-color: rgba(45, 142, 255, 70%);
}

.test-type-div.PERFORMANCE,
.test-type-div.TEST_STABILITY {
  background-color: rgba(255, 185, 37, 70%);
}

.test-type-div.MOCK_DATA {
  background-color: rgba(201, 119, 255, 70%);
}

.test-type-div.CUSTOMIZATION,
.test-type-div.TEST_CUSTOMIZATION {
  background-color: rgba(251, 129, 255, 70%);
}
</style>

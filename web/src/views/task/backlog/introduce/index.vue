<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">关于Backlog</div>
    <div class="mb-6">
      <div>Backlog是一个待办工作事项列表，分为产品Backlog和迭代Backlog两部分。主要用于管理和规划团队的工作任务，它包含了所有需要完成的工作事项，无论是在当前迭代（Sprint）中还是未来的迭代中。</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">产品Backlog</div>
            <div>通常是由产品经理（PM）或者产品负责人（PO）维护一个动态的、不断演化的产品工作事项列表，包含了所有与产品相关的需求、功能、缺陷修复和技术改进等，并且确保每个工作事项的优先级和清晰度。</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/2">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">迭代Backlog</div>
            <div>在迭代计划会议后，根据计划将产品 Backlog 分解到对应迭代，或者从每个迭代开始时从产品 Backlog 中选择的一组工作事项作为冲刺任务，它具体化了团队在当前迭代中要完成的工作。</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

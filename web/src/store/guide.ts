const TEXT = {
  task: {
    createProjectOne: {
      title: '添加项目',
      content: '首先点击“添加项目”按钮添加一个项目。'
    },
    createProjectTwo: {
      title: '添加项目',
      content: '在项目名称输入框输入项目名称，这里添加名称为“示例项目”的默认项目。'
    },
    openProject: {
      title: '打开项目',
      content: '项目添加成功后会自动进入项目任务看板，点击下一步进入首页添加当前项目“计划”。'
    },
    createPlanOne: {
      title: '添加计划',
      content: '项目添加成功后，让我们开始添加一个计划。'
    },
    // step: 打开添加弹窗无文案
    createPlanTwo: {
      title: '',
      content: ''
    },
    viewPlan: {
      title: '查看计划',
      content: '计划添加成功后，可以在计划列表看到所添加的计划，点击计划名称快速进入任务看版。'
    },
    createTaskOne: {
      title: '添加任务',
      content: '点击“添加任务”按钮添加一个任务。'
    },
    // step: 打开添加弹窗无文案
    createTaskTwo: {
      title: '',
      content: ''
    },
    viewTask: {
      title: '查看任务',
      content: '任务添加成功后，可以在任务列表看到所添加的任务。'
    },
    hideProject: {
      title: '收起左侧项目',
      content: '点击“收起”图标收起左侧项目树后，可以获得更多任务看版区域。'
    }
  },
  case: {
    createProjectOne: {
      title: '添加目录',
      content: '首先点击“添加目录”按钮添加一个目录。'
    },
    createProjectTwo: {
      title: '添加目录',
      content: ' 在目录名称输入框输入目录名称，这里添加名称为“示例目录”的默认目录。'
    },
    openProject: {
      title: '打开目录',
      content: ' 目录添加成功后会自动进入对应用例看板，点击下一步进入首页添加当前目录“计划”。'
    },
    createPlanOne: {
      title: '添加计划',
      content: '目录添加成功后，让我们开始添加一个计划。'
    },
    createPlanTwo: {
      title: '',
      content: ''
    },
    viewPlan: {
      title: '查看计划',
      content: '计划添加成功后，可以在计划列表看到所添加的计划，点击计划名称快速进入用例看版。'
    },
    createCaseOne: {
      title: '添加功能用例',
      content: '点击“添加用例”按钮添加一个用例。'
    },
    createCaseTwo: {
      title: '',
      content: ''
    },
    viewCase: {
      title: '查看用例',
      content: '用例添加成功后，可以在用例列表看到所添加的用例。'
    },
    hideProject: {
      title: '收起左侧目录',
      content: '点击“收起”图标收起左侧目录树后，可以获得更多用例看版区域。'
    }
  },
  api: {
    createProjectOne: {
      title: '添加项目',
      content: '首先点击“添加项目”按钮添加一个项目。'
    },
    createProjectTwo: {
      title: '添加项目',
      content: '在项目名称输入框输入项目名称，这里添加名称为“示例项目”的默认项目。'
    },
    openProject: {
      title: '查看项目首页',
      content: '项目添加成功后会自动进入项目首页。'
    },
    // 选择调试接口
    selectDebugApi: {
      title: '调试接口',
      content: '点击“调试”在线快速编辑、调试接口。'
    },
    debugApiOne: {
      title: '输入调试接口信息',
      content: '这里自动为您输入了调试接口信息。'
    },
    debugApiTwo: {
      title: '发送请求',
      content: '输入完调试接口信息后点击“发送请求”按钮进行接口调试。'
    },
    debugApiThree: {
      title: '查看调试结果',
      content: '发送请求成功后，可以在页面下方Tab页面看到如下响应信息。'
    },
    debugApiFour: {
      title: '归档接口',
      content: '点击“归档”按钮当前调试接口保存到项目中。'
    },
    debugApiSix: {
      title: '保存调试',
      content: '保存调试结果之前需要输入接口名称等参数。'
    },
    debugApiSeven: {
      title: '保存归档接口',
      content: '填写接口信息后点击“保存”按钮完成归档。'
    },
    hideProject: {
      title: '收起左侧项目',
      content: '点击“收起”图标收起左侧项目树后，可以获得更多调试区域。'
    }
  },
  scenario: {
    createProjectOne: {
      title: '添加目录',
      content: '首先点击“添加目录”按钮添加一个场景目录。'
    },
    createProjectTwo: {
      title: '添加目录',
      content: ' 在目录名称输入框输入目录名称，这里添加名称为“示例目录”的默认场景目录。'
    },
    openProject: {
      title: '打开目录',
      content: ' 目录添加成功后会自动进入目录首页看板。'
    },
    selectCreate: {
      title: '添加场景',
      content: '点击“添加”按钮快速添加一个Http场景。'
    },
    createScenOne: {
      title: '场景编排',
      content: '输入必须编排元素参数，如名称、接口地址，这里默认如下名称为“基准性能测试”的Http测试元素。'
    },
    createScenTwo: {
      title: '添加断言',
      content: '添加预期的测试结果，这里默认断言Http响应状态码为200。'
    },
    createScenThree: {
      title: '执行配置',
      content: '进入“执行配置”Tab页面，配置主要执行参数，如：测试类型、线程数、执行时长。'
    },
    openSave: {
      title: '保存场景',
      content: '点击“保存场景”按钮，打开保存场景页面，输入场景名称为“Http示例场景”。'
    },
    saveScen: {
      title: '场景保存',
      content: '点击“保存”按钮保存场景。'
    },
    hideProject: {
      title: '收起左侧目录',
      content: '点击“收起”图标收起左侧目录树后，可以获得更多场景编辑区域。'
    }
  }
};

export default {
  namespaced: true,
  state: {
    guideType: '', // 'task' | 'case' | 'api' | 'scenario'
    stepVisible: false,
    stepKey: '',
    stepContent: ''
  },
  mutations: {
    updateGuideType (state: { guideType:string, }, type:string):void {
      state.guideType = type;
    },

    updateGuideStep (state: { stepVisible: boolean;stepKey:string, stepContent:{title:string, content:string;}, guideType:string }, params:{visible: boolean, key:string}):void {
      state.stepVisible = params.visible;
      const existingMaskDiv = document.querySelector('body > .guide-mask');
      if (state.stepVisible) {
        if (!existingMaskDiv) {
          const maskDiv = document.createElement('div');
          maskDiv.className = 'guide-mask';
          maskDiv.style.cssText = 'position: fixed; inset: 0; z-index: 999; height: 100%; ';
          document.body.appendChild(maskDiv);
        }
      } else {
        if (existingMaskDiv) {
          existingMaskDiv.remove();
        }
      }

      if (params.key) {
        state.stepKey = params.key;
        const _TEXT = TEXT[state.guideType];
        state.stepContent = { title: _TEXT[params.key].title, content: _TEXT[params.key].content };
      } else {
        state.stepKey = '';
        state.stepContent = { title: '', content: '' };
      }
    }
  },
  actions: {}
};

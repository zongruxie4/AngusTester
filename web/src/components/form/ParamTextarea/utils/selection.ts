// import { cookieUtils } from '@xcan-angus/infra';

import { FunctionConfig, CallbackValue } from './type';

// let localeCookie: 'zh_CN' | 'en' = cookieUtils.get('localeCookie');
// if (!['zh_CN', 'en'].includes(localeCookie)) {
//   localeCookie = 'zh_CN';
// }

// let locale:'zhCn'|'en';
// switch (localeCookie) {
//   case 'zh_CN':
//     locale = 'zhCn';
//     break;
//   case 'en':
//     locale = 'en';
//     break;
// }

// 接口数据转为{key:value}结构
const setNode = (fnConfig: FunctionConfig): Record<string, string> => {
  if (!fnConfig) {
    return {};
  }

  const { name, descriptions = '', parameters = [], examples = [] } = fnConfig;
  const nameKey = name.replace(/\(\S*\)/g, '');
  const temp: Record<string, string> = {
    [nameKey]: descriptions,
    examples: JSON.stringify(examples),
    name
  };

  if (parameters && typeof parameters === 'object') {
    parameters.reduce((prev, curv) => {
      if (curv.name) {
        prev[curv.name] = curv.descriptions;
      }
      return prev;
    }, temp);
  }

  return temp;
};

// 字符串内容使用span标签包裹
export const stringToDocumentFragment = (str: string, value: Record<string, string>): (HTMLElement|string)[] => {
  if (!str?.length) {
    return [];
  }

  const fnNameString = str.match(/\S+\(/g);
  if (!fnNameString) {
    return [];
  }

  const fnName = fnNameString[0]?.replace(/\(/, '');
  const firstChild = document.createElement('span');
  // firstChild.setAttribute('data-value', value[fnName]);
  // firstChild.setAttribute('data-examples', value.examples);
  firstChild.setAttribute('data-name', fnName);
  firstChild.setAttribute('class', 'fn-tooltip');
  firstChild.setAttribute('contenteditable', 'false');
  firstChild.innerHTML = fnName;
  // firstChild.innerHTML = fnName;
  // firstChild.innerHTML = str.replace('@', '');
  // const secondChild = document.createElement('span');
  // secondChild.innerHTML = '(';
  const nodes = [firstChild, '('];
  // return [firstChild];
  // 取到所有的参数
  const paramstr = str.replace(/\S+\(/g, '').replace(/\)/, '');
  if (paramstr?.length) {
    const params = paramstr.split(',');
    if (params?.length) {
      params.reduce((prev, curv) => {
        const temp = document.createElement('span');
        temp.setAttribute('class', 'parameter-tooltip');
        temp.setAttribute('contenteditable', 'false');
        temp.innerHTML = curv;
        prev.push(temp);
        // const temp1 = document.createElement('span');
        // temp1.innerHTML = ',';
        prev.push(',');
        return prev;
      }, nodes);
      // 删除参数的最后一个逗号
      nodes.splice(nodes.length - 1, 1);
    }
  }
  nodes.push(')');
  return nodes;
};

export const stringToDomFragment = (value: Record<string, string>): HTMLElement => {
  value = setNode(value);
  const fnName = value.name.replace('()', '');
  const firstChild = document.createElement('span');
  firstChild.setAttribute('data-name', value.name);
  firstChild.setAttribute('class', 'fn-tooltip');
  firstChild.setAttribute('contenteditable', 'false');
  firstChild.innerHTML = fnName;
  return firstChild;
};

// 在指定位置插入
const insertHtmlAtCaret = (parentNode: HTMLElement, fnConfig: FunctionConfig, keyword):void => {
  if (!(parentNode instanceof HTMLElement)) {
    return;
  }

  // 自动获取焦点
  parentNode.focus();

  // const pNodeRange = window.getSelection().getRangeAt(0);

  // const sel = window.getSelection();
  // sel?.getRangeAt(0);

  if (!window.getSelection) {
    return;
  }

  const selection:Selection|null = window.getSelection();
  if (!selection) {
    return;
  }

  if (!selection.getRangeAt || !selection.rangeCount) {
    return;
  }

  const nodes = stringToDocumentFragment(fnConfig.name, setNode(fnConfig));
  const frag = document.createDocumentFragment();
  nodes.every(node => {
    frag.append(node);
    return true;
  });

  const range: Range = selection.getRangeAt(0);

  const pNode = range.endContainer;

  range.setStart(pNode, range.endOffset - 1 - (keyword?.length || 0));
  range.deleteContents();
  range.insertNode(frag);

  if (!nodes?.length) {
    return;
  }

  // 插入的函数有参数，则自动选择第一个参数
  if (nodes.length > 3) {
    setRange(range, selection, nodes, true);
    return;
  }

  // 插入的函数没有传入参数，则把选中范围设置为光标
  setRange(range, selection, nodes, false);
};

// 设置选中范围，isRange = false 设置为光标，isRange = true，设置为选中一段区域
const setRange = (range:Range, selection:Selection, nodes:Node[], isRange:boolean) => {
  range = range.cloneRange();
  if (isRange) {
    const firstParameterNode = nodes.find((item) => (item as HTMLElement).className === 'parameter-tooltip');
    if (!firstParameterNode) {
      return;
    }

    // 用于将某个节点的起始位置指定为Range对象所代表区域的起点位置
    range.setStartBefore(firstParameterNode);
    // 用于将某个节点的终点位置指定为Range对象所代表区域的终点位置
    range.setEndAfter(firstParameterNode);
    selection.removeAllRanges();
    selection.addRange(range);
    return;
  }

  const nodeEle = nodes.filter(i => typeof i !== 'string');
  range.setStartBefore(nodeEle[nodeEle.length - 1]);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
};

type Fn = (value:CallbackValue, node:HTMLElement)=>void
type PureFn = ()=>void

const mouseoutHandle = (callback:PureFn) => {
  return function (evt) {
    evt.stopPropagation();
    const _node = evt.target;
    const className = _node?.className;
    if (className === 'fn-tooltip' || 'content-wrapper') {
      callback();
    }
  };
};

const mouseoverHandle = (callback:Fn) => {
  return function (evt) {
    evt.stopPropagation();
    const _node = evt.target;
    const className = _node?.classList?.value || '';
    if (className.includes('fn-tooltip')) {
      console.log(evt, 'className');
      const name = _node?.getAttribute('data-name');
      const temp = {
        name: undefined
      };
      if (name?.length) {
        temp.name = name;
      }
      callback(temp, _node);
    }
  };
};
// parentNode - 插入节点的父元素 ，
const init = (parentNode:HTMLElement, mouseoverCallback:Fn, mouseoutCallback:PureFn):void => {
  // 添加监听事件
  if (!(parentNode instanceof HTMLElement)) {
    return;
  }

  parentNode.removeEventListener('mouseover', mouseoverHandle(mouseoverCallback));
  parentNode.addEventListener('mouseover', mouseoverHandle(mouseoverCallback));

  parentNode.removeEventListener('mouseout', mouseoutHandle(mouseoutCallback));
  parentNode.addEventListener('mouseout', mouseoutHandle(mouseoutCallback));
};

function pasteHtmlAtCaret (html:string):void {
  let sel: Selection | null;
  let range: Range;
  if (window.getSelection) {
    sel = window.getSelection();
    if (sel?.getRangeAt && sel.rangeCount) {
      range = sel.getRangeAt(0);
      range.deleteContents();

      const el = document.createElement('div');
      el.innerHTML = html;
      const frag = document.createDocumentFragment();
      let node: ChildNode | null;
      let lastNode: ChildNode;
      while ((node = el.firstChild)) {
        lastNode = frag.appendChild(node);
      }

      range.insertNode(frag);

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      if (lastNode) {
        range = range.cloneRange();
        range.setStartAfter(lastNode);
        range.collapse(true);
        sel.removeAllRanges();
        sel.addRange(range);
      }
    }
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
  } else if (document.selection && document.selection.type !== 'Control') {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    document.selection.createRange().pasteHTML(html);
  }
}

export { init, insertHtmlAtCaret, pasteHtmlAtCaret };

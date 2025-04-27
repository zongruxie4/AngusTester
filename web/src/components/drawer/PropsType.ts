export interface NavItem {
  icon: string,
  name: string,
  value: string,
  disabled?: boolean,
  auth?: string
}

// 这是数据示例
export const navs: NavItem[] = [
  {
    icon: 'icon-fuwuzhibiao',
    name: '接口性能指标',
    value: '1'
  },
  {
    icon: 'icon-bianliang',
    name: '变量',
    value: '2'
  }
];


export interface ListType{
  targetId:string,
  apiIds?: string[],
  expiredDuration:{
    value: string,
    unit: {
      value: string,
      message
    }
  },
  passd:string,
  url?:string,
  id:string,
  editPassd?: boolean,
  tempPass?: string,
  publicFlag: boolean,
  expiredFlag?: boolean,
  targetType: 'PROJECT' | 'SERVICE' | 'API',
  remark?: string
}

export interface StateType{
  visible:boolean,
  list:Array<ListType>
}

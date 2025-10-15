
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
  password:string,
  url?:string,
  id:string,
  editPassd?: boolean,
  tempPass?: string,
  public0: boolean,
  expiredFlag?: boolean,
  targetType: 'PROJECT' | 'SERVICE' | 'API',
  remark?: string
}

export interface StateType{
  visible:boolean,
  list:Array<ListType>
}

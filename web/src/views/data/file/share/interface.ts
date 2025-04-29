
// export apis StateType{
//   form:{
//     expiredDay:number,
//     password:string,
//     public0:boolean,
//     url:string,
//     targetType:string,
//     uri:string,
//     remark?: string
//   },
//   enums:Array<{value: boolean, label: string}>
// }

export interface ListType{
  expiredDuration: {
    value: string,
    unit: {
      value: string,
      message: string
    }
  },
  password?: string,
  public0: boolean,
  url: string,
  remark?: string,
  id: string,
  editPassd?: boolean,
  tempPass?: string,
  expiredFlag?: boolean,
  objectIds?: string[]
}

export interface StateType{
  visible:boolean,
  list:Array<ListType>
}

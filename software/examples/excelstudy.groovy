// Both xls and xlsx files are supported.
excelstudy{

    excelfile 'filename.xls'

    sheetlist() // returns sheetName list

    getheader('sheetName') // returns header list

    getdata('sheetName') // return 2D data with header

    getdata('sheetName',header=true) // return 2D data with header selection

    getdata('sheetName',hader=true,[0,1,2]) // column filter

    getdata('sheetName',hader=true,[0,1]) // column filter

    getdata('sheetName',["col_1","col_2"]) // column selection

    getdata ('sheetName', ["col_1",1,2]) // support for both string and integer

    getdatamap('sheetName') // return map
}

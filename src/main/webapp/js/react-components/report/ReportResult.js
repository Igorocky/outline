const ReportResult = ({columns, data, actions}) => {

    function renderCell(column, row, actions) {
        if (column.renderFunction) {
            return actions[column.renderFunction](
                ...(!column.renderFunctionArgs?[]:column.renderFunctionArgs.map(colName => row[colName]))
            )
        } else if (column.componentName) {
            return re(window[column.componentName], {
                cellData:row[column.name], componentConfig:column.componentConfig, actions:actions
            })
        } else {
            return row[column.name]
        }
    }

    return paper(RE.Table({size:"small"},
        RE.TableHead({},
            RE.TableRow({},
                columns.map(column => RE.TableCell({key:column.name}, column.title!=null?column.title:column.name))
            )
        ),
        RE.TableBody({},
            data.map((row,idx) => RE.TableRow({key:idx, className:"grey-background-on-hover"},
                columns.map(column => RE.TableCell({key:column.name},
                    renderCell(column, row, actions)
                ))
            ))
        )
    ))
}
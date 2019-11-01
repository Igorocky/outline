const ReportResult = ({columns, data, actions}) => {
    return paper(RE.Table({size:"small"},
        RE.TableHead({},
            RE.TableRow({},
                columns.map(column => RE.TableCell({key:column.name}, column.title))
            )
        ),
        RE.TableBody({},
            data.map((row,idx) => RE.TableRow({key:idx, className:"grey-background-on-hover"},
                columns.map(column => RE.TableCell({key:column.name},
                    column.componentName
                        ?re(window[column.componentName], {
                            cellData:row[column.name], componentConfig:column.componentConfig, actions:actions})
                        :row[column.name]
                ))
            ))
        )
    ))
}
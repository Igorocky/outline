const COMPONENTS = {
    "TimestampFromInstant": TimestampFromInstant,
    "BooleanPassFail": BooleanPassFail,
}

const ReportResult = ({columns, data}) => {
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
                        ?re(COMPONENTS[column.componentName], {cellData:row[column.name]})
                        :row[column.name]
                ))
            ))
        )
    ))
}
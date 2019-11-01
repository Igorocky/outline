const ReportResult = ({columns, data}) => {
    return paper(RE.Table({size:"small"},
        RE.TableHead({},
            RE.TableRow({},
                columns.map(column => RE.TableCell({key:column.name}, column.title))
            )
        ),
        RE.TableBody({},
            data.map((row,idx) => RE.TableRow({key:idx},
                columns.map(column => RE.TableCell({key:column.name}, row[column.name]))
            ))
        )
    ))
}
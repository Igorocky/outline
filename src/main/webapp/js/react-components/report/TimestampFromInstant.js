function TimestampFromInstant({cellData}) {
    return RE.span({}, cellData?(new Date(cellData).toLocaleString()):"")
}
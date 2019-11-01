const TimestampFromInstant = ({cellData}) => {
    return RE.span({}, new Date(cellData).toLocaleString())
}
const TextShortView = ({id, text, onChanged}) => {
    return paper(re(TextNodeEditable,
        {
            value:text,
            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
            onSave: ({newValue, onSaved}) => updateTextNodeText(id, newValue, onSuccess => {
                onSaved()
                onChanged()
            })
        }
    ))
}
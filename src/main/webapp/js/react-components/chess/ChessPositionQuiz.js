'use strict';

const ChessPositionQuiz = ({cards}) => {
    const [rndElemSelector] = useState(() => getNewRndElemSelector())
    const [commandStr, setCommandStr] = useState(null)
    const [userInputIsCorrect, setUserInputIsCorrect] = useState(true)

    function getNewRndElemSelector() {
        return new RandomElemSelector({elems: cards})
    }

    function onKeyDown(event) {
        if (event.keyCode == ENTER_KEY_CODE){
            if ("?" == commandStr) {
                setCommandStr(rndElemSelector.getCurrentElem().answer.join(""))
            } else if (isUserInputCorrect(commandStr)) {
                setUserInputIsCorrect(true)
                rndElemSelector.loadNextElem()
                setCommandStr(null)
            } else {
                setUserInputIsCorrect(false)
            }
        }
    }

    function renderTextField() {
        return re('input', {
            type:"text",
            style: {fontSize: "20px", width:"300px", border:userInputIsCorrect?"":"5px solid red"},
            onKeyDown: onKeyDown,
            value: commandStr?commandStr:"",
            variant: "outlined",
            onChange: e => setCommandStr(e.target.value)
        })
    }

    function isUserInputCorrect(userInput) {
        return userInput && compareArrays(
            rndElemSelector.getCurrentElem().answer.map(e => e.toUpperCase()),
            userInput.split(/(?<=\d)(?=\w)/).map(e => e.toUpperCase())
        )
    }

    function compareArrays(expected, actual) {
        if (expected.length != actual.length) {
            return false
        }
        for (let i = 0; i < actual.length; i++) {
            if (!expected.includes(actual[i])) {
                return false
            }
        }
        return true
    }

    return RE.Container.col.top.left({},{},
        RE.Container.row.left.top({},{style:{marginRight:"20px"}},
            RE.span({style:{display: "inline-block", width: "30px"}},rndElemSelector.getCurrentElem().question),
            renderTextField(),
        ),
        RE.Container.row.left.top({},{style:{marginRight:"20px"}},
            "Iteration: " + rndElemSelector.getIterationNumber(),
            "Remaining elements: " + rndElemSelector.getRemainingElements(),
        ),
    )
}


let NEXT_BTN_ID = "next-btn";
let USER_INPUT_ID = "user-input";
let BASIC_FORM_CONTAINER_ID = "basic-form-container";
let WORD_IN_TEXT_CONTAINER_ID = "word-in-text-container";
let TRANSCRIPTION_CONTAINER_ID = "transcription-container";
let MEANING_CONTAINER_ID = "meaning-container";
let SHOW_MEANING_BTN_ID = "SHOW_MEANING_BTN_ID";
let EXAMPLES_CONTAINER_ID = "examples-container";

function initPage() {
    initTranslateSelectionButtons("translate-buttons", pageState.textLanguage);
    goToNextWord();
}

function goToNextWord() {
    doGet({
        url: "/words/engText/" + pageState.engTextId + "/wordForLearning",
        success: function (response) {
            if (response.status == "ok") {
                pageState.word = response.word;
                drawWordToLearn();
                var taskName;
                if (pageState.learnDirection) {
                    taskName = "Words: RU -> " + pageState.textLanguage + ".";
                } else {
                    taskName = "Words: " + pageState.textLanguage + " -> RU.";
                }
                $("#task-description-area").html(
                    taskName + " Groups: " + response.groups + ". Counts: " + response.counts
                );
                focusNextControl();
            }
        }
    });
}

function hasBasicForm() {
    return pageState.word.word && pageState.word.word.trim().length > 0;
}

function drawBasicForm(containerId) {
    if (hasBasicForm()) {
        let $container = $("#" + containerId);
        if (pageState.learnDirection) {
            let $input = createUserInputTextField(pageState.word.word, function () {
                $container.html($("<span/>", {text:pageState.word.word, "class": "correct-user-input"}));
                $("#" + WORD_IN_TEXT_CONTAINER_ID).html(
                    $("<span/>", {text:pageState.word.wordInText})
                );
            });
            $container.html($input);
            focusNextControl();
        } else {
            $container.html($("<span/>", {text:pageState.word.word}));
        }
    }
}

function drawWordInText(containerId) {
    let $container = $("#" + containerId);
    let $span = $("<span/>", {text:pageState.word.wordInText});
    if (pageState.learnDirection) {
        if (hasBasicForm()) {
            $container.html(
                createShowButton($container, function () {
                    return $span;
                })
            );
        } else {
            let $input = createUserInputTextField(pageState.word.wordInText, function () {
                $container.html($("<span/>", {text:pageState.word.wordInText, "class": "correct-user-input"}));
                $("#" + BASIC_FORM_CONTAINER_ID).html(
                    $("<span/>", {text:pageState.word.word})
                );
            });
            $container.html($input);
            focusNextControl();
        }
    } else {
        return $container.html($span);
    }
}

function createOpenedTranscriptionElement() {
    return $("<span/>", {text:pageState.word.transcription});
}

function drawTranscription(containerId) {
    let $container = $("#" + containerId);
    $container.html(
        createShowButton($container, function () {
            return createOpenedTranscriptionElement();
        })
    );
}

function showTranscription() {
    $("#" + TRANSCRIPTION_CONTAINER_ID).html(createOpenedTranscriptionElement());
}

function showExamples() {
    $("#" + EXAMPLES_CONTAINER_ID).html(
        composeExamples(pageState.word.wordInText, pageState.word.examples, false)
    );
}

function drawMeaning(containerId) {
    let $container = $("#" + containerId);
    if (pageState.learnDirection) {
        $container.html(composeMeaning(pageState.word.meaning));
    } else {
        $container.html(
            createShowButton(
                $container,
                function () {
                    focusNextControl();
                    return composeMeaning(pageState.word.meaning);
                },
                {
                    id: SHOW_MEANING_BTN_ID,
                    "class": "green-on-focus",
                    onclick: function () {
                        showExamples();
                        showTranscription();
                    }
                }
            )
        );
    }
}

function drawExamples(containerId) {
    let $container = $("#" + containerId);
    $container.html(createShowButton($container, function () {
        return composeExamples(pageState.word.wordInText, pageState.word.examples, pageState.learnDirection);
    }));
}

function focusNextControl() {
    $("#" + NEXT_BTN_ID).focus();
    $("#" + SHOW_MEANING_BTN_ID).focus();
    $("#" + USER_INPUT_ID).focus();
}

function createUserInputTextField(correctValue, onCorrectInput) {
    let $input = $("<input/>", {"type": "text", id: USER_INPUT_ID});
    $input.keypress(function (e) {
        if (e.which == 13) {
            e.preventDefault();
            if (this.value != correctValue) {
                $("#user-input").addClass("incorrect-user-input")
            } else {
                onCorrectInput();
                showTranscription();
                showExamples();
                focusNextControl();
            }
        }
    });
    return $input;
}

function createShowButton($container, valueProducer, params) {
    let $button = $("<button/>", {"text": "Show"});
    $button.click(function (e) {
        $container.html(valueProducer());
        focusNextControl();
        if (params.onclick) {
            params.onclick();
        }
    });
    if (params) {
        if (params.id) {
            $button.prop("id", params.id)
        }
        if (params["class"]) {
            $button.prop("class", params["class"])
        }
    }
    return $button;
}

function drawWordToLearn() {
    $wordArea = $("#word-to-learn-area");
    $table = $("<table/>", {"class": "word-to-learn-table"});
    $wordArea.html($table);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"In text:"}))
            .append($("<td/>", {id: WORD_IN_TEXT_CONTAINER_ID}))
    );
    drawWordInText(WORD_IN_TEXT_CONTAINER_ID, TRANSCRIPTION_CONTAINER_ID);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Basic form:"}))
            .append($("<td/>", {id: BASIC_FORM_CONTAINER_ID}))
    );
    drawBasicForm(BASIC_FORM_CONTAINER_ID, TRANSCRIPTION_CONTAINER_ID);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Transcription:"}))
            .append($("<td/>", {id:TRANSCRIPTION_CONTAINER_ID}))
    );
    drawTranscription(TRANSCRIPTION_CONTAINER_ID);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Meaning:"}))
            .append($("<td/>", {id:MEANING_CONTAINER_ID}))
    );
    drawMeaning(MEANING_CONTAINER_ID);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Examples:"}))
            .append($("<td/>", {id:EXAMPLES_CONTAINER_ID}))
    );
    drawExamples(EXAMPLES_CONTAINER_ID);
}

function composeMeaning(meaning) {
    return strToDivs(meaning);
}

function composeExamples(currentWordInText, examples, hideWord) {
    return _.reduce(
        examples,
        function(memo, example){
            return memo.append(drawSentence(currentWordInText, example, hideWord));
        },
        $("<ul/>")
    );

}

function drawSentence(currentWordInText, sentence, hideWord) {
    return _.reduce(
        sentence,
        function(memo, token){
            return memo.append(createElemForToken(currentWordInText, token, hideWord));
        },
        $("<li/>")
    );
}

function createElemForToken(currentWordInText, token, hideWord) {
    if (token.meta) {
        return $("<span/>");
    }
    let isCurrentWord = currentWordInText === token.value;
    let $currentWordSpan = $("<span/>", {
        text:token.value,
        "class": (isCurrentWord)?"word-selected-group":""
    });
    if (isCurrentWord && hideWord) {
        $container = $("<span/>");
        $container.html(
            createShowButton($container, function () {
                return $currentWordSpan;
            })
        );
        return $container;
    } else {
        return $currentWordSpan;
    }
}

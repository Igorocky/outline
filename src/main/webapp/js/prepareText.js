let USE_MOCK_RESPONSE = true;
let ONLY_WORDS_TO_LEARN = "onlyWordsToLearn";
let TEXT_TITLE = "text-title";
let MAIN_TEXT_AREA = "main-text-area";
let WORDS_TO_LEARN_TABLE = "words-to-learn-table";
let SENTENCES_TABLE = "sentences-table";
let WORD_TO_LEARN = "word-to-learn";
let WORD_IGNORED = "word-ignored";
let IGNORE_LIST_TEXT_AREA = "ignore-list-text-area";
let LEARN_MODE_SELECT = "learn-mode-select";

function initPage() {
    initTextTitle(textDataJson);
    initMainTextArea(textDataJson);
    initWordsToLearnTable(textDataJson);
    initLearnModeSelect(textDataJson);
    initSentencesTable(textDataJson);
    initIgnoreListTextArea(textDataJson);
}

function initTextTitle(textDataJson) {
    editableTextFieldReadMode(TEXT_TITLE, textDataJson.title, function (newValue, respHandler) {
        console.log("newValue = '" + newValue + "'");
        prepareTextPageEndpoints.changeTitle(newValue, respHandler);
    })
}

function initMainTextArea(textDataJson) {
    editableTextAreaReadMode(MAIN_TEXT_AREA, textDataJson.text, function (newValue, respHandler) {
        console.log("newValue = '" + newValue + "'");
        prepareTextPageEndpoints.changeText(newValue, respHandler);
    })
}

function initIgnoreListTextArea(textDataJson) {
    editableTextAreaReadMode(IGNORE_LIST_TEXT_AREA, textDataJson.ignoreList, function (newValue, respHandler) {
        console.log("newValue = '" + newValue + "'");
        prepareTextPageEndpoints.changeText(newValue, function (response) {
            respHandler(response);
            ignoreListChangeHandler();
        });
    })
}

function initWordsToLearnTable(textDataJson) {
    _.each(
        textDataJson.wordsToLearn,
        function (word) {
            appendWordToLearn(word);
        }
    )
}

function createWordForSentenceSpan(textDataJson, wordOfSentence) {
    var wordClass = "";
    if (wordOfSentence.isWordToLearn) {
        wordClass += " " + WORD_TO_LEARN;
    }
    if (wordOfSentence.isIgnored) {
        wordClass += " " + WORD_IGNORED;
    }
    return $("<span/>", {'class': wordClass, text: wordOfSentence.word});
}

function initLearnModeSelect(textDataJson) {
    let onlyWordsToLearn = (textDataJson.learnMode == ONLY_WORDS_TO_LEARN);
    $("#" + LEARN_MODE_SELECT).html(
        $("<select/>").html(
            $("<option/>", {value: ONLY_WORDS_TO_LEARN, text: "Only words to learn", selected: onlyWordsToLearn})
        ).append(
            $("<option/>", {value: "ignoreList", text: "Ignore list", selected: !onlyWordsToLearn})
        ).change(function (event) {
            // console.log("event = '" + JSON.stringify(event) + "'");
            let newSelectValue = $( "#" + LEARN_MODE_SELECT + " option:selected" ).val();
            console.log("newSelectValue = '" + JSON.stringify(newSelectValue) + "'");
            prepareTextPageEndpoints.changeLearnMode(newSelectValue, function () {
                ignoreListChangeHandler();
            });
        })
    )
}

function initSentencesTable(textDataJson) {
    let $sentencesTable = $("#" + SENTENCES_TABLE);
    $sentencesTable.html("");
    _.each(
        textDataJson.sentences,
        function (sentence) {
            $sentencesTable.append(
                $("<tr/>").html(
                    _.reduce(
                        sentence,
                        function(memo, wordOfSentence){
                            return memo.append(createWordForSentenceSpan(textDataJson, wordOfSentence));
                        },
                        $("<td/>")
                    )
                )
            );
        }
    )
}

function appendWordToIgnore(ignoreWord) {
    $("#" + IGNORE_LISTS_TABLE + " tr:nth-last-child(1)").after(
        $("<tr/>", {id: "word-" + word.id}).html(
            $("<td/>").html(
                $("<button/>", {text: "Delete"}).click(function () {
                    removeWord(word);
                })
            )
        ).append(
            $("<td/>", {id: "word-wordInText-" + word.id})
        ).append(
            $("<td/>", {id: "word-word-" + word.id})
        ).append(
            $("<td/>", {id: "word-transcription-" + word.id})
        ).append(
            $("<td/>", {id: "word-meaning-" + word.id})
        )
    );
    editableTextFieldReadMode(
        "word-wordInText-" + word.id,
        word.wordInText,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordInText(word.id, newText, respHandler)
        }
    );
    editableTextFieldReadMode(
        "word-word-" + word.id,
        word.word,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordSpelling(word.id, newText, respHandler)
        }
    );
    editableTextFieldReadMode(
        "word-transcription-" + word.id,
        word.transcription,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordTranscription(word.id, newText, respHandler)
        }
    );
    editableTextAreaReadMode(
        "word-meaning-" + word.id,
        word.meaning,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordMeaning(word.id, newText, respHandler)
        }
    );
}

function appendWordToLearn(word) {
    $("#" + WORDS_TO_LEARN_TABLE + " tr:nth-last-child(1)").after(
        $("<tr/>", {id: "word-" + word.id}).html(
            $("<td/>").html(
                $("<button/>", {text: "Delete"}).click(function () {
                    removeWord(word);
                })
            )
        ).append(
            $("<td/>", {id: "word-wordInText-" + word.id})
        ).append(
            $("<td/>", {id: "word-word-" + word.id})
        ).append(
            $("<td/>", {id: "word-transcription-" + word.id})
        ).append(
            $("<td/>", {id: "word-meaning-" + word.id})
        )
    );
    editableTextFieldReadMode(
        "word-wordInText-" + word.id,
        word.wordInText,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordInText(word.id, newText, respHandler)
        }
    );
    editableTextFieldReadMode(
        "word-word-" + word.id,
        word.word,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordSpelling(word.id, newText, respHandler)
        }
    );
    editableTextFieldReadMode(
        "word-transcription-" + word.id,
        word.transcription,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordTranscription(word.id, newText, respHandler)
        }
    );
    editableTextAreaReadMode(
        "word-meaning-" + word.id,
        word.meaning,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordMeaning(word.id, newText, respHandler)
        }
    );
}

function editableTextFieldReadMode(contId, value, onEditDone) {
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Edit"}).click(function () {
            editableTextFieldWriteMode(contId, value, onEditDone);
        })
    ).append(
        $("<span/>", {text: value})
    );
}

function editableTextFieldWriteMode(contId, value, onEditDone) {
    onSave = function () {
        onEditDone(
            $("#" + contId + " input").val(),
            function (resp) {
                if (resp.status == "ok") {
                    editableTextFieldReadMode(contId, resp.value, onEditDone);
                } else {
                    editableTextFieldWriteMode(contId, resp.value, onEditDone);
                }
            }
        );
    };
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Save"}).click(function () {
            onSave();
        })
    ).append(
        $("<input/>", {type:"text", size:"30", value: value})
            .keypress(function (event) {
                let keycode = (event.keyCode ? event.keyCode : event.which);
                if (keycode == '13') {
                    onSave();
                }
            })
    ).append(
        $("<button/>", {text: "Cancel"}).click(function () {
            editableTextFieldReadMode(contId, value, onEditDone);
        })
    );
    $("#" + contId + " input").focus();
}

function editableTextAreaReadMode(contId, value, onEditDone) {
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Edit"}).click(function () {
            editableTextAreaWriteMode(contId, value, onEditDone);
        })
    );
    _.reduce(
        value.split("\n"),
        function(memo, line){
            return memo.append($("<div/>", {text: line}));
        },
        $cont
    )
}

function editableTextAreaWriteMode(contId, value, onEditDone) {
    onSave = function () {
        onEditDone(
            $("#" + contId + " textarea").val(),
            function (resp) {
                if (resp.status == "ok") {
                    editableTextAreaReadMode(contId, resp.value, onEditDone);
                } else {
                    editableTextAreaWriteMode(contId, resp.value, onEditDone);
                }
            }
        );
    };
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Save"}).click(function () {
            onSave();
        })
    ).append(
        $("<button/>", {text: "Cancel"}).click(function () {
            editableTextAreaReadMode(contId, value, onEditDone);
        })
    ).append(
        $("<textarea/>", {cols:"80", rows:"10", text: value})
    );
    $("#" + contId + " textarea").focus();
}

function translateSelection() {
    // var editor = document.getElementById(MAIN_TEXT_AREA);
    // var editorHTML = $("#" + MAIN_TEXT_AREA).val();
    // var selectionStart = 0;
    // var selectionEnd = 0;
    // if (editor.selectionStart) selectionStart = editor.selectionStart;
    // if (editor.selectionEnd) selectionEnd = editor.selectionEnd;
    // if (selectionStart != selectionEnd) {
    //     let editorCharArray = editorHTML.split("");
    //     let selection = editorCharArray.slice(selectionStart, selectionEnd).join("");
    //     // let urlPrefix = "https://translate.google.ru/#en/ru/";
    //     let urlPrefix = "https://www.lingvolive.com/ru-ru/translate/en-ru/";
    //     window.open(urlPrefix + selection, '_blank');
    // }
    // $("#" + MAIN_TEXT_AREA).focus();

    // let urlPrefix = "https://translate.google.ru/#en/ru/";
    let urlPrefix = "https://www.lingvolive.com/ru-ru/translate/en-ru/";
    window.open(urlPrefix + window.getSelection().toString(), '_blank');
}

function saveSelectedWord() {
    prepareTextPageEndpoints.createNewWord(window.getSelection().toString());
}

function ignoreListChangeHandler() {
    prepareTextPageEndpoints.getEngText(textDataJson.textId, function (data) {
        console.log("IgnoreListChange.");
        initLearnModeSelect(textDataJson);
        initSentencesTable(textDataJson);
        initIgnoreListTextArea(textDataJson);
    })
}

function createIgnoreListChangeHandler() {
    return function () {
        ignoreListChangeHandler();
    }
}

function ignoreSelectedWord() {
    prepareTextPageEndpoints.ignoreWord(window.getSelection().toString(), createIgnoreListChangeHandler());
}

function unignoreSelectedWord() {
    prepareTextPageEndpoints.unignoreWord(window.getSelection().toString(), createIgnoreListChangeHandler());
}

function removeWord(word) {
    confirmCancelDialog("dialog-confirm", "Delete word '" + word.wordInText + "'?", "Delete", function () {
        prepareTextPageEndpoints.removeWord(word);
    });
}

function doPostWithMock(useMockResponse, params, mockResponseGenerator) {
    if (useMockResponse) {
        console.log("Mocking post start>  params: " + JSON.stringify(params));
        let response = mockResponseGenerator(params.data);
        console.log("Mocking post end> returning: " + JSON.stringify(response));
        params.success(response);
    } else {
        return doPost(params);
    }
}

function doGetWithMock(useMockResponse, params, mockResponseGenerator) {
    if (useMockResponse) {
        console.log("Mocking get start>  params: " + JSON.stringify(params));
        let response = mockResponseGenerator(params.url);
        console.log("Mocking get end> returning: " + JSON.stringify(response));
        params.success(response);
    } else {
        return doGet(params);
    }
}

let prepareTextPageEndpoints = {
    changeTitle: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-title", newText, respHandler);
    },
    changeText: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-text", newText, respHandler);
    },
    changeIgnoreList: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-ignore-list", newText, respHandler);
    },
    changeWordInText: function (wordId, newText, respHandler) {
        changeAttrValueEndpoint(wordId, "eng-text-word-wordInText", newText, respHandler);
    },
    changeWordSpelling: function (wordId, newText, respHandler) {
        changeAttrValueEndpoint(wordId, "eng-text-word-spelling", newText, respHandler);
    },
    changeWordTranscription: function (wordId, newText, respHandler) {
        changeAttrValueEndpoint(wordId, "eng-text-word-transcription", newText, respHandler);
    },
    changeWordMeaning: function (wordId, newText, respHandler) {
        changeAttrValueEndpoint(wordId, "eng-text-word-meaning", newText, respHandler);
    },
    changeLearnMode: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-learn-mode", newText, respHandler);
    },
    createNewWord: function (spelling) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/createWord",
                data: {
                    engTextId: textDataJson.textId,
                    word: {
                        wordInText: spelling.trim(),
                        word: "",
                        transcription: "",
                        meaning: ""
                    }
                },
                success: function (response) {
                    if (response.status == "ok") {
                        appendWordToLearn(response.word);
                    }
                }
            },
            function (params) {
                return {status: "ok", word: Object.assign({id: generateNextId()}, params.word)};
            }
        );
    },
    removeWord: function (word) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/removeWord",
                data: {engTextId: textDataJson.textId, wordId: word.id},
                success: function (response) {
                    if (response.status == "ok") {
                        $("#" + "word-" + word.id).remove();
                    }
                }
            },
            function (params) {
                return {status: "ok"};
            }
        );
    },
    ignoreWord: function (spelling, onSuccess) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/ignoreWord",
                data: {engTextId: textDataJson.textId, spelling: spelling},
                success: function (response) {
                    if (response.status == "ok") {
                        console.log("word was ignored.");
                        onSuccess();
                    }
                }
            },
            function (params) {
                return {status: "ok"};
            }
        );
    },
    unignoreWord: function (spelling, onSuccess) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/unignoreWord",
                data: {engTextId: textDataJson.textId, spelling: spelling},
                success: function (response) {
                    if (response.status == "ok") {
                        onSuccess();
                    }
                }
            },
            function (params) {
                return {status: "ok"};
            }
        );
    },
    getEngText: function (textId, onDataRetrieved) {
        doGetWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/engText/" + textId,
                success: function (response) {
                    if (response.status == "ok") {
                        response.engText.ignoreListArr = response.engText.ignoreList.split(/\r?\n/);
                        textDataJson = response.engText;
                        onDataRetrieved(response.engText);
                    }
                }
            },
            function (url) {
                return mockTextDataJson(_.last(url.slice("/")));
            }
        );
    }
};

function changeAttrValueEndpoint(objId, attrName, newValue, respHandler) {
    doPostWithMock(
        USE_MOCK_RESPONSE,
        {
            url: "/changeAttrValue",
            data: {objId: objId, attrName: attrName, value: newValue},
            success: function (response) {
                if (response.status == "ok") {
                    respHandler({status: "ok", value: response.value})
                }
            }
        },
        function (params) {
            return {status: "ok", value: params.value};
        }
    );
}

function generateNextId() {
    return "id_" + (textDataJson.lastId++);
}

function createWordForSentence(textDataJson, wordSpelling) {
    let isWordToLearn = _.find(textDataJson.wordsToLearn, function (word) {
        return word.wordInText == wordSpelling;
    }) != undefined;
    var isIgnored = false;
    if (textDataJson.learnMode == ONLY_WORDS_TO_LEARN) {
        if (!isWordToLearn) {
            isIgnored = true;
        }
    } else {
        isIgnored = textDataJson.ignoreListArr.includes(wordSpelling);
    }
    return {word: wordSpelling, isWordToLearn: isWordToLearn, isIgnored: isIgnored};
}


function mockTextDataJson(engTextId) {
    let ignoreList = "the\nso";
    let res1 = {
        textId: engTextId,
        title: "Some title",
        text: "So far, Hulu has been the only streaming service available in North America on the Switch, leaving Netflix, Amazon, and others out in the cold. Its 6.2-inch 720p screen is very capable for both gaming and streaming, so users have been itching for more things to do with it.",
        wordsToLearn: [
            {id: "a97f721f-d5eb-4a37-8f16-8a818d25a6fd", wordInText: "been", word: "been", transcription: "ssdsdsd", meaning: "asda sasd asd a sd"},
            {id: "b97f721f-d5eb-4a37-8f16-8a818d25a6fd", wordInText: "only", word: "only", transcription: "uiopuio", meaning: "[as,mnbv;[[i"},
            {id: "c97f721f-d5eb-4a37-8f16-8a818d25a6fd", wordInText: "screen", word: "screen", transcription: "qwe,jp", meaning: "lkgjksve fsd sdf"}
        ],
        ignoreList: ignoreList,
        ignoreListArr: ignoreList.slice("\n"),
        learnMode: "onlyWordsToLearn",
        sentencesRow: [
            ["So", " ", "far", ",", " ", "Hulu", " ", "has", " ", "been", " ", "the", " ", "only", "."],
            ["Its", " ", "6.2-inch", " ", "720p", " ", "screen", " ", "is", " ", "very", " ", "capable", " ", "for", " ", "both", "."]
        ],
        lastId: 1000
    };
    let engText = Object.assign(res1, {
        sentences: _.map(res1.sentencesRow, function (sentence) {
            return _.map(sentence, function (wordSpelling) {
                return createWordForSentence(res1, wordSpelling);
            });
        })
    });
    return {status: "ok", engText: engText};
}
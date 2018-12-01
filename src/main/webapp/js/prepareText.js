let USE_MOCK_RESPONSE = false;
let ONLY_WORDS_TO_LEARN = "onlyWordsToLearn";
let TEXT_TITLE = "text-title";
let MAIN_TEXT_AREA = "main-text-area";
let WORDS_TO_LEARN_TABLE = "words-to-learn-table";
let WORD_IGNORED = "word-ignored";
let WORD_GENERAL = "word-general";
let WORD_TO_LEARN = "word-to-learn";
let WORD_TO_LEARN_NO_GROUP = "word-to-learn-no-group";
let WORD_SELECTED_GROUP = "word-selected-group";
let IGNORE_LIST_TEXT_AREA = "ignore-list-text-area";
let LEARN_GROUPS_SELECT = "learn-groups-select";
let LEARN_GROUP_SELECTOR = "learn-group-selector";
let MOVE_TO_GROUP_CURR_VALUE = "move-to-group-curr-value";

function initPage() {
    initTextTitle(textDataJson);
    initMainTextArea(textDataJson);
    initWordsToLearnTable(textDataJson);
    initLearnGroupsSelect();
    initIgnoreListTextArea(textDataJson);
}

function initTextTitle(textDataJson) {
    editableTextFieldReadMode(TEXT_TITLE, textDataJson.title, function (newValue, respHandler) {
        console.log("newValue = '" + newValue + "'");
        prepareTextPageEndpoints.changeTitle(newValue, respHandler);
    })
}

function initMainTextArea(textDataJson) {
    editableTextAreaReadMode(
        MAIN_TEXT_AREA,
        textDataJson,
        function (textDataJson) {
            return createSentencesTable(textDataJson);
        },
        function (textDataJson) {
            return textDataJson.text;
        },
        function (newValue, respHandler) {
            console.log("newValue = '" + newValue + "'");
            prepareTextPageEndpoints.changeText(newValue, respHandler);
        }
    )
}

function strToDivs(str) {
    return _.reduce(
        str.split("\n"),
        function(memo, line){
            return memo.append($("<div/>", {text: line}));
        },
        $("<div/>")
    );
}

function initIgnoreListTextArea(textDataJson) {
    editableTextAreaReadMode(
        IGNORE_LIST_TEXT_AREA,
        textDataJson.ignoreList,
        function (ignoreList) {
            return strToDivs(ignoreList);
        },
        function (ignoreList) {
            return ignoreList;
        },
        function (newValue, respHandler) {
            console.log("newValue = '" + newValue + "'");
            prepareTextPageEndpoints.changeIgnoreList(newValue, function (response) {
                respHandler(response);
                reloadEngText();
            });
        }
    )
}

function initWordsToLearnTable(textDataJson) {
    _.each(
        textDataJson.wordsToLearn.reverse(),
        function (word) {
            appendWordToLearn(word);
        }
    )
}

function createWordForSentenceSpan(wordOfSentence) {
    if (wordOfSentence.meta) {
        return $("<span/>");
    } else {
        var wordClass;
        if (!wordOfSentence.word) {
            wordClass = WORD_IGNORED;
        } else {
            wordClass = WORD_GENERAL;
        }
        if (wordOfSentence.wordToLearn) {
            if (wordOfSentence.doesntHaveGroup) {
                wordClass = WORD_TO_LEARN_NO_GROUP;
            } else {
                wordClass = WORD_TO_LEARN;
            }
        }
        if (wordOfSentence.selectedGroup) {
            wordClass = WORD_SELECTED_GROUP;
        }

        return $("<span/>", {'class': wordClass, text: wordOfSentence.value});
    }
}

function initLearnGroupsSelect() {
    $("#" + LEARN_GROUPS_SELECT).html(
        $("<select/>", {id:LEARN_GROUP_SELECTOR, multiple:"multiple"})
    );
    prepareTextPageEndpoints.getLearnGroupsInfo(function (learnGroupsInfo) {
        _.reduce(
            learnGroupsInfo.available,
            function(memo, available){
                return memo.append(
                    $("<option/>", {text:available, value:available})
                );
            },
            $("#" + LEARN_GROUP_SELECTOR)
        );
        _.reduce(
            learnGroupsInfo.selected,
            function(memo, selected){
                return memo.append(
                    $("<option/>", {text:selected, value:selected, selected:true})
                );
            },
            $("#" + LEARN_GROUP_SELECTOR)
        );
        onChange = function () {
            let val = $("#" + LEARN_GROUP_SELECTOR).val().toString();
            prepareTextPageEndpoints.changeLearnGroups(val.split(","))
        }
        $("#" + LEARN_GROUP_SELECTOR).multiSelect({
            selectableHeader: "<div>Available</div>",
            selectionHeader: "<div>Selected</div>",
            afterSelect: function (values) {
                onChange();
            },
            afterDeselect: function (values) {
                onChange();
            }
        });
    });
}

function createSentencesTable(textDataJson) {
    let $sentencesTable = $("<table/>", {"class": "outline-bordered-table main-text-table"});
    _.each(
        textDataJson.sentences,
        function (sentence) {
            $sentencesTable.append(
                $("<tr/>").html(
                    _.reduce(
                        sentence,
                        function(memo, wordOfSentence){
                            return memo.append(createWordForSentenceSpan(wordOfSentence));
                        },
                        $("<td/>")
                    )
                )
            );
        }
    );
    return $sentencesTable;
}

function appendWordToLearn(word) {
    $("#" + WORDS_TO_LEARN_TABLE + " tr:nth-child(1)").after(
        $("<tr/>", {id: "word-" + word.id}).html(
            $("<td/>").html(
                $("<button/>", {text: "Delete"}).click(function () {
                    removeWord(word);
                    reloadEngText();
                })
            )
        ).append(
            $("<td/>", {id: "word-group-" + word.id})
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
    editableSelectReadMode(
        "word-group-" + word.id,
        word.group,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordGroup(word.id, newText, function (resp) {
                respHandler(resp);
                reloadEngText();
            })
        },
        function (optionsLoadedHandler) {
            prepareTextPageEndpoints.getAvailableWordGroups(optionsLoadedHandler)
        }
    );
    editableTextFieldReadMode(
        "word-wordInText-" + word.id,
        word.wordInText,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordInText(word.id, newText, function (resp) {
                respHandler(resp);
                reloadEngText();
            });
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
        function (meaning) {
            return strToDivs(meaning);
        },
        function (meaning) {
            return meaning;
        },
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

function editableSelectReadMode(contId, value, onEditDone, loadOptions, params) {
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Edit"}).click(function () {
            editableSelectWriteMode(contId, value, onEditDone, loadOptions, params);
        })
    ).append(
        $("<span/>", {text: value})
    );
}

var editableSelectWriteModeId = 1;
function editableSelectWriteMode(contId, value, onEditDone, loadOptions, params) {
    onSave = function () {
        onEditDone(
            $("#" + contId + " input").val(),
            function (resp) {
                if (resp.status == "ok") {
                    editableSelectReadMode(contId, resp.value, onEditDone, loadOptions, params);
                } else {
                    editableSelectWriteMode(contId, resp.value, onEditDone, loadOptions, params);
                }
            }
        );
    };
    $cont = $("#" + contId);
    $cont.html("");
    let currId = "editableSelectWriteMode-" + editableSelectWriteModeId++;
    $cont.append(
        $("<button/>", {text: "Cancel"}).click(function () {
            if (params && params.onCancel) {
                params.onCancel();
            } else {
                editableSelectReadMode(contId, value, onEditDone, loadOptions, params);
            }
        })
    );
    $cont.append(
        $("<button/>", {text: "Save"}).click(function () {
            onSave();
        })
    );
    $cont.append(
        $((params && params.useSpan)?"<span/>":"<div/>", {'class':"select-editable"})
            .append(
                $("<select/>", {id:currId}).html(
                    $("<option/>", {value:""})
                ).change(function (event) {
                    this.nextElementSibling.value=this.value;
                    onSave();
                })
            ).append(
                $("<input/>", {type:"text", value: value})
                    .keypress(function (event) {
                        let keycode = (event.keyCode ? event.keyCode : event.which);
                        if (keycode == '13') {
                            onSave();
                        }
                    })
            )
    );
    $("#" + contId + " input").select();
    loadOptions(function (options) {
        $select = $("#" + currId);
        _.each(options, function(option) {
            $select.append(
                $("<option/>", {value:option}).html(option)
            )
        });
    })
}

function editableTextAreaReadMode(contId, value, valueView, valueEdit, onEditDone) {
    $cont = $("#" + contId);
    $cont.html(
        $("<button/>", {text: "Edit"}).click(function () {
            editableTextAreaWriteMode(contId, value, valueView, valueEdit, onEditDone);
        })
    );
    if (value) {
        $cont.append(valueView(value))
    }
}

function editableTextAreaWriteMode(contId, value, valueView, valueEdit, onEditDone) {
    onSave = function () {
        onEditDone(
            $("#" + contId + " textarea").val(),
            function (resp) {
                if (resp.status == "ok") {
                    editableTextAreaReadMode(contId, resp.value, valueView, valueEdit, onEditDone);
                } else {
                    editableTextAreaWriteMode(contId, resp.value, valueView, valueEdit, onEditDone);
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
            editableTextAreaReadMode(contId, value, valueView, valueEdit, onEditDone);
        })
    ).append(
        $("<textarea/>", {cols:"80", rows:"10", text: valueEdit(value)})
    );
    $("#" + contId + " textarea").focus();
}

function translateSelection(urlPrefix) {
    window.open(urlPrefix + window.getSelection().toString(), '_blank');
}

function saveSelectedWord() {
    prepareTextPageEndpoints.createNewWord(window.getSelection().toString());
}

function reloadEngText() {
    getEngText(textDataJson.textId, function (data) {
        console.log("reloadEngText.");
        initMainTextArea(textDataJson);
        initLearnGroupsSelect();
        initIgnoreListTextArea(textDataJson);
    })
}

function createIgnoreListChangeHandler() {
    return function () {
        reloadEngText();
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

function changeCurrentGroup() {
    let initialValue = $("#"+MOVE_TO_GROUP_CURR_VALUE).html();
    editableSelectWriteMode(
        MOVE_TO_GROUP_CURR_VALUE,
        initialValue,
        function (newText, respHandler) {
            $("#"+MOVE_TO_GROUP_CURR_VALUE).html(newText);
        },
        function (optionsLoadedHandler) {
            prepareTextPageEndpoints.getAvailableWordGroups(optionsLoadedHandler)
        },
        {
            onCancel: function () {
                $("#"+MOVE_TO_GROUP_CURR_VALUE).html(initialValue);
            },
            useSpan: true
        }
    );
}

function moveSelectedWordToGroup() {
    let selection = window.getSelection().toString();
    if (selection) {
        let wordInText = selection.trim();
        let word = _.find(textDataJson.wordsToLearn, function (word) {
            return word.wordInText === wordInText;
        });
        if (word) {
            let groupName = $("#"+MOVE_TO_GROUP_CURR_VALUE).html();
            prepareTextPageEndpoints.changeWordGroup(word.id, groupName, function (resp) {
                reloadEngText();
            })
        }
    }
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
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "/changeAttrValue",
                data: {objId: textDataJson.textId, attrName: "eng-text-text", value: newText},
                success: function (response) {
                    if (response.status == "ok") {
                        getEngText(textDataJson.textId, function (textDataJson) {
                            respHandler({status: "ok", value: textDataJson})
                        })
                    }
                }
            },
            function (params) {

            }
        );
    },
    changeIgnoreList: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-ignore-list", newText, respHandler);
    },
    changeWordGroup: function (wordId, newText, respHandler) {
        changeAttrValueEndpoint(wordId, "eng-text-word-group", newText, respHandler);
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
    changeLearnGroups: function (newLearnGroups) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "changeLearnGroups/" + textDataJson.textId,
                data: newLearnGroups,
                success: function (response) {
                    if (response.status == "ok") {
                        reloadEngText();
                    }
                }
            },
            function (params) {

            }
        );
    },
    createNewWord: function (spelling) {
        doPostWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "createWord",
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
                        reloadEngText();
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
                url: "removeWord",
                data: {engTextId: textDataJson.textId, wordId: word.id},
                success: function (response) {
                    if (response.status == "ok") {
                        $("#" + "word-" + word.id).remove();
                        reloadEngText();
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
                url: "ignoreWord",
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
                url: "unignoreWord",
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
    getAvailableWordGroups: function (onDataRetrieved) {
        doGetWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "engText/availableWordGroups/" + textDataJson.textId,
                success: function (response) {
                    if (response.status == "ok") {
                        onDataRetrieved(response.availableWordGroups);
                    }
                }
            },
            function (url) {
                return {
                    status: "ok",
                    availableWordGroups: ["G1", "G2", "Ggg"]
                };
            }
        );
    },
    getLearnGroupsInfo: function (onDataRetrieved) {
        doGetWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "engText/learnGroupsInfo/" + textDataJson.textId,
                success: function (response) {
                    if (response.status == "ok") {
                        onDataRetrieved(response);
                    }
                }
            },
            function (url) {
                return {
                    status: "ok",
                    available: ["G1", "G2", "Ggg"],
                    selected: ["G1", "G2", "Ggg"]
                };
            }
        );
    }
};

function getEngText(textId, onDataRetrieved) {
    doGetWithMock(
        USE_MOCK_RESPONSE,
        {
            url: "engText/" + textId,
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
    if (textDataJson.learnGroup == ONLY_WORDS_TO_LEARN) {
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
            {id: "a97f721f-d5eb-4a37-8f16-8a818d25a6fd", group: "A", wordInText: "been", word: "been", transcription: "ssdsdsd", meaning: "asda sasd asd a sd"},
            {id: "b97f721f-d5eb-4a37-8f16-8a818d25a6fd", group: "B", wordInText: "only", word: "only", transcription: "uiopuio", meaning: "[as,mnbv;[[i"},
            {id: "c97f721f-d5eb-4a37-8f16-8a818d25a6fd", group: "C", wordInText: "screen", word: "screen", transcription: "qwe,jp", meaning: "lkgjksve fsd sdf"}
        ],
        ignoreList: ignoreList,
        ignoreListArr: ignoreList.slice("\n"),
        learnGroup: "onlyWordsToLearn",
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
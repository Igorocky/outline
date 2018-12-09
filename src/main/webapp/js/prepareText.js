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

function initPage() {
    initTextTitle(textDataJson);
    initProps();
    initLearnGroupsSelect();
    if (isFullMode()) {
        initTranslateSelectionButtons(
            "translate-buttons",
            textDataJson.language,
            function (urlPrefix) {
                translateSelection(urlPrefix);
            }
        );
        initTranslateSelectionButtons(
            "translate-buttons-for-words-table",
            textDataJson.language,
            function (urlPrefix) {
                translateSelection(urlPrefix);
            }
        );
        initMainTextArea(textDataJson);
        initWordsToLearnTable(textDataJson);
        initIgnoreListTextArea(textDataJson);
    } else {
        initExerciseSelector();
    }
}

function initProps() {
    editableSelectReadMode(
        "lang-span",
        textDataJson.language,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeLanguage(newText, function (resp) {
                location.reload();
            })
        },
        function (optionsLoadedHandler) {
            prepareTextPageEndpoints.getAvailableLanguages(optionsLoadedHandler)
        },
        {
            useSpan: true
        }
    );
    editableTextFieldReadMode("pct-span", textDataJson.pct, function (newValue, respHandler) {
        prepareTextPageEndpoints.changePct(newValue, respHandler);
    });
}

function initExerciseSelector() {
    $("#x-ru-link").text("Words: " + textDataJson.language + " -> RU");
    $("#ru-x-link").text("Words: RU -> " + textDataJson.language);
}

function initTextTitle(textDataJson) {
    if (isFullMode()) {
        editableTextFieldReadMode(TEXT_TITLE, textDataJson.title, function (newValue, respHandler) {
            prepareTextPageEndpoints.changeTitle(newValue, respHandler);
        });
    } else {
        $("#text-title").html(textDataJson.title);
    }
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
            prepareTextPageEndpoints.changeText(newValue, respHandler);
        }
    )
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
            prepareTextPageEndpoints.changeIgnoreList(newValue, function (response) {
                respHandler(response);
                reloadEngText();
            });
        }
    )
}

function initWordsToLearnTable(textDataJson) {
    $("#words-to-learn-table-container").html(
        $("<table/>", {"class":"outline-bordered-table"}).html(
            $("<thead/>").html(
                $("<tr/>").html("")
                    .append($("<th/>", {text: "Group"}))
                    .append($("<th/>", {text: "Word in text"}))
                    .append($("<th/>", {text: "Basic form"}))
                    .append($("<th/>", {text: "Transcription"}))
                    .append($("<th/>", {text: "Meaning"}))
                    .append($("<th/>"))
            )
        ).append($("<tbody/>", {id:WORDS_TO_LEARN_TABLE}))
    );

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
            wordClass = WORD_TO_LEARN;
        }
        if (wordOfSentence.selectedGroup) {
            wordClass = WORD_SELECTED_GROUP;
        }
        if (wordOfSentence.doesntHaveGroup) {
            wordClass = WORD_TO_LEARN_NO_GROUP;
        }

        return $("<span/>", {'class': wordClass, text: wordOfSentence.value + (wordOfSentence.wordToLearn ? " {" + wordOfSentence.group + "}" : "") });
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
        function (sentence, idx) {
            let $tr = $("<tr/>");
            $tr.append($("<td/>").html(idx + 1));
            $tr.append(
                _.reduce(
                    sentence,
                    function(memo, wordOfSentence){
                        return memo.append(createWordForSentenceSpan(wordOfSentence));
                    },
                    $("<td/>")
                )
            );
            $sentencesTable.append($tr);
        }
    );
    return $("<div/>", {"class": "main-text-table-wrapper"}).html($sentencesTable);
}

function wordInTextEditableTextField(contId, word) {
    return editableTextFieldReadModeElem(
        contId,
        word.wordInText,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordInText(word.id, newText, function (resp) {
                respHandler(resp);
                reloadEngText();
            });
        }
    )
}

function basicFormEditableTextField(contId, word) {
    return editableTextFieldReadModeElem(
        contId,
        word.word,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordSpelling(word.id, newText, respHandler)
        }
    )
}

function transcriptionEditableTextField(contId, word) {
    return editableTextFieldReadModeElem(
        contId,
        word.transcription,
        function (newText, respHandler) {
            prepareTextPageEndpoints.changeWordTranscription(word.id, newText, respHandler)
        }
    )
}

function meaningEditableTextArea(contId, word) {
    return editableTextAreaReadModeElem(
        contId,
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

function appendWordToLearn(word) {
    $("#" + WORDS_TO_LEARN_TABLE).prepend(
        $("<tr/>", {id: "word-" + word.id}).html("").append(
            $("<td/>", {id: "word-group-" + word.id})
        ).append(
            $("<td/>", {id: "word-wordInText-" + word.id}).html(
                wordInTextEditableTextField("word-wordInText-" + word.id, word)
            )
        ).append(
            $("<td/>", {id: "word-word-" + word.id}).html(
                basicFormEditableTextField("word-word-" + word.id, word)
            )
        ).append(
            $("<td/>", {id: "word-transcription-" + word.id}).html(
                transcriptionEditableTextField("word-transcription-" + word.id, word)
            )
        ).append(
            $("<td/>", {id: "word-meaning-" + word.id}).html(
                meaningEditableTextArea("word-meaning-" + word.id, word)
            )
        ).append(
            $("<td/>").html(
                $("<button/>", {text: "x"}).click(function () {
                    removeWord(word);
                    reloadEngText();
                })
            )
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
}

function editableTextFieldReadModeElem(contId, value, onEditDone) {
    return $("<table/>", {"class":"outline-noborder-table"}).html(
        $("<tr/>").html(
            $("<td/>").html(
                $("<button/>", {text: "Edit"}).click(function () {
                    editableTextFieldWriteMode(contId, value, onEditDone);
                })
            )
        ).append(
            $("<td/>").html(
                $("<span/>", {text: value})
            )
        )
    );
}

function editableTextFieldReadMode(contId, value, onEditDone) {
    $("#" + contId).html(editableTextFieldReadModeElem(contId, value, onEditDone));
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
    $("#" + contId).html(
        $("<table/>", {"class":"outline-noborder-table"}).html(
            $("<tr/>").html(
                $("<td/>").html(
                    $("<button/>", {text: "Edit"}).click(function () {
                        editableSelectWriteMode(contId, value, onEditDone, loadOptions, params);
                    })
                )
            ).append(
                $("<td/>").html(
                    $("<span/>", {text: value})
                )
            )
        )
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
        $("<button/>", {text: "Save"}).click(function () {
            onSave();
        })
    );
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

function editableTextAreaReadModeElem(contId, value, valueView, valueEdit, onEditDone) {
    $cont = $("<span/>");
    $cont.html(
        $("<button/>", {text: "Edit"}).click(function () {
            editableTextAreaWriteMode(contId, value, valueView, valueEdit, onEditDone);
        })
    );
    if (value) {
        $cont.append(valueView(value))
    }
    return $cont;
}

function editableTextAreaReadMode(contId, value, valueView, valueEdit, onEditDone) {
    $cont = $("#" + contId);
    $cont.html(
        editableTextAreaReadModeElem(contId, value, valueView, valueEdit, onEditDone)
    );
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

function saveSelectedWord() {
    var selection = window.getSelection().toString();
    if (selection && selection.trim() !== "") {
        prepareTextPageEndpoints.createNewWord(selection);
    }
}

function editSelectedWord() {
    let selectedWord = getSelectedWord();
    if (selectedWord) {
        editWord(selectedWord.id, textDataJson.language);
    }
}
function editWord(wordId, language) {
    doGet(
        {
            url: "/words/engText/word/" + wordId,
            success: function (response) {
                if (response.status == "ok") {
                    clearSelection();
                    modalDialog(
                        "dialog-modal",
                        "Edit word: " + response.word.wordInText,
                        function () {
                            $content = $("<div/>");
                            $content.append(
                                $("<div/>").html(generateTranslateSelectionButtons(
                                    language,
                                    function (urlPrefix) {
                                        const selection = getSelectedText();
                                        if (!isEmptyString(selection)) {
                                            translateSelection(urlPrefix);
                                        } else {
                                            translateText(
                                                urlPrefix,
                                                isEmptyString(response.word.word)
                                                    ?response.word.wordInText
                                                    :response.word.word
                                            );
                                        }
                                    }

                                ))
                            );
                            $table = $("<table/>", {"class": "word-to-learn-table"});
                            $content.append($table);
                            $table.append(
                                $("<tr/>")
                                    .append($("<td/>", {text:"In text:", "class": "word-to-learn-attr-name"}))
                                    .append(
                                        $("<td/>").html(
                                            response.word.wordInText
                                        )
                                    )
                            );
                            $table.append(
                                $("<tr/>")
                                    .append($("<td/>", {text:"Basic form:", "class": "word-to-learn-attr-name"}))
                                    .append(
                                        $("<td/>", {id: BASIC_FORM_CONTAINER_ID+"-prepareText"}).html(
                                            basicFormEditableTextField(BASIC_FORM_CONTAINER_ID+"-prepareText", response.word)
                                        )
                                    )
                            );
                            $table.append(
                                $("<tr/>")
                                    .append($("<td/>", {text:"Transcription:", "class": "word-to-learn-attr-name"}))
                                    .append(
                                        $("<td/>", {id:TRANSCRIPTION_CONTAINER_ID+"-prepareText"}).html(
                                            transcriptionEditableTextField(TRANSCRIPTION_CONTAINER_ID+"-prepareText", response.word)
                                        )
                                    )
                            );
                            $table.append(
                                $("<tr/>")
                                    .append($("<td/>", {text:"Meaning:", "class": "word-to-learn-attr-name"}))
                                    .append(
                                        $("<td/>", {id:MEANING_CONTAINER_ID+"-prepareText"}).html(
                                            meaningEditableTextArea(MEANING_CONTAINER_ID+"-prepareText", response.word)
                                        )
                                    )
                            );
                            $table.append(
                                $("<tr/>")
                                    .append($("<td/>", {text: "Examples:", "class": "word-to-learn-attr-name"}))
                                    .append(
                                        $("<td/>", {id: EXAMPLES_CONTAINER_ID+"-prepareText"}).html(
                                            composeExamples(response.word.wordInText, response.word.examples, false)
                                        )
                                    )
                            );
                            return $content;
                        },
                        "Close",
                        function () {},
                        {
                            onClose: function () {
                                if (typeof(textDataJson) !== 'undefined') {
                                    getEngText(textDataJson.textId, function (textDataJson) {
                                        initWordsToLearnTable(textDataJson);
                                    });
                                }
                            }
                        }
                    )
                }
            }
        }
    );
}

function isFullMode() {
    return pageMode === "full";
}

function reloadEngText() {
    if (isFullMode()) {
        getEngText(textDataJson.textId, function (data) {
            initMainTextArea(textDataJson);
            initLearnGroupsSelect();
            initIgnoreListTextArea(textDataJson);
        })
    }
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
    confirmCancelDialog(
        "dialog-confirm",
        "Delete word '" + (isEmptyString(word.word)?word.wordInText:word.word) + "'?", "Delete",
        function () {
        prepareTextPageEndpoints.removeWord(word);
    }
    );
}

function getSelectedWord() {
    let selection = getSelectedText();
    if (!isEmptyString(selection)) {
        let wordInText = selection.trim();
        return _.find(textDataJson.wordsToLearn, function (word) {
            return word.wordInText === wordInText;
        });
    }
}

function moveSelectedWordToGroup() {
    let selectedWord = getSelectedWord();
    if (selectedWord) {
        let groupName = $("#move-to-group-curr-value").val();
        prepareTextPageEndpoints.changeWordGroup(selectedWord.id, groupName, function (resp) {
            reloadEngText();
        })
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
    changePct: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-pct", newText, respHandler);
    },
    changeLanguage: function (newText, respHandler) {
        changeAttrValueEndpoint(textDataJson.textId, "eng-text-lang", newText, respHandler);
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
                        editWord(response.word.id, textDataJson.language);
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
    getAvailableLanguages: function (onDataRetrieved) {
        doGetWithMock(
            USE_MOCK_RESPONSE,
            {
                url: "availableLanguages",
                success: function (response) {
                    if (response.status == "ok") {
                        onDataRetrieved(response.languages);
                    }
                }
            },
            function (url) {
                return {
                    status: "ok",
                    languages: ["EN", "PL"]
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
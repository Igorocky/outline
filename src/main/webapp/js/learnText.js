let SHOWN = "shown";

function initPage() {
    initTranslateSelectionButtons("translate-buttons", pageState.textLanguage);
    $("#go-to-sentence-number").keydown(function(e){
        if (e.which == 13) {
            goToNextSentenceByNumber();
        }
    });
    if (pageState.nextSentenceMode === 'random') {
        goToSentence("");
    } else {
        goToSentence(pageState.sentenceIdx);
    }
}

function goToSentence(sentenceIdx) {
    $("#show-sentence-area").html("");
    doGet({
        url: "/words/engText/" + pageState.engTextId + "/sentenceForLearning?sentenceIdx=" + sentenceIdx,
        success: function (response) {
            if (response.status == "ok") {
                pageState.maxSentenceIdx = response.maxSentenceIdx;
                if (!response.sentence) {
                    infoDialog(
                        "dialog-confirm",
                        "No sentence available. Max sentence number = " + (response.maxSentenceIdx + 1),
                        "OK",
                        function () {
                            $("#first-sentence-btn").focus();
                        }
                    );
                } else {
                    pageState.sentenceIdx = response.sentenceIdx;
                    pageState.sentence = response.sentence;
                    pageState.taskDescription = response.taskDescription;
                    pageState.countsBefore = response.countsBefore;
                    pageState.countsAfter = response.countsAfter;
                    drawSentenceToLearn();
                }
            }
        }
    });
}

function goToPrevSentence() {
    goToSentence(pageState.sentenceIdx-1);
}

function goToNextSentence() {
    if (pageState.nextSentenceMode === 'random') {
        goToSentence("");
    } else {
        goToSentence(pageState.sentenceIdx+1);
    }
}

function goToNextSentenceByNumber() {
    goToSentence(parseInt($("#go-to-sentence-number").val())-1);
}

function tryAgain() {
    goToSentence(pageState.sentenceIdx);
}

function checkWords() {
    $("#show-sentence-area").html("");
    $("#check-words-btn").focus();
    doPost({
        url: "/words/engText/" + pageState.engTextId + "/checkWords",
        data: pageState.sentence,
        success: function (response) {
            if (response.status == "ok") {
                pageState.sentence = response.sentence;
                drawSentenceToLearn();
            }
        }
    });
}

function drawSentenceToLearn() {
    let $sentenceArea = $("#sentence-to-learn-area");
    $sentenceArea.html($("<span/>", {text:(pageState.sentenceIdx + 1) + ":  ", "class": "current-sentence-number"}));
    let inputs = [];
    _.reduce(
        pageState.sentence,
        function(memo, token){
            let elem = createElemForToken(token);
            if (elem.is("input")) {
                inputs.push(elem);
            }
            return memo.append(elem);
        },
        $sentenceArea
    );
    if (_.size(inputs) === 0) {
        $("#try-again-btn").focus();
        $("#task-description-area").html(
            pageState.taskDescription + " " + (pageState.countsAfter?pageState.countsAfter:"")
        );
    } else {
        $("#task-description-area").html(
            pageState.taskDescription + " " + (pageState.countsBefore?pageState.countsBefore:"")
        );
        pageState.firstInput = _.first(inputs);
        pageState.firstInput.focus();
        for (var i = 0; i < _.size(inputs) - 1; i++) {
            let nextInput = inputs[i+1];
            inputs[i].keypress(function(e){
                if (e.which == 13) {
                    e.preventDefault();
                    nextInput.focus();
                }
            });
        }
        _.last(inputs).keypress(function(e){
            if (e.which == 13) {
                e.preventDefault();
                checkWords();
            }
        });
    }
}

function createElemForToken(token) {
    if (token.meta) {
        return $("<span/>");
    } else if (token.hidden) {
        if (token.correct) {
            return $("<span/>", {text:token.value, "class": "correct-user-input"});
        } else {
            var clazz = "";
            var value = "";
            if (token.userInput) {
                clazz = "incorrect-user-input";
                value = token.userInput;
            }
            return $("<input/>", {"type":"text", "class":clazz, value:value}).keyup(function () {
                token.userInput = this.value;
            });
        }
    } else {
        return $("<span/>", {text:token.value});
    }
}

function showSentence() {
    let $btn = $("#show-sentence-btn");
    let $showArea = $("#show-sentence-area");
    $showArea.html("");
    if (!$btn.prop(SHOWN)) {
        $btn.prop(SHOWN, true);
        _.reduce(
            pageState.sentence,
            function(memo, token){
                return memo.append($("<span/>", {text:token.value}));
            },
            $showArea
        );
    } else {
        $btn.prop(SHOWN, false);
    }
    pageState.firstInput.focus();
}
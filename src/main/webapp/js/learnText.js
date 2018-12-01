function goToSentence(sentenceIdx) {
    doGet({
        url: "/words/engText/" + pageState.engTextId + "/sentenceForLearning/" + sentenceIdx,
        success: function (response) {
            if (response.status == "ok") {
                pageState.maxSentenceIdx = response.maxSentenceIdx;
                if (!response.sentence) {
                    infoDialog(
                        "dialog-confirm",
                        "No sentence available. maxSentenceIdx = " + response.maxSentenceIdx,
                        "OK",
                        function () {}
                    );
                } else {
                    pageState.sentenceIdx = sentenceIdx;
                    pageState.sentence = response.sentence;
                    drawSentenceToLearn();
                }
            }
        }
    });
}

function drawSentenceToLearn() {
    let $sentenceArea = $("#sentence-to-learn-area");
    $sentenceArea.html("");
    _.reduce(
        pageState.sentence,
        function(memo, token){
            return memo.append(createElemForToken(token));
        },
        $sentenceArea
    );
}

function createElemForToken(token) {
    if (token.meta) {
        return "";
    } else if (token.hidden) {
        return $("<input/>", {"type":"text"});
    } else {
        return $("<span/>", {text:token.value});
    }
}
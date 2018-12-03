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
                $("#task-description-area").html(
                    "Learn words. Groups: " + response.groups + ". Counts: " + response.counts
                );
            }
        }
    });
}

function drawWordToLearn() {
    $wordArea = $("#word-to-learn-area");
    $table = $("<table/>", {"class": "word-to-learn-table"});
    $wordArea.html($table);
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Basic form:"}))
            .append($("<td/>", {text:pageState.word.word}))
    );
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"In text:"}))
            .append($("<td/>", {text:pageState.word.wordInText}))
    );
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Transcription:"}))
            .append($("<td/>", {text:pageState.word.transcription}))
    );
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Meaning:"}))
            .append($("<td/>").html(composeMeaning(pageState.word.meaning)))
    );
    $table.append(
        $("<tr/>")
            .append($("<td/>", {text:"Examples:"}))
            .append($("<td/>").html(composeExamples(pageState.word.examples)))
    );
}

function composeMeaning(meaning) {
    return strToDivs(meaning);
}

function composeExamples(examples) {
    return _.reduce(
        examples,
        function(memo, example){
            return memo.append(drawSentence(example));
        },
        $("<div/>")
    );

}

function drawSentence(sentence) {
    return _.reduce(
        sentence,
        function(memo, token){
            return memo.append(createElemForToken(token));
        },
        $("<div/>")
    );
}

function createElemForToken(token) {
    if (token.meta) {
        return $("<span/>");
    } else {
        return $("<span/>", {text:token.value});
    }
}

var wordsToImport = [];

function importWord(textId, wordIdx) {
    console.log("Importing word " + wordIdx);
    doPost(
        {
            url: "/words/createWord",
            data: {
                engTextId: textId,
                word: wordsToImport[wordIdx]
            },
            success: function (response) {
                if (response.status == "ok") {
                    if (wordIdx === wordsToImport.length - 1) {
                        console.log("Import done.");
                    } else {
                        importWord(textId, wordIdx + 1);
                    }
                } else {
                    console.log("response.status = '" + response.status + "'");
                }
            }
        }
    )
}
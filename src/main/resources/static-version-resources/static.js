function registerShortcuts() {
    document.addEventListener(
        "keyup",
        function onKeyUp(event) {
            if (event.key == "p") {
                document.getElementById("btn-up").click();
            } else if (event.key == "l") {
                document.getElementById("btn-down").click();
            } else if (event.key == ",") {
                document.getElementById("btn-left").click();
            } else if (event.key == ".") {
                document.getElementById("btn-right").click();
            } else if (event.key == "[") {
                document.getElementById("btn-leftmost").click();
            } else if (event.key == "]") {
                document.getElementById("btn-rightmost").click();
            } else if (event.key == "/") {
                showContent();
            }
        },
        false
    );
}

function showContent() {
    var x = document.getElementById("content-wrapper");
    if (x.style.display === "none") {
        x.style.display = "block";
    } else {
        x.style.display = "none";
    }
}
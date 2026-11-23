function initEmberRiddlePage() {
    var form = document.getElementById("answer-form");
    var answer = document.getElementById("answer");
    if (!form || !answer) return;

    answer.addEventListener("keydown", function (event) {
        if (event.key === "Enter" || event.keyCode === 257) {
            event.preventDefault();
            form.requestSubmit();
        }
    });
}

document.addEventListener("DOMContentLoaded", initEmberRiddlePage);

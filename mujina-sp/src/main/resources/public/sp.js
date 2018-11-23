document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("force-authn").addEventListener("change", function (e) {
        var link = document.getElementById("user-link");
        var checked = e.target.checked;
        link.href = link.href.replace(checked ? "false" : "true", checked ? "true" : "false");
    });
});

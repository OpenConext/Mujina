function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

document.addEventListener("DOMContentLoaded", function () {
    [].forEach.call(document.querySelectorAll(".help,.close"), function (el) {
        el.addEventListener("click", function (e) {
            e.stopPropagation();
            e.preventDefault();
            var explanation = document.getElementById("explanation");
            explanation.classList.toggle("hide");
            if (!explanation.classList.contains("hide")) {
                setTimeout(function () {
                    document.getElementById("close").focus();
                }, 25);
            }
        });
    });

    document.getElementById("close").addEventListener("blur", function () {
        document.getElementById("explanation").classList.add("hide");
    });

    document.querySelector(".acr-select").addEventListener("change", function (e) {
        var val = e.target.value;
        var acrSelect = document.getElementById("authn-context-class-ref-value");
        acrSelect.value = val;
    });

    document.querySelector(".attribute-select").addEventListener("change", function (e) {
        var val = e.target.value;
        var selectedOption = document.querySelector('option[value="' + val + '"]');
        var text = selectedOption.text;
        var multiplicity = selectedOption.dataset.multiplicity === "true";
        var newElement = document.createElement("li");
        newElement.setAttribute("class", "attribute-value");
        var mainId = guid();
        newElement.setAttribute("id", mainId);
        var spanId = guid();
        var inputId = guid();
        newElement.innerHTML = "<label>" + val + "</label>" +
            "<input class='input-attribute-value' type='text' id='" + inputId + "' name='" + val + "'>" +
            "<span id='" + spanId + "' class='remove-attribute-value'>🗑</span>";
        document.getElementById("attribute-list").appendChild(newElement);
        document.getElementById(spanId).addEventListener("click", function () {
            var element = document.getElementById(mainId);
            element.parentNode.removeChild(element);
            if (!multiplicity) {
                var select = document.getElementById("add-attribute");
                var option = document.createElement("option");
                option.text = text;
                option.value = val;
                select.add(option);
            }
        });
        var select = document.getElementById("add-attribute");
        if (!multiplicity) {
            select.remove(select.selectedIndex);
        }
        select.value = "Add attribute...";
        setTimeout(function () {
            var inputElement = document.getElementById(inputId);
            inputElement.focus();
            inputElement.addEventListener("keypress", function (e) {
                if (e.code === "Enter") {
                    e.stopPropagation();
                    e.preventDefault();
                    select.focus();
                }
            });
        }, 25);
    });
});

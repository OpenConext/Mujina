function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

var PERSONAS_STORAGE_KEY = "mujina.personas";
var filePersonas = null;

function b64encode(str) {
    return btoa(unescape(encodeURIComponent(str)));
}

function b64decode(b64) {
    return decodeURIComponent(escape(atob(b64)));
}

function loadPersonas() {
    try {
        var raw = localStorage.getItem(PERSONAS_STORAGE_KEY);
        return raw ? JSON.parse(raw) : {};
    } catch (e) {
        return {};
    }
}

function savePersonas(personas) {
    try {
        localStorage.setItem(PERSONAS_STORAGE_KEY, JSON.stringify(personas));
    } catch (e) {
        // Storage unavailable (e.g. private browsing) - personas are silently disabled.
    }
}

function collectFormData() {
    var attributes = [];
    [].forEach.call(document.querySelectorAll("#attribute-list li.attribute-value"), function (row) {
        attributes.push({
            name: row.querySelector("label").textContent,
            value: row.querySelector(".input-attribute-value").value
        });
    });
    return {
        username: document.getElementById("username").value,
        password: document.getElementById("password").value,
        persistMe: document.getElementById("persist-me").checked,
        acr: document.getElementById("authn-context-class-ref").value,
        attributes: attributes
    };
}

function createAttributeRow(name, value) {
    var select = document.getElementById("add-attribute");
    var option = select.querySelector('option[value="' + name + '"]');
    var multiplicity = option !== null && option.dataset.multiplicity === "true";
    var newElement = document.createElement("li");
    newElement.setAttribute("class", "attribute-value");
    newElement.setAttribute("id", guid());
    newElement.setAttribute("data-multiplicity", multiplicity ? "true" : "false");
    var label = document.createElement("label");
    label.textContent = name;
    var input = document.createElement("input");
    input.setAttribute("class", "input-attribute-value");
    input.setAttribute("type", "text");
    input.setAttribute("id", guid());
    input.setAttribute("name", name);
    input.value = value || "";
    var span = document.createElement("span");
    span.setAttribute("class", "remove-attribute-value");
    span.textContent = "🗑";
    span.addEventListener("click", function () {
        newElement.parentNode.removeChild(newElement);
        if (!multiplicity) {
            var removedOption = document.createElement("option");
            removedOption.text = name;
            removedOption.value = name;
            select.add(removedOption);
        }
        updateSavePersonaVisibility();
    });
    newElement.appendChild(label);
    newElement.appendChild(input);
    newElement.appendChild(span);
    document.getElementById("attribute-list").appendChild(newElement);
    if (!multiplicity && option !== null) {
        select.remove(option.index);
    }
    select.value = "Add attribute...";
    return input;
}

function restoreFormData(data) {
    document.getElementById("username").value = data.username || "";
    document.getElementById("password").value = data.password || "";
    document.getElementById("persist-me").checked = !!data.persistMe;
    var acrSelect = document.getElementById("authn-context-class-ref");
    var acrOption = acrSelect.querySelector('option[value="' + (data.acr || "") + '"]');
    acrSelect.value = acrOption !== null ? data.acr : acrSelect.options[0].value;
    document.getElementById("authn-context-class-ref-value").value = acrSelect.value;

    var list = document.getElementById("attribute-list");
    [].forEach.call(list.querySelectorAll("li.attribute-value"), function (row) {
        if (row.getAttribute("data-multiplicity") !== "true") {
            var name = row.querySelector("label").textContent;
            var select = document.getElementById("add-attribute");
            if (!select.querySelector('option[value="' + name + '"]')) {
                var option = document.createElement("option");
                option.text = name;
                option.value = name;
                select.add(option);
            }
        }
    });
    while (list.firstChild) {
        list.removeChild(list.firstChild);
    }
    (data.attributes || []).forEach(function (attr) {
        createAttributeRow(attr.name, attr.value);
    });
    updateSavePersonaVisibility();
}

function suggestedPersonaName() {
    var username = document.getElementById("username").value.trim();
    var base = username !== "" ? username : "settings-" + new Date().toISOString().slice(0, 10);
    return uniquePersonaName(base, loadPersonas());
}

function showPersonaNameRow() {
    document.getElementById("save-persona-button").hidden = true;
    var row = document.getElementById("persona-name-row");
    row.hidden = false;
    var input = document.getElementById("persona-name");
    input.value = suggestedPersonaName();
    setTimeout(function () {
        input.focus();
        input.select();
    }, 25);
}

function hidePersonaNameRow() {
    document.getElementById("persona-name-row").hidden = true;
    updateSavePersonaVisibility();
}

function uniquePersonaName(name, personas) {
    var candidate = name;
    var counter = 1;
    while (personas[candidate]) {
        candidate = name + "-" + counter;
        counter++;
    }
    return candidate;
}

function saveCurrentPersona() {
    var input = document.getElementById("persona-name");
    var name = input.value.trim();
    if (!name) {
        input.focus();
        return;
    }
    var personas = loadPersonas();
    personas[name] = collectFormData();
    savePersonas(personas);
    renderPersonas();
    hidePersonaNameRow();
}

function updateSavePersonaVisibility() {
    var hasRows = document.querySelectorAll("#attribute-list li.attribute-value").length > 0;
    document.getElementById("save-persona").hidden = !hasRows;
    if (!hasRows) {
        document.getElementById("persona-name-row").hidden = true;
        document.getElementById("save-persona-button").hidden = false;
    } else {
        document.getElementById("save-persona-button").hidden = !document.getElementById("persona-name-row").hidden;
    }
}

function renderPersonas() {
    var container = document.getElementById("personas");
    var list = document.getElementById("persona-list");
    while (list.firstChild) {
        list.removeChild(list.firstChild);
    }
    var personas = loadPersonas();
    var names = Object.keys(personas);
    container.hidden = names.length + (filePersonas !== null ? filePersonas.length : 0) === 0;
    names.forEach(function (name) {
        var li = document.createElement("li");
        li.className = "persona";

        var load = document.createElement("button");
        load.type = "button";
        load.className = "persona-load";
        load.textContent = name;
        load.title = "Load this persona into the form";
        load.addEventListener("click", function () {
            restoreFormData(personas[name]);
        });

        var copy = document.createElement("button");
        copy.type = "button";
        copy.className = "persona-copy";
        copy.textContent = "📋";
        copy.title = "Copy a URL that restores this persona in another browser";
        copy.addEventListener("click", function () {
            copyPersonaUrl(name, personas[name], copy);
        });

        var remove = document.createElement("button");
        remove.type = "button";
        remove.className = "persona-delete";
        remove.textContent = "×";
        remove.title = "Delete this persona";
        remove.addEventListener("click", function () {
            delete personas[name];
            savePersonas(personas);
            renderPersonas();
        });

        li.appendChild(load);
        li.appendChild(copy);
        li.appendChild(remove);
        list.appendChild(li);
    });
    (filePersonas !== null ? filePersonas : []).forEach(function (persona) {
        var li = document.createElement("li");
        li.className = "persona";

        var load = document.createElement("button");
        load.type = "button";
        load.className = "persona-load";
        load.textContent = persona.name;
        load.title = persona.description || "Load this persona into the form";
        load.addEventListener("click", function () {
            restoreFormData({
                username: persona.username,
                password: persona.password,
                persistMe: persona.persistMe,
                acr: persona.acr,
                attributes: persona.attributes
            });
        });

        li.appendChild(load);
        list.appendChild(li);
    });
}

function buildPersonaUrl(name, data) {
    var payload = {name: name};
    for (var key in data) {
        if (data.hasOwnProperty(key)) {
            payload[key] = data[key];
        }
    }
    return location.origin + location.pathname + "#data=" + b64encode(JSON.stringify(payload));
}

function copyToClipboardFallback(text) {
    var input = document.createElement("input");
    input.type = "text";
    input.value = text;
    input.style.position = "fixed";
    input.style.opacity = "0";
    document.body.appendChild(input);
    input.select();
    document.execCommand("copy");
    document.body.removeChild(input);
}

function copyToClipboard(text, done) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(done, function () {
            copyToClipboardFallback(text);
            done();
        });
    } else {
        copyToClipboardFallback(text);
        done();
    }
}

function copyPersonaUrl(name, data, button) {
    copyToClipboard(buildPersonaUrl(name, data), function () {
        button.textContent = "✓";
        setTimeout(function () {
            button.textContent = "📋";
        }, 1500);
    });
}

function importPersonaFromHash() {
    if (location.hash.indexOf("#data=") !== 0) {
        return;
    }
    var hash = location.hash;
    history.replaceState(null, "", location.pathname + location.search);
    var data = null;
    try {
        data = JSON.parse(b64decode(hash.slice(6)));
    } catch (e) {
        data = null;
    }
    if (!data || typeof data.name !== "string" || !data.name || !(data.attributes instanceof Array)) {
        return;
    }
    var personas = loadPersonas();
    personas[data.name] = {
        username: data.username || "",
        password: data.password || "",
        persistMe: !!data.persistMe,
        acr: data.acr || "",
        attributes: data.attributes
    };
    savePersonas(personas);
    restoreFormData(personas[data.name]);
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
        var input = createAttributeRow(val, "");
        updateSavePersonaVisibility();
        setTimeout(function () {
            input.focus();
            input.addEventListener("keypress", function (keypress) {
                if (keypress.code === "Enter") {
                    keypress.stopPropagation();
                    keypress.preventDefault();
                    document.getElementById("add-attribute").focus();
                }
            });
        }, 25);
    });

    var filePersonasElement = document.getElementById("file-personas");
    if (filePersonasElement !== null) {
        try {
            filePersonas = JSON.parse(filePersonasElement.textContent);
        } catch (e) {
            filePersonas = null;
        }
    }

    importPersonaFromHash();
    renderPersonas();

    document.getElementById("save-persona-button").addEventListener("click", function () {
        showPersonaNameRow();
    });
    document.getElementById("persona-name-save").addEventListener("click", saveCurrentPersona);
    document.getElementById("persona-name-cancel").addEventListener("click", hidePersonaNameRow);
    updateSavePersonaVisibility();
});
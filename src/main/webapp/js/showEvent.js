function selectAll(source) {
    var name = source.id;
    var type = source.localName;
    var selectors = $(type + "[id^='" + name + "']");
    for (var i = 0; i < selectors.length; i++) {
        selectors[i].value = source.value;
        selectors[i].checked = source.checked;
    }
}
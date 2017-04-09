/**
 * Created by Алексей on 10.04.2017.
 */

function toggle(source) {
    var name = source.name;
    var checkboxes = $("input[name^='" + name + "']");
    for (var i = 0, n = checkboxes.length; i < n; i++) {
        checkboxes[i].checked = source.checked;
    }
}
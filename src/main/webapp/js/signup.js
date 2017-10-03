/*
 * Created by artem on 11.03.17.
 *
 * Scripts for automatic generation of badge names
 * based on user's first and last names in registration form.
 */

var inputFirstName = document.getElementById("inputFirstName");
var inputLastName = document.getElementById("inputLastName");
var inputBadgeName = document.getElementById("inputBadgeName");
var inputFirstNameCyr = document.getElementById("inputFirstNameCyr");
var inputLastNameCyr = document.getElementById("inputLastNameCyr");
var inputBadgeNameCyr = document.getElementById("inputBadgeNameCyr");

function generateBadgeName(inputFirstName, inputLastName, inputBadgeName) {
    inputBadgeName.value = inputFirstName.value + " " + inputLastName.value;
}

function badgeNameOnInput() {
    generateBadgeName(inputFirstName, inputLastName, inputBadgeName);
}

function badgeNameCyrOnInput() {
    generateBadgeName(inputFirstNameCyr, inputLastNameCyr, inputBadgeNameCyr);
}
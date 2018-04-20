function checkValue(form, emptyUserMessage, emptyLoginMessage) {
    
    var userInput = form[form.id + ":username"];
    var loginInput = form[form.id + ":password"];
    
    if (userInput.value === ''){
        alert(emptyUserMessage);
        userInput.focus();
        return false;
    }
    if (loginInput.value === '') {
        alert(emptyLoginMessage);
        loginInput.focus();
        return false;
    }
    return true;
}

function showProgress(data) {
    if (data.status === "begin") {
        document.getElementById('loading_wrapper').style.display = "block";
    } else if (data.status === "success") {
        document.getElementById('loading_wrapper').style.display = "none";
    }
}


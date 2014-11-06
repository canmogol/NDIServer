window.onload = function () {
    var loginController = new LoginController();
    loginController.init();
};

function LoginController() {

    this.init = function () {
        var screenLoginButton = document.getElementById("loginPageLoginButton");
        var loginController = this;
        screenLoginButton.onclick = function () {
            var username = document.getElementById('loginPageUsername').value;
            var password = document.getElementById('loginPagePassword').value;
            if (username.trim().length > 0 && password.trim().length > 0) {
                loginController.doLogin(username, password);
            } else {
                alert("username and password cannot be empty");
            }
        };
    };

    this.doLogin = function (username, password) {
        // first open a loading dialog, this will be removed if the user clicks button
        var requestHandler = {
            url: "login",
            method: "GET",
            async: true,
            cancelled: false,
            headers: {"x-http-requester": "X212"},
            data: 'username=' + username + '&password=' + password,
            onCancel: function () {
                console.log("Request cancelled!");
            },
            error: function (e) {
                console.log("error: " + e)
            },
            requestNotInitialized: function () {
                console.log("requestNotInitialized")
            },
            serverConnectionEstablished: function () {
                console.log("serverConnectionEstablished")
            },
            requestReceived: function () {
                console.log("requestReceived")
            },
            processingRequest: function () {
                console.log("processingRequest")
            },
            requestFinishedResponseReady: function (request, response) {
                console.log("CALL CALLBACK! requestFinishedResponseReady, cancelled: " + requestHandler.cancelled);
                Alerts.removeAllAlerts();
                try {
                    if (response.response.content.data == "logged") {
                        Store.putStorage("userInformation", response.user);
                        Store.putStorage("loginResponseMessage", response.message);
                        var loggedController = new LoggedController();
                        loggedController.init();
                    } else {
                        alert("could not login, message: " + response.message);
                    }
                } catch (e) {
                    alert(e);
                }
            }
        };
        alert("Logging in", Alerts.LOADING_CIRCLE, function (buttonIndex, buttonText) {
            requestHandler.cancelled = true;
        }, "cancel", null);
        sendRequest(requestHandler);
    };

}

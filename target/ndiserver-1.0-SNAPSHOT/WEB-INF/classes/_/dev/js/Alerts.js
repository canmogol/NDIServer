(function (parentObject) {

    ////////////////////////////////
    //// CONSTRUCTOR CODE BEGIN ////
    ////////////////////////////////

    try {
        // add this "Alerts" object to the parent
        parentObject.Alerts = this;
        parentObject.Alerts.LOADING_CIRCLE = "LOADING-CIRCLE";
        parentObject.Alerts.LOADING = "LOADING";

        // create AlertListener object
        var alertListener = new AlertListener();

        // create a UI for Alert
        var alertUI = new AlertUI();

        // try to replaace alert function
        if (parentObject.alert instanceof Object) {
            parentObject.alert = function (text, image, callback, buttonText, titleText) {
                addAlert(text, image, callback, buttonText, titleText);
            }
        }
    } catch (e) {
        //console.debug(e);
    }

    ////////////////////////////////
    //// CONSTRUCTOR CODE END   ////
    ////////////////////////////////


    /*
     public methods below
     */

    this.removeAllAlerts = function () {
        alertUI.remove();
        alertListener = new AlertListener();
        alertUI = new AlertUI();
    };

    this.addAlert = function (text, image, callback, buttonText, titleText) {
        alertListener.add(text, image, callback, buttonText, titleText);
    };

    this.getAlertTexts = function (text) {
        return alertListener.getAlertTexts();
    };

    this.createDialogOkCancel = function (text, okListener, cancelListener) {
        //do something
    };

    /*
     private methods below
     */

    function privateMethod1(text) {
        /// do something
    }

    function privateMethod2(obj) {
        /// do something
    }


    /**
     * AlertListener Class
     * @constructor
     */
    function AlertListener() {

        ////////////////////////////////
        //// CONSTRUCTOR CODE BEGIN ////
        ////////////////////////////////
        // private variable for alert texts
        var alertTexts = [];
        var alertImages = [];
        var alertCallBacks = [];
        var alertButtonTexts = [];
        var alertTitleTexts = [];
        var isAlertVisible = false;
        ////////////////////////////////
        //// CONSTRUCTOR CODE END   ////
        ////////////////////////////////


        /*
         public methods below
         */

        this.add = function (text, image, callback, buttonText, titleText) {
            alertTexts.push(text);
            alertImages.push(image);
            alertCallBacks.push(callback);
            alertButtonTexts.push(buttonText);
            alertTitleTexts.push(titleText);
            notify();
        };

        this.getAlertTexts = function (text) {
            var alertTextsReturn = [];
            for (var i = 0; alertTexts.length > i; i++) {
                alertTextsReturn[i] = alertTexts[i];
            }
            return alertTextsReturn;
        };

        this.okClicked = function (callback, buttonIndex, buttonText) {
            alertUI.remove();
            isAlertVisible = false;
            if (callback != null && callback != undefined) {
                try {
                    callback(buttonIndex, buttonText);
                } catch (e) {
                    console.log("could not run the callback: " + callback);
                }
            }
            notify();
        };

        /*
         private methods below
         */

        function notify() {
            //console.debug("listener notified, will check if there are any alert messages");
            if (!isAlertVisible) {
                if (alertTexts.length > 0) {
                    //console.debug("there are alert messages");
                    try {
                        createAlert(alertTexts.shift(), alertImages.shift(), alertCallBacks.shift(), alertButtonTexts.shift(), alertTitleTexts.shift());
                    } catch (e) {
                        //console.debug(e);
                    }
                } else {
                    //console.debug("no alert message what so ever");
                }
            } else {
                //console.debug("an alert message is already visible");
            }
        }

        function createAlert(text, image, callback, buttonText, titleTexts) {
            //console.debug("creating an alert with text: " + text);
            alertUI.setText(text);
            alertUI.setImage(image);
            alertUI.setCallBack(callback);
            alertUI.setButtonText(buttonText);
            alertUI.setTitleText(titleTexts);
            alertUI.setListener(alertListener);
            alertUI.show();
            isAlertVisible = true;
        }

    }


    /**
     * AlertUI Class
     * @constructor
     */
    function AlertUI() {

        ////////////////////////////////
        //// CONSTRUCTOR CODE BEGIN ////
        ////////////////////////////////
        // private variable for alert text
        var alertTransparentID = "AlertsAlertUITransparentContainer";
        var alertDialogID = "AlertsAlertUIDialogContainer";
        var alertDialogClasses = "AlertFadeIn";
        var alertDialogTextID = "AlertsAlertUIDialogContainerText";
        var alertDialogTitleID = "AlertsAlertUIDialogContainerTitle";
        var alertDialogImgID = "AlertsAlertUIDialogContainerImg";
        var alertDialogButtonID = "AlertsAlertUIDialogContainerButton";
        var alertText = "";
        var alertImage = null;
        var alertCallBack = null;
        var alertButtonText = null;
        var alertTitleText = null;
        var alertListener;
        var alertLocale = getUserLocale();
        var alertButtonI18 = {en: "OK", tr: "Tamam"};
        var alertButtonTextOK = (alertButtonI18[alertLocale] != undefined ? alertButtonI18[alertLocale] : alertButtonI18["en"]);
        ////////////////////////////////
        //// CONSTRUCTOR CODE END   ////
        ////////////////////////////////


        /*
         public methods below
         */

        this.setText = function (text) {
            alertText = text;
        };

        this.setImage = function (image) {
            alertImage = image;
        };

        this.setCallBack = function (callback) {
            alertCallBack = callback;
        };

        this.setButtonText = function (buttonText) {
            alertButtonText = buttonText;
        };

        this.setTitleText = function (titleText) {
            alertTitleText = titleText;
        };

        this.setListener = function (listener) {
            alertListener = listener;
        };

        this.show = function () {
            renderHtml();
            // commented below code, will render html always
            /*
             try {
             if (!window instanceof Object) {
             throw "is this not a browser?";
             }
             renderHtml();
             } catch (e) {
             //console.debug(e);
             renderNative();
             }*/
        };

        this.remove = function () {
            try {
                var div = document.getElementById(alertTransparentID);
                while (div) {
                    // if already on screen remove it
                    div.parentNode.removeChild(div);
                    // again try to get it from document
                    div = document.getElementById(alertTransparentID);
                }
            } catch (e) {
                console.debug(e);
            }
        };

        /*
         private methods below
         */

        function createButton(callback, buttonIndex, buttonText) {
            // set the listener to button
            var button = document.createElement('a');
            button.id = alertDialogButtonID;
            button.addEventListener('click', function () {
                alertListener.okClicked(callback, buttonIndex, buttonText);
            }, false);
            button.title = buttonText;
            button.href = "#";
            var linkText = document.createTextNode(buttonText);
            button.appendChild(linkText);
            return button;
        }

        function renderHtml() {
            try {
                var div = document.getElementById(alertTransparentID);
                if (div) {
                    alertUI.remove();
                }

                // create the alert dialog div
                div = document.createElement('div');
                // set the id to AlertsAlertUITransparentContainer
                div.id = alertTransparentID;
                div.className = alertDialogClasses;

                // create the alert dialog div
                var divDialog = document.createElement('div');
                // set the id to AlertsAlertUIDialogContainer
                divDialog.id = alertDialogID;
                divDialog.className = alertDialogClasses;
                div.appendChild(divDialog);

                // set the value of Title
                if (alertTitleText != null && alertTitleText != undefined) {
                    var title = document.createElement('label');
                    title.id = alertDialogTitleID;
                    divDialog.appendChild(title);
                    title.innerHTML = alertTitleText;
                }

                // set the value of text
                var text = document.createElement('label');
                text.id = alertDialogTextID;
                divDialog.appendChild(text);
                text.innerHTML = alertText;

                // set the image if available
                if (alertImage != null && alertImage != undefined) {
                    if (alertImage === "LOADING") {
                        var divLoading = document.createElement('div');
                        divLoading.innerHTML = '<div id="noTrespassingOuterBarG"><div id="noTrespassingFrontBarG" class="noTrespassingAnimationG"><div class="noTrespassingBarLineG"></div><div class="noTrespassingBarLineG"></div><div class="noTrespassingBarLineG"></div><div class="noTrespassingBarLineG"></div><div class="noTrespassingBarLineG"></div><div class="noTrespassingBarLineG"></div></div></div>';
                        divDialog.appendChild(divLoading);
                    } else if (alertImage === "LOADING-CIRCLE") {
                        var divLoading = document.createElement('div');
                        divLoading.innerHTML = '<div id="floatingCirclesG"><div class="f_circleG" id="frotateG_01"></div><div class="f_circleG" id="frotateG_02"></div><div class="f_circleG" id="frotateG_03"></div><div class="f_circleG" id="frotateG_04"></div><div class="f_circleG" id="frotateG_05"></div><div class="f_circleG" id="frotateG_06"></div><div class="f_circleG" id="frotateG_07"></div><div class="f_circleG" id="frotateG_08"></div></div>';
                        divDialog.appendChild(divLoading);
                    } else {
                        var img = document.createElement('img');
                        img.id = alertDialogImgID;
                        divDialog.appendChild(img);
                        img.src = alertImage;
                    }
                }

                if (alertButtonText != null && alertButtonText != undefined) {
                    if (alertButtonText instanceof Array) {
                        for (var i = 0; i < alertButtonText.length; i++) {
                            var button  = createButton(alertCallBack, i, alertButtonText[i]);
                            divDialog.appendChild(button);
                        }
                    } else {
                        divDialog.appendChild(createButton(alertCallBack, 0, alertButtonText));
                    }
                }else{
                    divDialog.appendChild(createButton(alertCallBack, 0, alertButtonTextOK));
                }

                // append this div to document
                document.body.appendChild(div);
            } catch (e) {
                //console.debug("could not create alert dialog, ex: " + e);
                alert(alertText);
            }
        }

        function renderNative() {
            alert("Dear developer, please implement me, thanks :)");
        }

        function getUserLocale() {
            try {
                if (navigator.userLanguage) {
                    // Explorer
                    return (navigator.userLanguage.length > 2 ? navigator.userLanguage.substring(0, 2) : navigator.userLanguage);
                } else if (navigator.language) {
                    return (navigator.language.length > 2 ? navigator.language.substring(0, 2) : navigator.language);
                }
            } catch (e) {
                //console.debug(e);
            }
            return "tr";
        }

    }

})(this);

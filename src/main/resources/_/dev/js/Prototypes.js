/**
 * will create and set a console object if there is not any
 * if it does, it means the logging not implement by this platform
 * use TOP_LEVEL_OBJECT (if it is window or document or whatever)
 * TOP_LEVEL_OBJECT.ConsoleLoggingArray array for collecting logs
 */
(function (topLevelObject) {
    try {
        if (!topLevelObject.console.log instanceof Object) {
            throw "not an object!";
        } else {
            try {
                if (navigator.userAgent.match(/IEMobile\/10\.0/)) {
                    window.console = {
                        log: function (str) { window.external.Notify("log: "+str); },
                        debug: function (str) { window.external.Notify("debug: " + str); },
                        error: function (str) { window.external.Notify("error: " + str); },
                        info: function (str) { window.external.Notify("info: " + str); }
                    };

                    window.onerror = function (e) {
                        console.log("ERROR: " + JSON.stringify(e));
                    };
                }
            }catch(e){
            }
            //topLevelObject.console.debug("platform supports console logging, using console.log()");
        }
    } catch (exception) {
        topLevelObject.ConsoleLoggingArray = [];
        topLevelObject.ConsoleLoggingArray.prototype.add = function (text) {
            try {
                // loggingArray may be used for logging for visually
                // it will always contain the last 10 messages
                // just in case that the logging mechanism implemented by developer cannot attach at time
                if (topLevelObject.ConsoleLoggingArray.length > 10) {
                    // firstLogMessage will die in vain
                    var firstLogMessage = topLevelObject.ConsoleLoggingArray.shift();
                }
                topLevelObject.ConsoleLoggingArray.push(text);
            } catch (exception) {
                // can't do anything :(
                // either an object is not there or it could not do the shifting/pushing
            }
        };
        topLevelObject.console = {
            debug: function (text) {
                this.log(text);
            },
            error: function (text) {
                this.log(text);
            },
            info: function (text) {
                this.log(text);
            },
            log: function (text) {
                topLevelObject.ConsoleLoggingArray.add(text);
            }
        }
    }
})(this);


/**
 * attach getClass method to every object to get any object's type
 * @return {*}
 */
Object.prototype.getClass = function getClass() {
    if (typeof this === "undefined")
        return "undefined";
    if (this === null)
        return "null";
    return Object.prototype.toString.call(this).match(/^\[object\s(.*)\]$/)[1];
};

/**
 * i18n translation function
 * @param value
 * @returns {*}
 */
function tr(value) {
    return value;
}

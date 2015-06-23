// define storage
Store = {

    putStorage: function (key, value) {
        try {
            localStorage.setItem(key, value);
        } catch (e) {
            console.debug("exception occurred while local storage setItem, e:" + e);
        }
    },

    getStorage: function (key) {
        try {
            return localStorage.getItem(key);
        } catch (e) {
            console.debug("exception occurred while local storage getItem, e:" + e);
            return null;
        }
    },

    clearStorage: function () {
        try {
            localStorage.clear();
        } catch (e) {
            console.debug("exception occurred while local storage clear, e:" + e);
        }
    }

};
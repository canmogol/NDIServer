package com.fererlab.session;

import com.fererlab.servlet.AES;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * acm 10/15/12
 */
public class Session extends TreeMap<String, Serializable> {

    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());

    private String cookieDefaultPassword;
    private String cookieSignSecretKey;
    private String applicationCookieName;
    private String rawContent = "";
    private String sessionId;
    private SessionUser user;

    public Session(String rawContent) {
        this.rawContent = rawContent;
        xStreamJSON.setMode(XStream.SINGLE_NODE_XPATH_RELATIVE_REFERENCES);
        xStreamJSON.autodetectAnnotations(true);
    }

    public void fromCookie(String applicationCookieName, String cookieSignSecretKey) {
        this.applicationCookieName = applicationCookieName;
        this.cookieSignSecretKey = cookieSignSecretKey;
        this.cookieDefaultPassword = sha1(cookieSignSecretKey).substring(0, 16);
        String applicationCookieSignedName = applicationCookieName + "_" + SessionKeys.COOKIE_SIGN_KEY.getValue();
        String applicationCookie = null;
        String applicationCookieValue = null;
        String applicationCookieSigned = null;
        String applicationCookieSignedValue = null;
        String[] appAndSignCookies = rawContent.split(";");
        /*
         SampleApplication=a2V5MT12YWx1ZTE7a2V5Mj12YWx1ZTI7
         SampleApplication_fr_ck_sn_ky=YWRhOTI0M2QyZDA5ZmQwYmQ1ZTM4MGE5ODc4Y2M3YTlhZDA2M2E0MA
          */
        for (String cookie : appAndSignCookies) {
            if (cookie != null && !cookie.isEmpty()) {
                // SampleApplication=a2V5MT12YWx1ZTE7a2V5Mj12YWx1ZTI7==
                String[] cookieKeyValuePair = splitFromFirst(cookie.trim(), "=");
                if (cookieKeyValuePair.length == 2 &&
                        !"".trim().equals(cookieKeyValuePair[0]) &&
                        !"".trim().equals(cookieKeyValuePair[1])) {
                    /*
                       SampleApplication
                       a2V5MT12YWx1ZTE7a2V5Mj12YWx1ZTI7

                       OR

                       SampleApplication_fr_ck_sn_ky
                       YWRhOTI0M2QyZDA5ZmQwYmQ1ZTM4MGE5ODc4Y2M3YTlhZDA2M2E0MA
                     */

                    // this is the SampleApplication
                    if (applicationCookieName.equalsIgnoreCase(cookieKeyValuePair[0])) {
                        applicationCookie = cookieKeyValuePair[0].trim();
                        applicationCookieValue = cookieKeyValuePair[1].trim();

                    } else if (applicationCookieSignedName.equalsIgnoreCase(cookieKeyValuePair[0])) {
                        applicationCookieSigned = cookieKeyValuePair[0].trim();
                        applicationCookieSignedValue = cookieKeyValuePair[1].trim();
                    }

                }
            }
        }

        // 1. check if there are application cookie and its signed form
        // if values found, check if they are correct
        if (applicationCookie != null && applicationCookieSigned != null) {
            // 2.   requestSigned = SHA1(cookieSignSecretKey + applicationCookie)
            try {
                String requestSigned = sign(applicationCookieValue, cookieSignSecretKey);
                // 3.   check(applicationCookieSigned == requestSigned)
                if (applicationCookieSignedValue.equals(requestSigned)) { // the request cookie is authentic
                    // 4.   String decodedApplicationCookie = decode(applicationCookie)
                    String decodedApplicationCookie = decode(applicationCookieValue);
                    // 5.   String[] keyValuePairs = decodedApplicationCookie.split(";")
                    String[] keyValuePairs = decodedApplicationCookie.split(";");
                    // 5.   keyValuePairs.foreach(pair){String[] keyAndValuePair = pair.split("=")}
                    for (String pair : keyValuePairs) {
                        String[] keyAndValuePair = splitFromFirst(pair.trim(), "=");
                        if (keyAndValuePair != null && keyAndValuePair.length == 2) {
                            // 6.   {...  put(keyAndValuePair[0],keyAndValuePair[1])   ...}
                            this.put(keyAndValuePair[0].trim(), keyAndValuePair[1].trim()); //   here we add the cookie key / value pairs to Map
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String[] splitFromFirst(String value, String split) {
        Pattern p = Pattern.compile(split);
        Matcher m = p.matcher(value);
        int index = 1;
        while (m.find()) {
            index = m.start();
            if (index > 0) {
                break;
            }
        }
        return new String[]{
                value.substring(0, index).trim(),
                value.substring(index + 1).replace("\"", "").trim()
        };
    }

    public Map<String, String> getKeyValueMap() {
        putEncrypt(SessionKeys.USER.getValue(), xStreamJSON.toXML(getUser()));
        Map<String, String> keyValueMap = new HashMap<String, String>();
        String applicationCookieKeyValueString = "";
        for (String key : this.keySet()) {
            applicationCookieKeyValueString += key + "=" + this.get(key) + ";";
        }
        String applicationCookieContent = Base64.encodeBase64String(applicationCookieKeyValueString.getBytes()).replace("\n", "");
        try {
            if (applicationCookieName != null) {
                keyValueMap.put(applicationCookieName, applicationCookieContent);
                keyValueMap.put(applicationCookieName + "_" + SessionKeys.COOKIE_SIGN_KEY.getValue(), sign(applicationCookieContent, cookieSignSecretKey));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyValueMap;
    }

    public String decode(String value) {
        return new String(Base64.decodeBase64(value));
    }

    public String decrypt(String value) {
        return decrypt(value, cookieDefaultPassword);
    }

    public String decrypt(String value, String password) {
        return AES.symmetricDecrypt(value, password);
    }

    public String encrypt(String value, String password) {
        return AES.symmetricEncrypt(value, password);
    }

    private String sign(String value, String secretKey) throws Exception {
        String signValue = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (byte b : md.digest((value + secretKey).getBytes())) {
                signValue += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        }
        return signValue;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getRawContent() {
        return rawContent;
    }

    private String sha1(String content) {
        try {
            String sha1 = "";
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (byte b : md.digest(content.getBytes())) {
                sha1 += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
            }
            return sha1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public void putEncoded(String key, String value) {
        put(key, Base64.encodeBase64(value.getBytes()));
    }

    public void putEncrypt(String key, String value) {
        put(key, AES.symmetricEncrypt(value, cookieDefaultPassword));
    }

    public SessionUser getUser() {
        if (user == null) {
            user = new SessionUser();
            if (containsKey(SessionKeys.USER.getValue()) && get(SessionKeys.USER.getValue()) != null) {
                String encryptedUser = String.valueOf(get(SessionKeys.USER.getValue()));
                String decryptedUser = decrypt(encryptedUser);
                XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());
                xStreamJSON.setMode(XStream.SINGLE_NODE_XPATH_RELATIVE_REFERENCES);
                xStreamJSON.autodetectAnnotations(true);
                xStreamJSON.setClassLoader(Session.class.getClassLoader());
                user = (SessionUser) xStreamJSON.fromXML(decryptedUser);
            }
        }
        return user;
    }

    public String getSessionId() {
        if (sessionId == null) {
            sessionId = sha1(new Random().nextDouble() + cookieSignSecretKey);
        }
        return sessionId;
    }

}

package com.genfare.applet.encoder;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.print.PrintException;

import netscape.javascript.JSObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.genfare.smartcard.CardEncoder;
import com.genfare.smartcard.CardEncoder.FileNumber;
import com.genfare.smartcard.CardEncoderUltralightC;
import com.genfare.smartcard.CardTerminalFacade;
import com.genfare.smartcard.NativeCardEncoder;
import com.genfare.smartcard.PrintCards;
import com.genfare.smartcard.bean.EncoderConfig;
import com.genfare.smartcard.bean.PeriodPassProduct;
import com.genfare.smartcard.bean.PrintCardDTO;
import com.genfare.smartcard.bean.ProductBase;
import com.genfare.smartcard.bean.ProductBaseMixIn;
import com.genfare.smartcard.bean.StoredRideProduct;
import com.genfare.smartcard.bean.StoredValueProduct;
import com.genfare.smartcard.bean.Transfer;
import com.genfare.smartcard.bean.TransitCard;
import com.genfare.smartcard.exception.CardEncoderException;
import com.genfare.smartcard.ultralightc.bean.EncoderConfigUltralightC;
import com.genfare.smartcard.ultralightc.bean.TransitCardUltralightC;

public class EncoderApplet extends javax.swing.JApplet {

    private static final Logger log = Logger.getLogger(EncoderApplet.class.getName());

    private static final long serialVersionUID = -8016186980589184469L;

    private CardEncoder encoderDESFire = null;
    private CardEncoderUltralightC encoderUltralightC = null;
    
    private static final String cardTypeDESFire = "DESFire";
    private static final String cardTypeUltralightC = "UltralightC";
    private static String cardType = cardTypeDESFire;
    
    // private String configurationSettings = "";

    public class ResultObject {

        private boolean success = false;
        private String value = "";
        private String message = "";

        public boolean getSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Success: " + success + " and the Message: " + message;
        }

    }

    public void init() {

        log.info("EncoderApplet init");

        // Once applet has been loaded, call Javascript back on page to finalize page setup
        JSObject win = null;
        try {
            win = (JSObject) JSObject.getWindow(this);
            win.call("initEncoder", null);

            log.info("Called JS initEncoder()");

        } catch (Exception ex) {
            log.warning("ERROR calling JS .initEncoder()");
        }

    }

    private ResultObject _setEncoderResult;

    private String _setEncoderEncoderName;
    private String _setEncoderConfigJSON;
    private String _desfireKey9;
    private String _desfireKey4;
    private String _ulcKey;

    public ResultObject setEncoder(String encoderName, String configJSON, String desfireKey4, String desfireKey9, String ulcKey) {

        _setEncoderResult = new ResultObject();

        _setEncoderEncoderName = encoderName;
        _setEncoderConfigJSON = configJSON;
        _desfireKey4 = desfireKey4;
        _desfireKey9 = desfireKey9;
        _ulcKey = ulcKey;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws CardEncoderException {

                        EncoderConfig config_desfire = null;
                        EncoderConfigUltralightC config_ultralight_c = null;

                        // Get Config
                        if (_setEncoderConfigJSON != null) {

                            log.info("Deserializing config JSON during second config: " + _setEncoderConfigJSON);

                            ObjectMapper objMapper = new ObjectMapper();
                            objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                            log.info("Got Object Mapper!");

                            try {

                                log.info("Trying to read config!");

                                config_desfire = objMapper.readValue(_setEncoderConfigJSON, EncoderConfig.class);
                                log.info("Got Config DESFire! [" + config_desfire.toString() + "]");
                                
                                config_ultralight_c = objMapper.readValue(_setEncoderConfigJSON, EncoderConfigUltralightC.class);
                                log.info("Got Config ULC! [" + config_ultralight_c.toString() + "]");

                            } catch (JsonParseException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            }

                        encoderDESFire = new CardEncoder(_setEncoderEncoderName, config_desfire, _desfireKey4, _desfireKey9);
                        encoderUltralightC = new CardEncoderUltralightC(_setEncoderEncoderName, config_ultralight_c, _ulcKey);
                    }

                    _setEncoderResult.setSuccess(true);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _setEncoderResult.setSuccess(false);

            if (e.getCause() != null) {
                _setEncoderResult.setMessage(e.getCause().getMessage());
            } else {
                _setEncoderResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _setEncoderResult.setSuccess(false);

            if (e.getCause() != null) {
                _setEncoderResult.setMessage(e.getCause().getMessage());
            } else {
                _setEncoderResult.setMessage(e.getMessage());
            }

        }

        return _setEncoderResult;

    }

    public ResultObject setEncoder(String encoderName, String configJSON, boolean isDESFire, boolean isUltralightC, String desfireKey4, String desfireKey9, String ulcKey) {
        
        if(isDESFire) {
            cardType = cardTypeDESFire;
        } else if(isUltralightC) {
            cardType = cardTypeUltralightC;
        }
        
        log.info("entering setEncoder");

        _setEncoderResult = new ResultObject();

        _setEncoderEncoderName = encoderName;
        _setEncoderConfigJSON = configJSON;
        _desfireKey4 = desfireKey4;
        _desfireKey9 = desfireKey9;
        _ulcKey = ulcKey;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws CardEncoderException {

                        EncoderConfig config_desfire = null;
                        EncoderConfigUltralightC config_ultralight_c = null;

                        // Get Config
                        if (_setEncoderConfigJSON != null) {

                            log.info("Deserializing config JSON during second config: " + _setEncoderConfigJSON);

                            ObjectMapper objMapper = new ObjectMapper();

                            log.info("Got Object Mapper!");

                            try {

                                log.info("Trying to read config!");

                                config_desfire = objMapper.readValue(_setEncoderConfigJSON, EncoderConfig.class);
                                log.info("Got Config DESFire! [" + config_desfire.toString() + "]");
                                
                                config_ultralight_c = objMapper.readValue(_setEncoderConfigJSON, EncoderConfigUltralightC.class);
                                log.info("Got Config ULC! [" + config_ultralight_c.toString() + "]");

                            } catch (JsonParseException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new CardEncoderException("Configuration invalid", e);
                            }

                        encoderDESFire = new CardEncoder(_setEncoderEncoderName, config_desfire, _desfireKey4, _desfireKey9);
                        encoderUltralightC = new CardEncoderUltralightC(_setEncoderEncoderName, config_ultralight_c, _ulcKey);
                    }

                    _setEncoderResult.setSuccess(true);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _setEncoderResult.setSuccess(false);

            if (e.getCause() != null) {
                _setEncoderResult.setMessage(e.getCause().getMessage());
            } else {
                _setEncoderResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _setEncoderResult.setSuccess(false);

            if (e.getCause() != null) {
                _setEncoderResult.setMessage(e.getCause().getMessage());
            } else {
                _setEncoderResult.setMessage(e.getMessage());
            }

        }

        return _setEncoderResult;

    }

    private String _getSerialNumberEncoderName;
    private ResultObject _getSerialNumberResult;

    public ResultObject getSerialNumber(String encoderName) {

        log.info("Getting serial...");

        _getSerialNumberEncoderName = encoderName;
        _getSerialNumberResult = new ResultObject();

        if (_getSerialNumberEncoderName == null || _getSerialNumberEncoderName.equals("")) {
            log.info("Encoder not specified");
            _getSerialNumberResult.setSuccess(false);
            _getSerialNumberResult.setMessage("Encoder not specified");
            return _getSerialNumberResult;
        } else {
            log.info("Getting serial...");
        }

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() {
                    
                    log.info("Entering card encoder portion..."+_getSerialNumberEncoderName);

                    NativeCardEncoder nativeCardEncoder = new NativeCardEncoder();
                    String serialNumber = nativeCardEncoder.getSerialNumber(_getSerialNumberEncoderName);

                    _getSerialNumberResult.setSuccess(true);
                    _getSerialNumberResult.setValue(serialNumber);

                    return true;
                }
            });

            log.info("Done with privileged.");

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
            log.info("Getting serial PrivilegedActionException...");

            _getSerialNumberResult.setSuccess(false);

            if (e.getCause() != null) {
                _getSerialNumberResult.setMessage(e.getCause().getMessage());
            } else {
                _getSerialNumberResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            log.info("Getting serial Exception...");
            e.printStackTrace(System.out);

            _getSerialNumberResult.setSuccess(false);

            if (e.getCause() != null) {
                _getSerialNumberResult.setMessage(e.getCause().getMessage());
            } else {
                _getSerialNumberResult.setMessage(e.getMessage());
            }

        }

        log.info("Got serial: " + _getSerialNumberResult.getValue());

        return _getSerialNumberResult;

    }

    private ResultObject _getEncoderListResult;

    public ResultObject getEncoderList() {

        _getEncoderListResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() {

                    CardTerminalFacade terminalFacade = new CardTerminalFacade();

                    String encoderList = terminalFacade.getEncoderListJSON();

                    _getEncoderListResult.setSuccess(true);
                    _getEncoderListResult.setValue(encoderList);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();

            _getEncoderListResult.setSuccess(false);

            if (e.getCause() != null) {
                _getEncoderListResult.setMessage(e.getCause().getMessage());
            } else {
                _getEncoderListResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _getEncoderListResult.setSuccess(false);

            if (e.getCause() != null) {
                _getEncoderListResult.setMessage(e.getCause().getMessage());
            } else {
                _getEncoderListResult.setMessage(e.getMessage());
            }

        }

        return _getEncoderListResult;

    }

    private String _encoderName;
    private String _encoderNameResult;

    public String getEncoderName(String encoderName) {

        _encoderName = encoderName;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() {

                    CardTerminalFacade terminalFacade = new CardTerminalFacade();

                    _encoderNameResult = terminalFacade.getEncoderName(_encoderName);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();

            _encoderNameResult = "PROBLEM!";

        }

        return _encoderNameResult;

    }

    private boolean _isCardPresent;

    public boolean isCardPresent() {

        _isCardPresent = false;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    if(cardType.equals(cardTypeDESFire)) {
                        log.info("Check if DESFire present.");
                        _isCardPresent = encoderDESFire.isCardPresent();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Check if ULC present.");
                        _isCardPresent = encoderUltralightC.isCardPresent();
                    }

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
        }

        return _isCardPresent;

    }

    private boolean _isCardConnected;

    public boolean connectCard() {

        _isCardConnected = false;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    if(cardType.equals(cardTypeDESFire)) {
                        _isCardConnected = encoderDESFire.connectCard();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        _isCardConnected = encoderUltralightC.connectCard();
                    }

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
        }

        return _isCardConnected;

    }

    private ResultObject _getCardPIDResult;

    public ResultObject getCardPID() {

        _getCardPIDResult = new ResultObject();
        
        if(!isEncoderSet()) {
            _getCardPIDResult.setSuccess(false);
            _getCardPIDResult.setMessage("Internal error, could not retrieve PID. Please contact support.");
            return _getCardPIDResult;
        }

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    
                    String cardPID = null;

                    if(cardType.equals(cardTypeDESFire)) {
                        log.info("Card type was DESFire");
                        cardPID = encoderDESFire.getCardPID();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Card type was ULC");
                        cardPID = encoderUltralightC.getCardPID();
                    }

                    _getCardPIDResult.setSuccess(true);
                    _getCardPIDResult.setValue(cardPID);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _getCardPIDResult.setSuccess(false);

            if (e.getCause() != null) {
                _getCardPIDResult.setMessage(e.getCause().getMessage());
            } else {
                _getCardPIDResult.setMessage(e.getMessage());
            }
        }

        return _getCardPIDResult;

    }

    private ResultObject _encodeCardResult;

    private int _encodeCardUserProfile;
    private int _encodeCardLanguage;
    private int _encodeCardAccessibility;

    private String _encodeCardPeriodPassProductJSON;
    private String _encodeCardStoredRideProductJSON;
    private String _encodeCardStoredValueProductJSON;

    public ResultObject encodeCard(String cardPID, Integer userProfile, Integer language, Integer accessibility,
        String periodPassProductJSON, String storedRideProductJSON, String storedValueProductJSON) {

        _encodeCardResult = new ResultObject();
        
        if(!isEncoderSet()) {
            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Internal error, could not encode card. Please contact support.");
            return _encodeCardResult;
        }

        _encodeCardUserProfile = userProfile == null ? 0 : userProfile;
        _encodeCardLanguage = language == null ? 0 : language;
        _encodeCardAccessibility = accessibility == null ? 0 : accessibility;

        _encodeCardPeriodPassProductJSON = periodPassProductJSON;
        _encodeCardStoredRideProductJSON = storedRideProductJSON;
        _encodeCardStoredValueProductJSON = storedValueProductJSON;

        ResultObject cardPIDResult = getCardPID();

        // Validate that card is still there and has not been switched
        if (cardPID == null || !cardPIDResult.getSuccess() || !cardPID.equalsIgnoreCase(cardPIDResult.getValue())) {

            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Could not read card Printed ID. Retry card on reader, or card may be damaged.");

            return _encodeCardResult;

        }

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    PeriodPassProduct periodPassProduct = null;
                    StoredRideProduct storedRideProduct = null;
                    StoredValueProduct storedValueProduct = null;

                    // Get Period Pass Product
                    if (_encodeCardPeriodPassProductJSON != null) {
                        log.info("Deserializing period pass product JSON: " + _encodeCardPeriodPassProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            periodPassProduct = objMapper.readValue(_encodeCardPeriodPassProductJSON, PeriodPassProduct.class);
                            log.info("Got Period Pass Product! [" + periodPassProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        }

                    }

                    // Get Stored Ride Product
                    if (_encodeCardStoredRideProductJSON != null) {
                        log.info("Deserializing stored ride product JSON: " + _encodeCardStoredRideProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedRideProduct = objMapper.readValue(_encodeCardStoredRideProductJSON, StoredRideProduct.class);
                            log.info("Got Stored Ride Product! [" + storedRideProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        }

                    }

                    // Get Stored Value Product
                    if (_encodeCardStoredValueProductJSON != null) {
                        log.info("Deserializing stored value product JSON: " + _encodeCardStoredValueProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedValueProduct = objMapper.readValue(_encodeCardStoredValueProductJSON, StoredValueProduct.class);
                            log.info("Got Stored Value Product! [" + storedValueProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        }

                    }
                    
                    String cardPID = null;

                    cardPID = encoderDESFire.encodeCard(_encodeCardUserProfile, _encodeCardLanguage, _encodeCardAccessibility,
                        periodPassProduct, storedRideProduct, storedValueProduct);

                    _encodeCardResult.setSuccess(true);
                    _encodeCardResult.setValue(cardPID);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _encodeCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _encodeCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }

        }

        return _encodeCardResult;

    }
    
    public ResultObject encodeCardUltralightC(String cardPID, String periodPassProductJSON, String storedRideProductJSON, String storedValueProductJSON) {

        _encodeCardResult = new ResultObject();
        
        if(!isEncoderSet()) {
            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Internal error, could not encode card. Please contact support.");
            return _encodeCardResult;
        }

        _encodeCardPeriodPassProductJSON = periodPassProductJSON;
        _encodeCardStoredRideProductJSON = storedRideProductJSON;
        _encodeCardStoredValueProductJSON = storedValueProductJSON;

        ResultObject cardPIDResult = getCardPID();

        // Validate that card is still there and has not been switched
        if (cardPID == null || !cardPIDResult.getSuccess() || !cardPID.equalsIgnoreCase(cardPIDResult.getValue())) {
            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Card not read card Printed ID. Retry card on reader, or card may be damaged.");
            return _encodeCardResult;
        }

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws CardEncoderException {

                    String cardPID = null;
                    com.genfare.smartcard.ultralightc.bean.PeriodPassProduct periodPassProduct = null;
                    com.genfare.smartcard.ultralightc.bean.StoredRideProduct storedRideProduct = null;
                    com.genfare.smartcard.ultralightc.bean.StoredValueProduct storedValueProduct = null;
                    
                    // Get Period Pass Product
                    if (_encodeCardPeriodPassProductJSON != null) {
                        log.info("Deserializing period pass product JSON: " + _encodeCardPeriodPassProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            periodPassProduct = objMapper.readValue(_encodeCardPeriodPassProductJSON, com.genfare.smartcard.ultralightc.bean.PeriodPassProduct.class);
                            log.info("Got Period Pass Product! [" + periodPassProduct.toString() + "]");
                            cardPID = encoderUltralightC.encodeCard(periodPassProduct);
                            log.info("Encoded and returned card PID: "+cardPID);

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        }

                    } else if (_encodeCardStoredRideProductJSON != null) { // Get Stored Ride Product
                        log.info("Deserializing stored ride product JSON: " + _encodeCardStoredRideProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedRideProduct = objMapper.readValue(_encodeCardStoredRideProductJSON, com.genfare.smartcard.ultralightc.bean.StoredRideProduct.class);
                            log.info("Got Stored Ride Product! [" + storedRideProduct.toString() + "]");
                            cardPID = encoderUltralightC.encodeCard(storedRideProduct);
                            log.info("Encoded and returned card PID: "+cardPID);

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        }

                    } else if (_encodeCardStoredValueProductJSON != null) {
                        log.info("Deserializing stored value product JSON: " + _encodeCardStoredValueProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedValueProduct = objMapper.readValue(_encodeCardStoredValueProductJSON, com.genfare.smartcard.ultralightc.bean.StoredValueProduct.class);
                            log.info("Got Stored Value Product! [" + storedValueProduct.toString() + "]");
                            cardPID = encoderUltralightC.encodeCard(storedValueProduct);
                            log.info("Encoded and returned card PID: "+cardPID);

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        }

                    }
                    
                    _encodeCardResult.setSuccess(true);
                    _encodeCardResult.setValue(cardPID);
                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _encodeCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _encodeCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }

        }

        return _encodeCardResult;

    } //TODO bookmark delete

    private String _encodeCardProductListJSON;

    public ResultObject encodeCard(String cardPID, Integer userProfile, Integer language, Integer accessibility, String productListJSON) {
        log.info("attempting to encode cards using product list: " + productListJSON);
        _encodeCardResult = new ResultObject();
        
        if(!isEncoderSet()) {
            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Internal error, could not encode card. Please contact support.");
            return _encodeCardResult;
        }

        _encodeCardUserProfile = userProfile == null ? 0 : userProfile;
        _encodeCardLanguage = language == null ? 0 : language;
        _encodeCardAccessibility = accessibility == null ? 0 : accessibility;
        _encodeCardProductListJSON = productListJSON;

        ResultObject cardPIDResult = getCardPID();

        // Validate that card is still there and has not been switched
        if (cardPID == null || !cardPIDResult.getSuccess() || !cardPID.equalsIgnoreCase(cardPIDResult.getValue())) {
            _encodeCardResult.setSuccess(false);
            _encodeCardResult.setMessage("Card not read card Printed ID. Retry card on reader, or card may be damaged.");
            return _encodeCardResult;
        }

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    List<ProductBase> products = null;

                    String cardPID = null;
                    String value = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {

                        try {
                            ObjectMapper objMapper = new ObjectMapper();
                            
                            // Get Products
                            if (_encodeCardProductListJSON != null) {
                                objMapper.addMixInAnnotations(ProductBase.class, ProductBaseMixIn.class);
                            }
                            products = objMapper.readValue(_encodeCardProductListJSON,
                                TypeFactory.defaultInstance().constructCollectionType(List.class, ProductBase.class));
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        }
                        cardPID = encoderDESFire.encodeCard(_encodeCardUserProfile, _encodeCardLanguage, _encodeCardAccessibility, products);
                        try {
                            ObjectMapper objMapper = new ObjectMapper();
                            value = objMapper.writeValueAsString(products);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        _encodeCardResult.setSuccess(false);
                        _encodeCardResult.setValue("Internal error. Incorrect function call for Ultralight-C. Contact support.");
                    }
                    

                    _encodeCardResult.setSuccess(true);
                    _encodeCardResult.setValue(value);
                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
            _encodeCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            _encodeCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _encodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _encodeCardResult.setMessage(e.getMessage());
            }
        }
        return _encodeCardResult;
    }

    private ResultObject _bulkEncodeCardResult;

    private int _bulkEncodeCardUserProfile;
    private int _bulkEncodeCardLanguage;
    private int _bulkEncodeCardAccessibility;

    private String _bulkEncodeCardPeriodPassProductJSON;
    private String _bulkEncodeCardStoredRideProductJSON;
    private String _bulkEncodeCardStoredValueProductJSON;

    private int _bulkEncodeCardDockDelay;
    private int _bulkEncodeCardEjectDelay;

    public ResultObject bulkEncodeCard(Integer userProfile, Integer language, Integer accessibility,
        String periodPassProductJSON, String storedRideProductJSON, String storedValueProductJSON, int dockDelay, int ejectDelay) {

        _bulkEncodeCardResult = new ResultObject();

        _bulkEncodeCardUserProfile = userProfile == null ? 0 : userProfile;
        _bulkEncodeCardLanguage = language == null ? 0 : language;
        _bulkEncodeCardAccessibility = accessibility == null ? 0 : accessibility;

        _bulkEncodeCardPeriodPassProductJSON = periodPassProductJSON;
        _bulkEncodeCardStoredRideProductJSON = storedRideProductJSON;
        _bulkEncodeCardStoredValueProductJSON = storedValueProductJSON;

        _bulkEncodeCardDockDelay = dockDelay;
        _bulkEncodeCardEjectDelay = ejectDelay;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    PeriodPassProduct periodPassProduct = null;
                    StoredRideProduct storedRideProduct = null;
                    StoredValueProduct storedValueProduct = null;

                    // Get Period Pass Product
                    if (_encodeCardPeriodPassProductJSON != null) {
                        log.info("Deserializing period pass product JSON: " + _bulkEncodeCardPeriodPassProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            periodPassProduct = objMapper.readValue(_bulkEncodeCardPeriodPassProductJSON, PeriodPassProduct.class);
                            log.info("Got Period Pass Product! [" + periodPassProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Period Pass invalid", e);
                        }

                    }

                    // Get Stored Ride Product
                    if (_encodeCardStoredRideProductJSON != null) {
                        log.info("Deserializing stored ride product JSON: " + _bulkEncodeCardStoredRideProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedRideProduct = objMapper.readValue(_bulkEncodeCardStoredRideProductJSON, StoredRideProduct.class);
                            log.info("Got Stored Ride Product! [" + storedRideProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Ride invalid", e);
                        }

                    }

                    // Get Stored Value Product
                    if (_bulkEncodeCardStoredValueProductJSON != null) {
                        log.info("Deserializing stored value product JSON: " + _bulkEncodeCardStoredValueProductJSON);

                        ObjectMapper objMapper = new ObjectMapper();

                        try {

                            storedValueProduct = objMapper.readValue(_bulkEncodeCardStoredValueProductJSON, StoredValueProduct.class);
                            log.info("Got Stored Value Product! [" + storedValueProduct.toString() + "]");

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Stored Value invalid", e);
                        }

                    }
                    
                    String cardPID = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        cardPID = encoderDESFire.bulkEncodeCard(_bulkEncodeCardUserProfile, _bulkEncodeCardLanguage, _bulkEncodeCardAccessibility,
                            periodPassProduct, storedRideProduct, storedValueProduct, _bulkEncodeCardDockDelay, _bulkEncodeCardEjectDelay);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        throw new CardEncoderException("Bulk operations not supported for Ultralight-C.");
                    }

                    _bulkEncodeCardResult.setSuccess(true);
                    _bulkEncodeCardResult.setValue(cardPID);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _bulkEncodeCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _bulkEncodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _bulkEncodeCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _bulkEncodeCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _bulkEncodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _bulkEncodeCardResult.setMessage(e.getMessage());
            }

        }

        return _bulkEncodeCardResult;

    }

    private ResultObject _addProductsResult;

    private String _addProductsProductListJSON;

    public ResultObject addProducts(String cardPID, String productListJSON) {

        _addProductsResult = new ResultObject();

        if(!isEncoderSet()) {
            _addProductsResult.setSuccess(false);
            _addProductsResult.setMessage("Internal error, could not add products. Please contact support.");
            return _addProductsResult;
        }

        _addProductsProductListJSON = productListJSON;

        ResultObject cardPIDResult = getCardPID();

        // Validate that card is still there and has not been switched
        if (cardPID == null || !cardPIDResult.getSuccess() || !cardPID.equalsIgnoreCase(cardPIDResult.getValue())) {

            _addProductsResult.setSuccess(false);
            _addProductsResult.setMessage("Card not read card Printed ID. Retry card on reader, or card may be damaged.");

            return _addProductsResult;

        }

        log.info("Products: " + productListJSON);

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    List<ProductBase> products = null;

                    // Get Period Pass Product
                    if (_addProductsProductListJSON != null) {

                        ObjectMapper objMapper = new ObjectMapper();

                        objMapper.addMixInAnnotations(ProductBase.class, ProductBaseMixIn.class);

                        try {

                            products = objMapper.readValue(_addProductsProductListJSON,
                                TypeFactory.defaultInstance().constructCollectionType(List.class, ProductBase.class));

                        } catch (JsonParseException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CardEncoderException("Product List invalid", e);
                        }

                    }
                    
                    String cardPID = null;

                    if(cardType.equals(cardTypeDESFire)) {
                        cardPID = encoderDESFire.addProducts(products);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Method not implemented, please use encodeCardUltralightC()");
                    }
                    

                    _addProductsResult.setSuccess(true);
                    _addProductsResult.setValue(cardPID);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _addProductsResult.setSuccess(false);
            if (e.getCause() != null) {
                _addProductsResult.setMessage(e.getCause().getMessage());
            } else {
                _addProductsResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _addProductsResult.setSuccess(false);

            if (e.getCause() != null) {
                _addProductsResult.setMessage(e.getCause().getMessage());
            } else {
                _addProductsResult.setMessage(e.getMessage());
            }

        }

        return _addProductsResult;

    }

    private ResultObject _updateCardDataResult;
    private String _field;
    private Date _inputDate;
    private String _input;
    private short _inputShort;

    public ResultObject updateCardData(String field, String input) {
        _updateCardDataResult = new ResultObject();

        _field = field;
        _input = input;
        log.info("Applet: Entering updateCardData for field: " + field + " inputDate: " + input);

        if(!isEncoderSet()) {
            _updateCardDataResult.setSuccess(false);
            _updateCardDataResult.setMessage("Internal error, encoder was not properly set.");
            return _updateCardDataResult;
        } else if(field == null) {
            _updateCardDataResult.setSuccess(false);
            _updateCardDataResult.setMessage("Choose field to update");
            return _updateCardDataResult;
        }

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    log.info("Updating card details field: " + _field + " input is: " + _input);
                    
                    String cardPID = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        log.info("Updating a DESFire card");
                        
                        if(_input != null) {
                            if(_field.equals("changeuserprofile")) { // User profile needs input short
                                log.info("Change user profile");
                                cardPID = encoderDESFire.updateCardData(_field, null, Short.parseShort(_input));
                            } else { // Process date if date is input
                                log.info("Fixing date and field was  "+_field);
                                try {
                                    _inputDate = new SimpleDateFormat("MM/dd/yyyy").parse(_input);
                                    log.info("Date object generated as :"+_inputDate.toString());
                                    if(_inputDate == null) {
                                        _updateCardDataResult.setSuccess(false);
                                        _updateCardDataResult.setMessage("Invalid input date format.");
                                        return true;
                                    }
                                    log.info("Input date: "+_input+" parsed as: "+_inputDate);
                                } catch (ParseException e1) {
                                    log.info("Parse exception");
                                    _updateCardDataResult.setSuccess(false);
                                    _updateCardDataResult.setMessage("Invalid date format");
                                    return true;
                                }
                                cardPID = encoderDESFire.updateCardData(_field, _inputDate, new Short("0"));
                            }
                        } else {
                            log.info("Input null and field was "+_field);
                            cardPID = encoderDESFire.updateCardData(_field, null, new Short("0"));
                            _updateCardDataResult.setSuccess(true);
                            _updateCardDataResult.setMessage("Successful encode operation.");
                            log.info("Completed");
                            return true;
                        }
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        if(_field.equals("changecardexp") ||
                            _field.equals("changeulccardstart") ||
                            _field.equals("changeulcperioddays")) {
                            log.info("Updating a ULC card");
                            
                            try{
                                log.info("Sending update request for "+_field);
                                cardPID = encoderUltralightC.updateCardData(_field, Short.parseShort(_input));
                                log.info("Got update response.");
                            } catch(NumberFormatException e2) {
                                _updateCardDataResult.setSuccess(false);
                                _updateCardDataResult.setValue(cardPID);
                            }
                        } else if(_field.equals("togglebadlistcard")) {
                            log.info("Sending update request for card exp.");
                            cardPID = encoderUltralightC.updateCardData(_field, null);
                            log.info("Got update response.");
                        } else {
                            _updateCardDataResult.setMessage("Internal error- Operation not implemented for this card type.");
                            _updateCardDataResult.setSuccess(false);
                            _updateCardDataResult.setValue(cardPID);
                        }
                    }
                    
                    _updateCardDataResult.setSuccess(true);
                    _updateCardDataResult.setValue(cardPID);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _updateCardDataResult.setSuccess(false);
            if (e.getCause() != null) {
                _updateCardDataResult.setMessage(e.getCause().getMessage());
            } else {
                _updateCardDataResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            _updateCardDataResult.setSuccess(false);
            if (e.getCause() != null) {
                _updateCardDataResult.setMessage(e.getCause().getMessage());
            } else {
                _updateCardDataResult.setMessage(e.getMessage());
            }
        }

        return _updateCardDataResult;
    }

    private ResultObject _updateProductDatesResult;
    private Integer _productNumber;
    private Date _startDate;
    private Date _endDate;

    public ResultObject updateProductDates(String cardPID, Integer productNumber, String startDate, String endDate) {

        _updateProductDatesResult = new ResultObject();
        log.info("Updating product dates for slot number: " + _productNumber + " with start date: " + startDate + " and endDate " + endDate);
        
        if(!isEncoderSet()) {
            _updateProductDatesResult.setSuccess(false);
            _updateProductDatesResult.setMessage("Internal error, could not update product dates as encoder was not set. Contact support.");
            return _updateProductDatesResult;
        }

        _productNumber = productNumber;
        log.info("try to parse dates");
        try {
            _startDate = new SimpleDateFormat("MM/dd/yyyy").parse(startDate);
            _endDate = new SimpleDateFormat("MM/dd/yyyy").parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace(System.out);

            _updateProductDatesResult.setSuccess(false);
            if (e.getCause() != null) {
                _updateProductDatesResult.setMessage(e.getCause().getMessage());
            } else {
                _updateProductDatesResult.setMessage(e.getMessage());
            }
            return _updateProductDatesResult;
        }

        ResultObject cardPIDResult = getCardPID();

        // Validate that card is still there and has not been switched
        if (cardPID == null || !cardPIDResult.getSuccess() || !cardPID.equalsIgnoreCase(cardPIDResult.getValue())) {
            _updateProductDatesResult.setSuccess(false);
            _updateProductDatesResult.setMessage("Card not read card Printed ID. Retry card on reader, or card may be damaged.");

            return _updateProductDatesResult;
        }
        log.info("card PID validated");
        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    if (_productNumber == null) {
                        throw new CardEncoderException("Slot number not provided");
                    }
                    log.info("Updating product");
                    
                    String cardPID = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        cardPID = encoderDESFire.updateProductDates(_productNumber.byteValue(), _startDate, _endDate);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Function updateCardData not implemented for Ultralight-C.");
                    }

                    _updateProductDatesResult.setSuccess(true);
                    _updateProductDatesResult.setValue(cardPID);

                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _updateProductDatesResult.setSuccess(false);
            if (e.getCause() != null) {
                _updateProductDatesResult.setMessage(e.getCause().getMessage());
            } else {
                _updateProductDatesResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            _updateProductDatesResult.setSuccess(false);
            if (e.getCause() != null) {
                _updateProductDatesResult.setMessage(e.getCause().getMessage());
            } else {
                _updateProductDatesResult.setMessage(e.getMessage());
            }
        }
        return _updateProductDatesResult;
    }
    
    private ResultObject _getCardUserProfileResult;
    private ResultObject _setCardUserProfileResult;
    private short _setCardUserProfileUserProfile;
    
    public ResultObject getCardUserProfile() {
        _getCardUserProfileResult = new ResultObject();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    short userProfile = encoderDESFire.getCardUserProfile();

                    _getCardUserProfileResult.setSuccess(true);
                    _getCardUserProfileResult.setValue(String.valueOf(userProfile));

                    return Boolean.valueOf(true);
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _getCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null)
                _getCardUserProfileResult.setMessage(e.getCause().getMessage());
            else
                _getCardUserProfileResult.setMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _getCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null)
                _getCardUserProfileResult.setMessage(e.getCause().getMessage());
            else {
                _getCardUserProfileResult.setMessage(e.getMessage());
            }

        }

        return _getCardUserProfileResult;
    }

    public ResultObject setCardUserProfile(Short userProfile) {
        _setCardUserProfileResult = new ResultObject();
        _setCardUserProfileUserProfile = userProfile.shortValue();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    encoderDESFire.setCardUserProfile(_setCardUserProfileUserProfile);

                    _setCardUserProfileResult.setSuccess(true);
                    _setCardUserProfileResult.setMessage("Card User Profile Set to " + _setCardUserProfileUserProfile);

                    return Boolean.valueOf(true);
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _setCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null)
                _setCardUserProfileResult.setMessage(e.getCause().getMessage());
            else
                _setCardUserProfileResult.setMessage(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _setCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null)
                _setCardUserProfileResult.setMessage(e.getCause().getMessage());
            else {
                _setCardUserProfileResult.setMessage(e.getMessage());
            }

        }

        return _setCardUserProfileResult;
    }

    private ResultObject _adminSetCardUserProfileResult;
    private short _adminUserProfile;

    public ResultObject adminSetCardUserProfile(Integer userProfile) {

        _adminSetCardUserProfileResult = new ResultObject();

        _adminUserProfile = (short) userProfile.intValue();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    if(cardType.equals(cardTypeDESFire)) {
                        encoderDESFire.setCardUserProfile(_adminUserProfile);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Function not supported for Ultralight-C.");
                    }

                    _adminSetCardUserProfileResult.setSuccess(true);
                    _adminSetCardUserProfileResult.setMessage("Card User Profile Set to " + _adminUserProfile);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _adminSetCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminSetCardUserProfileResult.setMessage(e.getCause().getMessage());
            } else {
                _adminSetCardUserProfileResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _adminSetCardUserProfileResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminSetCardUserProfileResult.setMessage(e.getCause().getMessage());
            } else {
                _adminSetCardUserProfileResult.setMessage(e.getMessage());
            }

        }

        return _adminSetCardUserProfileResult;

    }

    private ResultObject _adminDumpCardResult;

    public ResultObject adminDumpCard() {

        _adminDumpCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    String message = null; 

                        if(cardType.equals(cardTypeDESFire)) {
                        message = encoderDESFire.adminDumpCard();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Function not supported for Ultralight-C.");
                    }

                    _adminDumpCardResult.setSuccess(true);
                    _adminDumpCardResult.setMessage(message);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _adminDumpCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminDumpCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminDumpCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _adminDumpCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminDumpCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminDumpCardResult.setMessage(e.getMessage());
            }

        }

        return _adminDumpCardResult;

    }

    private ResultObject _adminDecodeCardResult;

    public ResultObject adminDecodeCard() {

        _adminDecodeCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    String message = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        message = encoderDESFire.adminDecodeCard();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        log.info("Function not supported for Ultralight-C.");
                    }

                    _adminDecodeCardResult.setSuccess(true);
                    _adminDecodeCardResult.setMessage(message);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _adminDecodeCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminDecodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminDecodeCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _adminDecodeCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminDecodeCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminDecodeCardResult.setMessage(e.getMessage());
            }

        }

        return _adminDecodeCardResult;

    }

    private ResultObject _readCardResult;

    public String readCard() {
        _readCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController
        log.info("Entering adminReadCard");
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException, JsonProcessingException {
                    ObjectMapper mapper = new ObjectMapper();
                    String message = "";

                    try {
                        log.info("readCard");
                        log.info("cardtype "+cardType);
                        
                        if(cardType.equals(cardTypeDESFire)) {
                            TransitCard card = new TransitCard();
                            card = encoderDESFire.readCard(true);
                            log.info("mapping");
                            message = mapper.writeValueAsString(card);
                            _readCardResult.setSuccess(true);
                            _readCardResult.setMessage(message);
                        } else if(cardType.equals(cardTypeUltralightC)) {
                            TransitCardUltralightC card = new TransitCardUltralightC();
                            card = encoderUltralightC.readCardUltralightC();
                            message = mapper.writeValueAsString(card);
                            _readCardResult.setSuccess(true);
                            _readCardResult.setMessage(message);
                        }
                    } catch (Exception e) {
                        log.info("error calling readCard in encoderApplet: " + e);
                        message = "Error reading card.";
                        return false;
                    }
                    _readCardResult.setMessage(message);
                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
            _readCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _readCardResult.setMessage(e.getCause().getMessage());
            } else {
                _readCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            _readCardResult.setSuccess(false);
            if (e.getCause() != null) {
                _readCardResult.setMessage(e.getCause().getMessage());
            } else {
                _readCardResult.setMessage(e.getMessage());
            }
        }
        return _readCardResult.getMessage();
    }

    private ResultObject _quickReadCardResult;

    public String quickReadCard() {
    	_quickReadCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController
        log.info("Entering quickReadCard");
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException, JsonProcessingException {
                    ObjectMapper mapper = new ObjectMapper();
                    String message = "";

                    try {
                        log.info("readCard");
                        log.info("cardtype "+cardType);
                        
                        if(cardType.equals(cardTypeDESFire)) {
                            TransitCard card = new TransitCard();
                            card = encoderDESFire.quickReadCard();
                            log.info("mapping");
                            message = mapper.writeValueAsString(card);
                            _quickReadCardResult.setSuccess(true);
                            _quickReadCardResult.setMessage(message);
                        } else if(cardType.equals(cardTypeUltralightC)) {
                            TransitCardUltralightC card = new TransitCardUltralightC();
                            card = encoderUltralightC.quickReadCardUltralightC();
                            message = mapper.writeValueAsString(card);
                            _quickReadCardResult.setSuccess(true);
                            _quickReadCardResult.setMessage(message);
                        }
                    } catch (Exception e) {
                        log.info("error calling readCard in encoderApplet: " + e);
                        message = "Error reading card.";
                        return false;
                    }
                    _quickReadCardResult.setMessage(message);
                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
            _quickReadCardResult.setSuccess(false);
            if (e.getCause() != null) {
            	_quickReadCardResult.setMessage(e.getCause().getMessage());
            } else {
            	_quickReadCardResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
            _quickReadCardResult.setSuccess(false);
            if (e.getCause() != null) {
            	_quickReadCardResult.setMessage(e.getCause().getMessage());
            } else {
            	_quickReadCardResult.setMessage(e.getMessage());
            }
        }
        return _quickReadCardResult.getMessage();
    }

    private ResultObject _adminClearCardResult;
    private short _profile;
    private short _storedValueType;

    public ResultObject adminClearCard(short profile, short storedValueType) {
        
        log.info("Entering adminClearCard with profile "+profile+" and storedValueType "+storedValueType);

        _adminClearCardResult = new ResultObject();
        _profile = profile;
        _storedValueType = storedValueType;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    
                    String message = null;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        message = encoderDESFire.adminClearCard(_profile, _storedValueType);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        encoderUltralightC.clearCardUltralightC();
                        message = "Cleared card.";
                    }

                    _adminClearCardResult.setSuccess(true);
                    _adminClearCardResult.setMessage(message);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _adminClearCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminClearCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminClearCardResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _adminClearCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _adminClearCardResult.setMessage(e.getCause().getMessage());
            } else {
                _adminClearCardResult.setMessage(e.getMessage());
            }

        }

        return _adminClearCardResult;

    }

    // Check if bulk printer is installed

    private ResultObject _isBulkPrinterAvailableResult;

    public ResultObject isBulkPrinterAvailable() {

        _isBulkPrinterAvailableResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    CardTerminalFacade terminalFacade = new CardTerminalFacade();

                    Boolean result = (terminalFacade.findAvailableBulkPrinter() != null);

                    _isBulkPrinterAvailableResult.setSuccess(result);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _isBulkPrinterAvailableResult.setSuccess(false);

            if (e.getCause() != null) {
                _isBulkPrinterAvailableResult.setMessage(e.getCause().getMessage());
            } else {
                _isBulkPrinterAvailableResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _isBulkPrinterAvailableResult.setSuccess(false);

            if (e.getCause() != null) {
                _isBulkPrinterAvailableResult.setMessage(e.getCause().getMessage());
            } else {
                _isBulkPrinterAvailableResult.setMessage(e.getMessage());
            }

        }

        return _isBulkPrinterAvailableResult;

    }

    // print a card
    private ResultObject _didCardPrint;

    private String _nameField, _nameValue, _employeeIdField, _employeeIdValue,
        _expField, _expValue, _issField, _issValue, _imgData, _imgTemplate, _imgBack, _imgPortrait;

    public ResultObject printCard(String imgData, String imgTemplate,
        String imgBack, String imgPortrait,
        String nameField, String nameValue,
        String employeeIdField, String employeeIdValue,
        String expField, String expValue,
        String issField, String issValue) {

        _didCardPrint = new ResultObject();
        _nameField = nameField;
        _nameValue = nameValue;
        _employeeIdField = employeeIdField;
        _employeeIdValue = employeeIdValue;
        _expField = expField;
        _expValue = expValue;
        _issField = issField;
        _issValue = issValue;
        _imgData = imgData;
        _imgTemplate = imgTemplate;
        _imgBack = imgBack;
        _imgPortrait = imgPortrait;

        System.out.println("_nameField: " + _nameField);
        System.out.println("nameField: " + nameField);
        System.out.println("_nameValue: " + _nameValue);
        System.out.println("nameValue: " + nameValue);
        System.out.println("_employeeIdField: " + _employeeIdField);
        System.out.println("employeeIdField: " + employeeIdField);
        System.out.println("_employeeIdValue: " + _employeeIdValue);
        System.out.println("employeeIdValue: " + employeeIdValue);
        System.out.println("_expField: " + _expField);
        System.out.println("expField: " + expField);
        System.out.println("_expValue: " + _expValue);
        System.out.println("expValue: " + expValue);
        System.out.println("_issField: " + _issField);
        System.out.println("issField: " + issField);
        System.out.println("_issValue: " + _issValue);
        System.out.println("issValue: " + issValue);
        System.out.println("_imgData: " + ((_imgData != null) ? true : false));
        System.out.println("imgData: " + ((imgData != null) ? true : false));
        System.out.println("_imgTemplate: " + _imgTemplate);
        System.out.println("imgTemplate: " + imgTemplate);
        System.out.println("_imgBack: " + _imgBack);
        System.out.println("imgBack: " + imgBack);
        System.out.println("_imgPortrait: " + _imgPortrait);
        System.out.println("imgPortrait: " + imgPortrait);

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    System.out.println("Printing Card");
                    PrintCards p = new PrintCards();

                    PrintCardDTO textDTO = null;

                    System.out.println("_imgTemplate" + _imgTemplate);
                    System.out.println("_imgBack" + _imgBack);
                    System.out.println("_imgPortrait" + _imgPortrait);
                    System.out.println("_imgData" + _imgData);

                    p.setupTemplateImages(_imgTemplate, _imgBack, _imgPortrait, _imgData);
                    System.out.println("Setting up printer fields");
                    System.out.println("_nameField: " + _nameField);
                    System.out.println("is namefield null : " + (_nameField != null));

                    System.out.println("_nameValue: " + _nameValue);
                    System.out.println("_employeeIdField: " + _employeeIdField);
                    System.out.println("_employeeIdValue: " + _employeeIdValue);
                    System.out.println("_expField: " + _expField);
                    System.out.println("_expValue: " + _expValue);
                    System.out.println("_issField: " + _issField);
                    System.out.println("_issValue: " + _issValue);

                    if (_nameField != null) {
                        textDTO = mapJSONtoDTO(_nameField);
                        p.setupNameField(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    System.out.println("Required: should see name value JSON:");
                    if (_nameValue != null) {
                        textDTO = mapJSONtoDTO(_nameValue);
                        p.setupNameValue(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_employeeIdField != null) {
                        textDTO = mapJSONtoDTO(_employeeIdField);
                        p.setEmpIdField(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_employeeIdValue != null) {
                        textDTO = mapJSONtoDTO(_employeeIdValue);
                        p.setEmpIdValue(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_expField != null) {
                        textDTO = mapJSONtoDTO(_expField);
                        p.setExpField(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_expValue != null) {
                        textDTO = mapJSONtoDTO(_expValue);
                        p.setExpValue(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_issField != null) {
                        textDTO = mapJSONtoDTO(_issField);
                        p.setIssField(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }
                    if (_issValue != null) {
                        textDTO = mapJSONtoDTO(_issValue);
                        p.setIssValue(textDTO.getText(), textDTO.getX(), textDTO.getY(),
                            textDTO.getFontSize(), textDTO.getFont(), textDTO.getFontWeight());
                    }

                    try {
                        System.out.println("Sending print command");
                        p.doPrint();
                    } catch (PrintException e) {
                        throw new CardEncoderException("Configuration invalid PrintException: ", e);
                    } catch (InterruptedException e) {
                        throw new CardEncoderException("Configuration invalid InterruptedException: ", e);
                    } catch (NullPointerException e) {
                        throw new CardEncoderException("Configuration invalid NullPointerException: ", e);
                    }

                    _didCardPrint.setSuccess(true);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            System.out.println("PrivilegedActionException");
            e.printStackTrace(System.out);

            _didCardPrint.setSuccess(false);

            if (e.getCause() != null) {
                _didCardPrint.setMessage(e.getCause().getMessage());
            } else {
                _didCardPrint.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace(System.out);
            _didCardPrint.setSuccess(false);

            if (e.getCause() != null) {
                _didCardPrint.setMessage(e.getCause().getMessage());
            } else {
                _didCardPrint.setMessage(e.getMessage());
            }
        }
        return _didCardPrint;

    }

    private PrintCardDTO mapJSONtoDTO(String json) throws CardEncoderException {
        if (json != null) {
            PrintCardDTO configJSON;
            log.info("Deserializing config JSON mapping: " + json);
            ObjectMapper objMapper = new ObjectMapper();
            log.info("Got Object Mapper!");
            try {
                log.info("Trying to read config!");
                configJSON = objMapper.readValue(json, PrintCardDTO.class);
                log.info("Got Config! [" + configJSON.toString() + "]");
            } catch (JsonParseException e) {
                throw new CardEncoderException("Configuration invalid JsonParseException: ", e);
            } catch (JsonMappingException e) {
                throw new CardEncoderException("Configuration invalid JsonMappingException: ", e);
            } catch (IOException e) {
                throw new CardEncoderException("Configuration invalid IOException: ", e);
            }
            return configJSON;
        }
        return null;
    }

    // Dock card into bulk encoder

    private ResultObject _dockCardResult;

    public ResultObject dockCard() {

        _dockCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    
                    Boolean result = false;
                    
                    if(cardType.equals(cardTypeDESFire)) {
                        result = encoderDESFire.dockCard();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        _dockCardResult.setSuccess(false);
                        _dockCardResult.setMessage("Internal error, Ultralight C does not support bulk encoder at this time.");
                        return false;
                    }

                    _dockCardResult.setSuccess(result);
                    //TODO dockCardResult not returned?
                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _dockCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _dockCardResult.setMessage(e.getCause().getMessage());
            } else {
                _dockCardResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _dockCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _dockCardResult.setMessage(e.getCause().getMessage());
            } else {
                _dockCardResult.setMessage(e.getMessage());
            }

        }

        return _dockCardResult;

    }

    // Eject Card from bulk encoder

    private ResultObject _ejectCardResult;

    public ResultObject ejectCard() {

        _ejectCardResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    
                    Boolean success = false;

                    if(cardType.equals(cardTypeDESFire)) {
                        success = encoderDESFire.ejectCard();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        //Bulk ULC currently unsupported
                        //success = encoderDESFire.ejectCard();
                        throw new CardEncoderException("Bulk operations not implemented for LUCC");
                    }

                    _ejectCardResult.setSuccess(success);

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _ejectCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _ejectCardResult.setMessage(e.getCause().getMessage());
            } else {
                _ejectCardResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _ejectCardResult.setSuccess(false);

            if (e.getCause() != null) {
                _ejectCardResult.setMessage(e.getCause().getMessage());
            } else {
                _ejectCardResult.setMessage(e.getMessage());
            }

        }

        return _ejectCardResult;

    }

    // Get count of queued jobs on bulk encoder

    private ResultObject _getQueuedJobCountResult;

    public ResultObject getQueuedJobCount() {

        _getQueuedJobCountResult = new ResultObject();

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    
                    int queuedJobCount = 0;

                    if(cardType.equals(cardTypeDESFire)) {
                        queuedJobCount = encoderDESFire.getQueuedJobCount();
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        //Bulk ULC unsupported
                        //queuedJobCount = encoderUltralightC.getQueuedJobCount();
//TODO error
                    }

                    _getQueuedJobCountResult.setSuccess(true);
                    _getQueuedJobCountResult.setValue(Integer.toString(queuedJobCount));

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _getQueuedJobCountResult.setSuccess(false);

            if (e.getCause() != null) {
                _getQueuedJobCountResult.setMessage(e.getCause().getMessage());
            } else {
                _getQueuedJobCountResult.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);

            _getQueuedJobCountResult.setSuccess(false);

            if (e.getCause() != null) {
                _getQueuedJobCountResult.setMessage(e.getCause().getMessage());
            } else {
                _getQueuedJobCountResult.setMessage(e.getMessage());
            }

        }

        return _getQueuedJobCountResult;

    }
    
    private ResultObject _setCardBadlist;
    private boolean _isCardBadlisted;
    
    public ResultObject setCardBadlist(boolean isCardBadlisted) {

        _setCardBadlist = new ResultObject();
        _isCardBadlisted = isCardBadlisted;
        log.info("Encoder, setting card badlist to " + isCardBadlisted);

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {
                    if(cardType.equals(cardTypeDESFire)) {
                        log.info("setCardBadlist("+_isCardBadlisted+")");
                        String message = encoderDESFire.setCardBadlist(_isCardBadlisted);
                        _setCardBadlist.setSuccess(true);
                        _setCardBadlist.setMessage(message);
                        log.info("Set card badlist reports: "+message);
                    } else if(cardType.equals(cardTypeUltralightC)) {
                        throw new CardEncoderException("Badlist operations not implemented for LUCC");
                    }
                    return true;
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);
            _setCardBadlist.setSuccess(false);
            if (e.getCause() != null) {
                _setCardBadlist.setMessage(e.getCause().getMessage());
            } else {
                _setCardBadlist.setMessage(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            _setCardBadlist.setSuccess(false);
            if (e.getCause() != null) {
                _setCardBadlist.setMessage(e.getCause().getMessage());
            } else {
                _setCardBadlist.setMessage(e.getMessage());
            }
        }
        return _setCardBadlist;
    }

    private ResultObject _throwExceptionResult;

    private int x;

    /**
     * Throws an exception, just to see what happens... :)
     * 
     * @param y
     * @return
     */
    public ResultObject throwException(int y) {

        _throwExceptionResult = new ResultObject();

        x = y;

        // In order for JavaScript to call privileged functions without being signed,
        // wrap call in AccessController

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() {

                    if (x == 0) {

                        throw new RuntimeException("NOOOO!!!");

                    }

                    if (x == 1) {

                        throw new RuntimeException("x can't be 1!");

                    }

                    _throwExceptionResult.setSuccess(true);
                    _throwExceptionResult.setMessage("Function completed!");

                    return true;
                }
            });
        } catch (PrivilegedActionException e) {

            log.severe("CAUGHT PrivilegedActionException!!!!");
            e.printStackTrace(System.out);

        } catch (Exception e) {

            log.severe("EXCEPTION");

            e.printStackTrace(System.out);

            _throwExceptionResult.setSuccess(false);
            _throwExceptionResult.setMessage(e.getMessage());

        }

        return _throwExceptionResult;

    }
    
    public void setCardTypeDESFire() {
        cardType = "DESFire";
        log.info("cardType is now: "+cardType);
    }
    
    public void setCardTypeUltralightC() {
        cardType = "UltralightC";
        log.info("cardType is now: "+cardType);
    }
    
    private boolean isEncoderSet() {
        log.info("encoderDESFire: "+encoderDESFire);
        log.info("encoderUltralightC: "+encoderUltralightC);
        log.info("cardType: "+cardType);
        if(encoderDESFire == null && encoderUltralightC == null){
           return false;
        } else if (cardType.equals(cardTypeDESFire) && encoderDESFire == null) {
            return false;
        } else if(cardType.equals(cardTypeUltralightC) && encoderUltralightC == null) {
            return false;
        } else {
            return true;
        }
    }
    
    
    private String _bonusOrCappedType;
    private short _bonusOrCappedDes;
    private short _bonusOrCappedValue;
    private ResultObject _addBonusResult;

    public ResultObject createBonusOrCappedTransfer(String type, String designator, String value) {
        log.info("attempting to add "+ type +" products");
        _addBonusResult = new ResultObject();
        _bonusOrCappedType = type;
        if (_bonusOrCappedType == null || _bonusOrCappedType.trim().isEmpty()) {
            _addBonusResult.setSuccess(false);
            _addBonusResult.setMessage("encode.applet.error.missing.bonus.capped");
            log.warning("bonus products not added, type is missing.");
            return _addBonusResult;
        }

        try {
            _bonusOrCappedDes = Short.parseShort(designator);
            _bonusOrCappedValue = Short.parseShort(value);
            AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws CardEncoderException {

                    Transfer newTransfer = new Transfer();
                    
                    newTransfer.setDesignator(_bonusOrCappedDes);
                    newTransfer.setRideCount(_bonusOrCappedValue);
                    
                    log.info("Setting designator to " + _bonusOrCappedDes);
                    log.info("Setting value to " + _bonusOrCappedValue);
                    boolean success = false;
                    try {
                        if (_bonusOrCappedType.equalsIgnoreCase("BONUS")) {
                            encoderDESFire.addTransfer(newTransfer, FileNumber.TRANSFER3);
                            success = true;
                        } else if (_bonusOrCappedType.equalsIgnoreCase("CAPPED")) {
                            encoderDESFire.addTransfer(newTransfer, FileNumber.TRANSFER2);
                            success = true;
                        } else {
                            log.warning("Unknown bonus capped type " + _bonusOrCappedType);
                        }                     
                        
                    } catch (CardEncoderException cardEx) {
                        cardEx.printStackTrace();
                    }
                    
                    _addBonusResult.setSuccess(success);
                    return true;
                }
            });

        } catch (PrivilegedActionException e) {
            e.printStackTrace(System.out);

            _addBonusResult.setSuccess(false);
            if (e.getCause() != null) {
                _addBonusResult.setMessage(e.getCause().getMessage());
            } else {
                _addBonusResult.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);

            _addBonusResult.setSuccess(false);

            if (e.getCause() != null) {
                _addBonusResult.setMessage(e.getCause().getMessage());
            } else {
                _addBonusResult.setMessage(e.getMessage());
            }

        }

        return _addBonusResult;
    }
}

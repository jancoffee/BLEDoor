package bledoor.river.se.bledoor;

import java.util.UUID;

/**
 * Parse UUIDs into descriptive string
 */
class UUIDParser{

    //java.util.HashMap<String, Integer> batteryUUIDinfo = new java.util.HashMap<String, Integer>();

    //Battery power
    static final String BATTERY_SERVICE_UUID = "180f";
    static final String BATTERY_SERVICE_LEVEL_UUID = "2a19";
    static final String BATTERY_CLIENT_CHAR_CONF_UUID= "2902";
    static final String BATTERY_SERVICE_GATT_REPORT_REF_UUID = "2908";

    //Transmitt power
    static final String TX_PWR_LEVEL_SERVICE_UUID = "1804";
    static final String PROXIMITY_TX_POWER_LEVEL_UUID = "2a07";
    static final String PROXIMITY_GATT_CLIENT_CHAR_CFG_UID = "2902";

    //Immediate alert
    static final String IMMEDIATE_ALERT_SERVICE_UUID = "1802";
    static final String IMMEDIATE_GATT_CHARACTER_UUID = "2803";
    static final String IMMEDIATE_PROXIMITY_ALERT_LEVEL_UUID = "2a06";

    //Generic Access
    static final String GAP_SERVICE_UUID = "1800";

    //Generic Attribute
    static final String GATT_SERVICE_UUID = "1801";

    //Device Information
    static final String DEVINFO_SERV_UUID = "180a";

    //Link loss
    static final String LINK_LOST_SERVICE_UUID = "1803";

    static public String Parse(UUID input){

        String uuid[] = input.toString().split("\\-");
        String retval;
        //Battery
        if(uuid[0].contains(BATTERY_SERVICE_UUID))
            retval = "BATTERY_SERVICE_UUID";
        else if(uuid[0].contains(BATTERY_SERVICE_LEVEL_UUID))
            retval = "BATTERY_SERVICE_LEVEL_UUID";
        else if(uuid[0].contains(BATTERY_CLIENT_CHAR_CONF_UUID))
            retval = "BATTERY_CLIENT_CHAR_CONF_UUID";
        else if(uuid[0].contains(BATTERY_SERVICE_GATT_REPORT_REF_UUID))
            retval = "BATTERY_SERVICE_GATT_REPORT_REF_UUID";

            //TX power
        else if(uuid[0].contains(TX_PWR_LEVEL_SERVICE_UUID))
            retval = "TX_PWR_LEVEL_SERVICE_UUID";
        else if(uuid[0].contains(PROXIMITY_TX_POWER_LEVEL_UUID))
            retval = "PROXIMITY_TX_POWER_LEVEL_UUID";
        else if(uuid[0].contains(PROXIMITY_GATT_CLIENT_CHAR_CFG_UID))
            retval = "PROXIMITY_GATT_CLIENT_CHAR_CFG_UID";

            //Alert
        else if(uuid[0].contains(IMMEDIATE_ALERT_SERVICE_UUID))
            retval = "IMMEDIATE_ALERT_SERVICE_UUID";
        else if(uuid[0].contains(IMMEDIATE_GATT_CHARACTER_UUID))
            retval = "IMMEDIATE_GATT_CHARACTER_UUID";
        else if(uuid[0].contains(IMMEDIATE_PROXIMITY_ALERT_LEVEL_UUID))
            retval = "IMMEDIATE_PROXIMITY_ALERT_LEVEL_UUID";

            //Generic Access
        else if(uuid[0].contains(GAP_SERVICE_UUID))
            retval = "GAP_SERVICE_UUID";

            //Generic Attribute
        else if(uuid[0].contains(GATT_SERVICE_UUID))
            retval = "GATT_SERVICE_UUID";

            //Device Information
        else if(uuid[0].contains(DEVINFO_SERV_UUID))
            retval = "DEVINFO_SERV_UUID";

            //Link loss
        else if(uuid[0].contains(LINK_LOST_SERVICE_UUID))
            retval = "LINK_LOST_SERVICE_UUID";

        else {
            retval = uuid[0].replace("0000", "0x");
            return retval;
        }
        return retval+" ("+uuid[0].replace("0000", "0x")+") ";
    }
}

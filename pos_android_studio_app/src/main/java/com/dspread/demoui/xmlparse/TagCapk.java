package com.dspread.demoui.xmlparse;

import com.dspread.demoui.xmlparse.BaseTag;

/**
 * Created by dsppc11 on 2018/7/31.
 */

public class TagCapk extends BaseTag {
    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    private String RID ;
    private String Public_Key_Index ;
    private String Public_Key_Module ;
    private String Public_Key_CheckValue ;
    private String Pk_exponent ;
    private String Expired_date ;
    private String Hash_algorithm_identification ;
    private String Pk_algorithm_identification ;

    public String getPublic_Key_Index() {
        return Public_Key_Index;
    }

    public void setPublic_Key_Index(String public_Key_Index) {
        Public_Key_Index = public_Key_Index;
    }

    public String getPublic_Key_Module() {
        return Public_Key_Module;
    }

    public void setPublic_Key_Module(String public_Key_Module) {
        Public_Key_Module = public_Key_Module;
    }

    public String getPublic_Key_CheckValue() {
        return Public_Key_CheckValue;
    }

    public void setPublic_Key_CheckValue(String public_Key_CheckValue) {
        Public_Key_CheckValue = public_Key_CheckValue;
    }

    public String getPk_exponent() {
        return Pk_exponent;
    }

    public void setPk_exponent(String pk_exponent) {
        Pk_exponent = pk_exponent;
    }

    public String getExpired_date() {
        return Expired_date;
    }

    public void setExpired_date(String expired_date) {
        Expired_date = expired_date;
    }

    public String getHash_algorithm_identification() {
        return Hash_algorithm_identification;
    }

    public void setHash_algorithm_identification(String hash_algorithm_identification) {
        Hash_algorithm_identification = hash_algorithm_identification;
    }

    public String getPk_algorithm_identification() {
        return Pk_algorithm_identification;
    }

    public void setPk_algorithm_identification(String pk_algorithm_identification) {
        Pk_algorithm_identification = pk_algorithm_identification;
    }


    @Override
    public String toString() {
        return "TagCapk{" +
                "RID='" + RID + '\'' +
                ", Public_Key_Index='" + Public_Key_Index + '\'' +
                ", Public_Key_Module='" + Public_Key_Module + '\'' +
                ", Public_Key_CheckValue='" + Public_Key_CheckValue + '\'' +
                ", Pk_exponent='" + Pk_exponent + '\'' +
                ", Expired_date='" + Expired_date + '\'' +
                ", Hash_algorithm_identification='" + Hash_algorithm_identification + '\'' +
                ", Pk_algorithm_identification='" + Pk_algorithm_identification + '\'' +
                '}';
    }

}

package com.dspread.demoui.xmlparse;


import android.text.TextUtils;

import com.dspread.xpos.EmvAppTag;
import com.dspread.xpos.EmvCapkTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;

/**
 * Created by dsppc11 on 2018/7/31.
 */

public class SAXParserHandler extends DefaultHandler {
    StringBuffer value = new StringBuffer();
    private List<TagApp> appList = new ArrayList<>();
    private List<TagCapk> capkList = new ArrayList<>();
    private TagApp tagApp;
    private TagCapk tagCapk;
    public List<TagApp> getAppList() {
        return appList;
    }
    public List<TagCapk> getCapkList() {
        return capkList;
    }
    private int appIndex = 0;
    private int capkIndex = 0;
    private boolean paresTagApp = false;
    private boolean paresTagCapk = false;
    /**
     * 用来标识解析开始
     */
    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.startDocument();
        System.out.println("SAX解析开始");
    }

    /**
     * 用来标识解析结束
     */
    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.endDocument();
        System.out.println("SAX解析结束");
    }

    /**
     * 解析xml元素
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        //调用DefaultHandler类的startElement方法
        super.startElement(uri, localName, qName, attributes);
        value.delete(0,value.length());
        if (qName.equals("app")) {
            appIndex++;
            tagApp = new TagApp();
            paresTagApp = true;

        }else if (qName.equals("capk")) {
            capkIndex++;
            tagCapk = new TagCapk();
            paresTagCapk = true;
        }
    }



    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        //调用DefaultHandler类的endElement方法
        super.endElement(uri, localName, qName);
        if (qName.equals("app")) {
            appList.add(tagApp);
            tagApp = null;
            paresTagApp = false;
        }
        else if (qName.equals("capk")) {
            capkList.add(tagCapk);
            tagCapk = null;
            paresTagCapk = false;
        }
        else if (qName.startsWith("_")){
            matchDataAndKey(qName.substring(1,qName.length()));
        }
    }

    private void matchDataAndKey(String qName) {
        String realValue = value.toString().trim();
        String concat = null;
        switch (qName){
            case "9F06":
                if (paresTagApp) {
                     concat = EmvAppTag.Application_Identifier_AID_terminal.concat(realValue);
                    tagApp.setApplication_Identifier_AID_terminal(concat);
                }
                if (paresTagCapk){
                  concat = EmvCapkTag.RID.concat(realValue);
                    tagCapk.setRID(concat);
                }
                break;
            case "9F22":
                concat = EmvCapkTag.Public_Key_Index.concat(realValue);
                tagCapk.setPublic_Key_Index(concat);
                break;
            case "DF02":
                  concat = EmvCapkTag.Public_Key_Module.concat(realValue);
                tagCapk.setPublic_Key_Module(concat);
                break;
            case "DF03":
                  concat = EmvCapkTag.Public_Key_CheckValue.concat(realValue);
                tagCapk.setPublic_Key_CheckValue(concat);
                break;
            case "DF04":
                  concat = EmvCapkTag.Pk_exponent.concat(realValue);
                tagCapk.setPk_exponent(concat);
                break;
            case "DF05":
                  concat = EmvCapkTag.Expired_date.concat(realValue);
                tagCapk.setExpired_date(concat);
                break;
            case "DF06":
                  concat = EmvCapkTag.Hash_algorithm_identification.concat(realValue);
                tagCapk.setHash_algorithm_identification(concat);
                break;
            case "DF07":
                  concat = EmvCapkTag.Pk_algorithm_identification.concat(realValue);
                tagCapk.setPk_algorithm_identification(concat);
                break;

            case "9F15":
                  concat = EmvAppTag.Merchant_Category_Code.concat(realValue);
                tagApp.setMerchant_Category_Code(concat);
                break;
            case "9F09":
                  concat = EmvAppTag.Application_Version_Number.concat(realValue);
                tagApp.setApplication_Version_Number(concat);
                break;
            case "9F01":
                  concat = EmvAppTag.Acquirer_Identifier.concat(realValue);
                tagApp.setAcquirer_Identifier(concat);
                break;
            case "5F36":
                  concat = EmvAppTag.Transaction_Currency_Exponent.concat(realValue);
                tagApp.setTransaction_Currency_Exponent(concat);
                break;
            case "9F1E":
                  concat = EmvAppTag.Interface_Device_IFD_Serial_Number.concat(realValue);
                tagApp.setInterface_Device_IFD_Serial_Number(concat);
                break;
            case "9F1C":
                 concat = EmvAppTag.Terminal_Identification.concat(realValue);
                tagApp.setTerminal_Identification(concat);
                break;
            case "9F1B":
                 concat = EmvAppTag.Terminal_Floor_Limit.concat(realValue);
                tagApp.setTerminal_Floor_Limit(concat);
                break;
            case "9F1A":
                 concat = EmvAppTag.Terminal_Country_Code.concat(realValue);
                tagApp.setTerminal_Country_Code(concat);
                break;
            case "9F16":
                 concat = EmvAppTag.Merchant_Identifier.concat(realValue);
                tagApp.setMerchant_Identifier(concat);
                break;
            case "9F33":
                 concat = EmvAppTag.Terminal_Cterminal_contactless_transaction_limitapabilities.concat(realValue);
                tagApp.setTerminal_Cterminal_contactless_transaction_limitapabilities(concat);
                break;
            case "9F3D":
                 concat = EmvAppTag.Transaction_Reference_Currency_Exponent.concat(realValue);
                tagApp.setTransaction_Reference_Currency_Exponent(concat);
                break;
            case "9F3C":
                 concat = EmvAppTag.Transaction_Reference_Currency_Code.concat(realValue);
                tagApp.setTransaction_Reference_Currency_Code(concat);
                break;
            case "9F39":
                 concat = EmvAppTag.Point_of_Service_POS_EntryMode.concat(realValue);
                tagApp.setPoint_of_Service_POS_EntryMode(concat);
                break;
            case "9F35":
                 concat = EmvAppTag.Terminal_type.concat(realValue);
                tagApp.setTerminal_type(concat);
                break;
            case "9F40":
                 concat = EmvAppTag.Additional_Terminal_Capabilities.concat(realValue);
                tagApp.setAdditional_Terminal_Capabilities(concat);
                break;
            case "9F4E":
                 concat = EmvAppTag.Merchant_Name_and_Location.concat(realValue);
                tagApp.setMerchant_Name_and_Location(concat);
                break;
            case "9F66":
                 concat = EmvAppTag.Terminal_Default_Transaction_Qualifiers.concat(realValue);
                tagApp.setTerminal_Default_Transaction_Qualifiers(concat);
                break;

            case "DF13":
                 concat = EmvAppTag.TAC_Denial.concat(realValue);
                tagApp.setTAC_Denial(concat);
                break;
            case "DF12":
                 concat = EmvAppTag.TAC_Online.concat(realValue);
                tagApp.setTAC_Online(concat);
                break;
            case "DF11":
                 concat = EmvAppTag.TAC_Default.concat(realValue);
                tagApp.setTAC_Default(concat);
                break;
            case "DF01":
                 concat = EmvAppTag.Application_Selection_Indicator.concat(realValue);
                tagApp.setApplication_Selection_Indicator(concat);
                break;
            case "9F7B":
                 concat = EmvAppTag.Electronic_cash_Terminal_Transaction_Limit.concat(realValue);
                tagApp.setElectronic_cash_Terminal_Transaction_Limit(concat);
                break;
            case "9F73":
                 concat = EmvAppTag.Currency_conversion_factor.concat(realValue);
                tagApp.setCurrency_conversion_factor(concat);
                break;
            case "DF15":
                 concat = EmvAppTag.Threshold_Value_BiasedRandom_Selection.concat(realValue);
                tagApp.setThreshold_Value_BiasedRandom_Selection(concat);
                break;
            case "DF14":
                 concat = EmvAppTag.Default_DDOL.concat(realValue);
                tagApp.setDefault_DDOL(concat);
                break;
            case "DF16":
                 concat = EmvAppTag.Maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection.concat(realValue);
                tagApp.setMaximum_Target_Percentage_to_be_used_for_Biased_Random_Selection(concat);
                break;
            case "DF17":
                 concat = EmvAppTag.Target_Percentage_to_be_Used_for_Random_Selection.concat(realValue);
                tagApp.setTarget_Percentage_to_be_Used_for_Random_Selection(concat);
                break;
            case "DF19":
                 concat = EmvAppTag.terminal_contactless_offline_floor_limit.concat(realValue);
                tagApp.setTerminal_contactless_offline_floor_limit(concat);
                break;
            case "DF20":
                 concat = EmvAppTag.terminal_contactless_transaction_limit.concat(realValue);
                tagApp.setTerminal_contactless_transaction_limit(concat);
                break;
            case "DF21":
                 concat = EmvAppTag.terminal_execute_cvm_limit.concat(realValue);
                tagApp.setTerminal_execute_cvm_limit(concat);
                break;
            case "DF78":
                 concat = EmvAppTag.Contactless_CVM_Required_limit.concat(realValue);
                tagApp.setContactless_CVM_Required_limit(concat);
                break;
            case "DF70":
                 concat = EmvAppTag.Currency_Exchange_Transaction_Reference.concat(realValue);
                tagApp.setCurrency_Exchange_Transaction_Reference(concat);
                break;
            case "DF71":
                 concat = EmvAppTag.Script_length_Limit.concat(realValue);
                tagApp.setScript_length_Limit(concat);
                break;
            case "DF72":
                 concat = EmvAppTag.ICS.concat(realValue);
                tagApp.setICS(concat);
                break;
            case "DF73":
                 concat = EmvAppTag.status.concat(realValue);
                tagApp.setStatus(concat);
                break;
            case "DF74":
                 concat = EmvAppTag.Identity_of_each_limit_exist.concat(realValue);
                tagApp.setIdentity_of_each_limit_exist(concat);
                break;
            case "DF75":
                 concat = EmvAppTag.terminal_status_check.concat(realValue);
                tagApp.setTerminal_status_check(concat);
                break;
            case "DF79":
                 concat = EmvAppTag.ContactlessTerminal_Capabilities.concat(realValue);
                tagApp.setContactlessTerminal_Capabilities(concat);
                break;
            case "5F2A":
                 concat = EmvAppTag.Transaction_Currency_Code.concat(realValue);
                tagApp.setTransaction_Currency_Code(concat);
                break;
            case "DF7A":
                 concat = EmvAppTag.ContactlessAdditionalTerminal_Capabilities.concat(realValue);
                tagApp.setContactlessAdditionalTerminal_Capabilities(concat);
                break;
            case "DF76":
                 concat = EmvAppTag.Default_Tdol.concat(realValue);
                tagApp.setDefault_Tdol(concat);
                break;


        }
        if (TextUtils.isEmpty(concat))
            concat = qName;
        if (paresTagApp)
            tagApp.addData(concat);
        if (paresTagCapk)
            tagCapk.addData(concat);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        super.characters(ch, start, length);
        value.append(ch, start, length);

    }
}

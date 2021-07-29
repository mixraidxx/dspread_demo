package com.dspread.demoui;

/**
 * Time:2020/4/1
 * Author:Qianmeng Chen
 * Description:
 */
public class Test {
    public enum FORMATID {
        MKSK("001"), LP("02"), MKSK_PLAIN("03"), MOSAMBEE("04"), SOFTPAY("05"), DUKPT("06");
        private String id;
        FORMATID(String s) {
            this.id = s;
        }
    }

    public static void main(String[] args) {
        String s1 = FORMATID.DUKPT.id;
        String s2 = FORMATID.valueOf("MKSK").name();
        System.out.println("value = "+s1+" -- "+s2);
    }
}

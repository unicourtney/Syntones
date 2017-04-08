package com.syntones.remote;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.syntones.syntones_mobile.MainActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Courtney Love on 10/1/2016.
 */

public class IpAddressSetting {

  /*  MainActivity mainActivity = new MainActivity();*/

    String iPAddress = "192.168.1.5";
    public IpAddressSetting() {
    }

    public String getiPAddress() {
        return iPAddress;
    }

    public void setiPAddress(String iPAddress) {
        this.iPAddress = iPAddress;
    }
}

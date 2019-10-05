package com.example.jrnjsyx.beepbeep.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Common {

    public static boolean isDebug = true;
    public static void println(String str){
        if(isDebug){
            System.out.println(str);
        }
    }

    public static boolean isHostConnectable(String host, int port) {
        Socket sClient = null;
        try {
            SocketAddress saAdd = new InetSocketAddress(host, port);
            sClient = new Socket();
            sClient.bind(null);
            sClient.connect(saAdd, 10000);
        }
        catch (UnknownHostException e) {
            return false;
        }
        catch (SocketTimeoutException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            try {
                if (sClient != null) {
                    sClient.close();
                }
            }
            catch (Exception e) {
            }
        }
        return true;
    }
    public static boolean isHostReachable(String host, Integer timeOut) {
        try {
            return InetAddress.getByName(host).isReachable(timeOut);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}

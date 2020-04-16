package com.example.jrnjsyx.beepbeep.utils;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

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


    public static final String SDPATH = Environment.getExternalStorageDirectory()+ File.separator;//"/sdcard/";

    public static void createEmptyFile(String name){
        File file = new File(SDPATH+name+".txt");
        try {
            file.delete();
            file.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void saveShorts(short[] data, String name, boolean append){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file,append);
            if(!file.exists())
                file.createNewFile();
            for(int i = 0; i < data.length ; i++){
                fw.write(String.valueOf(data[i]) + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void saveStrings(List<String> data, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists())
                file.createNewFile();
            for(String str:data){
                fw.write(str + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void saveString(String data, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists()) {
                file.createNewFile();
            }
            fw.write(data + "\r\n");
            fw.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void saveInts(int[] data, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists())
                file.createNewFile();
            for(int i = 0; i < data.length ; i++){
                fw.write(String.valueOf(data[i]) + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void saveFloats(float[] data, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists())
                file.createNewFile();
            for(int i = 0; i < data.length ; i++){
                fw.write(String.valueOf(data[i]) + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void saveList(List data, String name){
        File file = new File(SDPATH+name+".txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            if(!file.exists())
                file.createNewFile();
            for(int i = 0; i < data.size() ; i++){
                fw.write(String.valueOf(data.get(i)) + "\r\n");
                fw.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(fw != null)
                    fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



    public static float[] readTxt(String name, int length){
        String tmp = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        float[] pcm = null;
        try{
            fileReader = new FileReader(new File(SDPATH + name));
            bufferedReader = new BufferedReader(fileReader);
            pcm = new float[length];
            int i = 0;
            while ((tmp = bufferedReader.readLine()) != null){
                pcm[i++] = (short) Float.parseFloat(tmp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader!= null)
                    bufferedReader.close();
                if(fileReader != null)
                    fileReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return pcm;
    }

    public static float[] readFilterCoefficient(String name, int length){
        String tmp = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        float[] filterCoefficient = null;
        try{
            fileReader = new FileReader(new File(SDPATH + name));
            bufferedReader = new BufferedReader(fileReader);
            filterCoefficient = new float[length];
            int i = 0;
            while ((tmp = bufferedReader.readLine()) != null){
                filterCoefficient[i++] = Float.parseFloat(tmp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader!= null)
                    bufferedReader.close();
                if(fileReader != null)
                    fileReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return filterCoefficient;
    }


}

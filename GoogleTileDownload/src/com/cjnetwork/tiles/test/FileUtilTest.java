package com.cjnetwork.tiles.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.junit.Test;

public class FileUtilTest {
	private static final Logger logger = Logger.getLogger(FileUtilTest.class); 
	
	/**
     * һ��һ�ж�ȡ�ļ����ʺ��ַ���ȡ������ȡ�����ַ�ʱ���������
     * 
     * ���Ĺر�˳���ȴ򿪵ĺ�أ���򿪵��ȹأ�
     *       �����п��ܳ���java.io.IOException: Stream closed�쳣
     * 
     * @throws IOException 
     */
    @Test
    public void readFile01() throws IOException {
        FileReader fr=new FileReader("D:/temp/work_release/evdokey_201103221556.txt");
        BufferedReader br=new BufferedReader(fr);
        String line="";
        String[] arrs=null;
        while ((line=br.readLine())!=null) {
            arrs=line.split(",");
            System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
        }
        br.close();
        fr.close();
    }
    
    /**
     * һ��һ�ж�ȡ�ļ��������ȡ�����ַ�ʱ��������
     * 
     * ���Ĺر�˳���ȴ򿪵ĺ�أ���򿪵��ȹأ�
     *       �����п��ܳ���java.io.IOException: Stream closed�쳣
     * 
     * @throws IOException 
     */
    @Test
    public void readFile02() throws IOException {
        FileInputStream fis=new FileInputStream("E:/phsftp/evdokey/evdokey_201103221556.txt");
        InputStreamReader isr=new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        //��д����
        //BufferedReader br = new BufferedReader(new InputStreamReader(
        //        new FileInputStream("E:/phsftp/evdokey/evdokey_201103221556.txt"), "UTF-8"));
        String line="";
        String[] arrs=null;
        while ((line=br.readLine())!=null) {
            arrs=line.split(",");
            System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
        }
        br.close();
        isr.close();
        fis.close();
    }
    
    /**
     * һ��һ��д���ļ����ʺ��ַ�д�룬��д�������ַ�ʱ���������
     * 
     * ���Ĺر�˳���ȴ򿪵ĺ�أ���򿪵��ȹأ�
     *       �����п��ܳ���java.io.IOException: Stream closed�쳣
     * 
     * @throws IOException 
     */
    @Test
    public void writeFile01() throws IOException {
        String[] arrs={
            "zhangsan,23,FuJian",
            "lisi,30,ShangHai",
            "wangwu,43,BeiJing",
            "laolin,21,ChongQing",
            "ximenqing,67,GuiZhou"
        };
        FileWriter fw=new FileWriter(new File("D:/temp/work_release/evdokey_201103221556.txt"));
        //д�������ַ�ʱ���������
        BufferedWriter  bw=new BufferedWriter(fw);
        //BufferedWriter  bw=new BufferedWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("E:/phsftp/evdokey/evdokey_201103221556.txt")), "UTF-8")));

        for(String arr:arrs){
            bw.write(arr+"\n");
        }
        bw.close();
        fw.close();
    }
    
    /**
     * һ��һ��д���ļ������д�������ַ�ʱ��������
     * 
     * ���Ĺر�˳���ȴ򿪵ĺ�أ���򿪵��ȹأ�
     *       �����п��ܳ���java.io.IOException: Stream closed�쳣
     * 
     * @throws IOException 
     */
    @Test
    public void writeFile02() throws IOException {
        String[] arrs={
                "zhangsan,23,����",
                "lisi,30,�Ϻ�",
                "wangwu,43,����",
                "laolin,21,����",
                "ximenqing,67,����"
        };
        //д�������ַ�ʱ���������������
        FileOutputStream fos=new FileOutputStream(new File("D:/temp/work_release/evdokey_201103221556.txt"),true);
        OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw=new BufferedWriter(osw);
        //��д���£�
        //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
        //        new FileOutputStream(new File("E:/phsftp/evdokey/evdokey_201103221556.txt")), "UTF-8"));

        for(String arr:arrs){
            bw.write(arr+"\n");
        }
        
        //ע��رյ��Ⱥ�˳���ȴ򿪵ĺ�رգ���򿪵��ȹر�
        bw.close();
        osw.close();
        fos.close();
    }
    
    public static void writeLine(BufferedWriter bw,String url) {
        try {
			bw.write(url+"\n");
		} catch (IOException e) {
			logger.error("д��һ���ļ�����!");
			throw new RuntimeException(e);
		}
	}
}

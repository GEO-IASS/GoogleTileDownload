package com.cjnetwork.tiles.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class UrlFileHandler {
	private static final Logger logger = Logger.getLogger(UrlFileHandler.class);
	private List<File> fileList=new ArrayList<File>();
	private Integer success=0,fail=0,all=0;
	private String path;
	private static String downloadDir;

	public UrlFileHandler() {
	}
	public UrlFileHandler(String path) {
		this.path=path;
		downloadDir=ConfigUtil.get("downloadDir");
	}

	public void getAllUrlFile(String rootPath, List<File> fileList) {
		File file = new File(rootPath);
		File[] files = file.listFiles(new UrlFileFilter());
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getAllUrlFile(files[i].getPath(), fileList);
			} else {
				fileList.add(files[i]);
			}
		}
	}
	
	public void execute() {
		getAllUrlFile(path,fileList);
		for (File file : fileList) {
			parseFile(file);
		}
		logger.info("一共"+all+"个,成功"+success+",失败"+fail+"个");
	}

	public void parseFile(File file) {
		logger.debug("正在处理文件:" + file.getAbsolutePath());
		if (!file.exists()) {
			throw new RuntimeException("文件不存在!");
		}
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				String[] arr = lineTxt.split(",");
				int x = Integer.parseInt(arr[0]);
				int y = Integer.parseInt(arr[1]);
				int z = Integer.parseInt(arr[2]);
				all++;
				download(x, y, z);
			}
			read.close();
			
			if (file.delete()) {
				logger.warn("文件"+file.getAbsolutePath()+"删除成功!");
			}else {
				logger.info("文件"+file.getAbsolutePath()+"删除失败!");
			}
			
		} catch (IOException e) {
			logger.error("解析文件" + file.getName() + "失败");
			throw new RuntimeException(e);
		}
	}

	public void download(int x, int y, int z) {
		String url = "http://mt1.google.cn/vt/lyrs=m@216000000&hl=zh-CN&gl=CN&src=app&s=Galileo&x=" + x + "&y=" + y + "&z=" + z;
//		String target = downloadDir + "/tiles/" + z + "/" + x + "/" + y + ".png";
		String target = downloadDir + "/" + z + "/" + x + "/" + y + ".png";
		DownloadUtil.setProxy(false);
		try {
			DownloadUtil.download(url, target, true);
			logger.debug("(" + x + "," + y + "," + z + ")下载成功!");
			success++;
		} catch (Exception e) {
			logger.error(e);
			fail++;
			logger.error("(" + x + "," + y + "," + z + ")下载失败!");
		}
	}
}

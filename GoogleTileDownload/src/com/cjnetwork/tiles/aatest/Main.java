package com.cjnetwork.tiles.aatest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;
import com.cjnetwork.tiles.model.Lnglat;
import com.cjnetwork.tiles.model.Tile;
import com.cjnetwork.tiles.util.ConfigUtil;
import com.cjnetwork.tiles.util.CoordinateUtil;
import com.cjnetwork.tiles.util.DownloadUtil;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	private static final int MODE_FILE = 0;
	private static final int MODE_PIC = 1;
	private static int mode = MODE_FILE;	// 默认为保存文件
	static String path = "http://mt1.google.cn/vt/lyrs=m@216000000&hl=zh-CN&gl=CN&src=app&s=Galileo&x=";
	static ExecutorService pool;
	static String downloadDir; 			// 下载的瓦片的存放目录
	static int[] zoom; 					// 放大倍数
	static Lnglat leftTopLnglat; 		// 左上角经纬度
	static Lnglat rightBottomLnglat; 	// 右下角经纬度
	static int roundCount;
	static int totalSize;
	static int currentIndex = 0;
	static int failedCount = 0;
	static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	static ReentrantReadWriteLock lockFailCount = new ReentrantReadWriteLock();

	/**
	 * 功能描述：<br>
	 * 当前处理索引加1
	 * @return void 修改记录:
	 */
	public static int addCurrentIndex() {
		synchronized (lock.writeLock()) {
			try {
				lock.writeLock().lock();
				currentIndex++;
			} finally {
				lock.writeLock().unlock();
			}
		}
		return currentIndex;
	}

	public static void addFailedCount() {
		synchronized (lockFailCount.writeLock()) {
			try {
				lockFailCount.writeLock().lock();
				failedCount++;
			} finally {
				lockFailCount.writeLock().unlock();
			}
		}
	}

	public static int getFailedCount() {
		int count = 0;
		synchronized (lockFailCount.readLock()) {
			try {
				lockFailCount.readLock().lock();
				count = failedCount;
			} finally {
				lockFailCount.readLock().unlock();
			}
		}
		return count;
	}

	public static void main(String[] args) {
		init();
		printCalculatSize(zoom, leftTopLnglat, rightBottomLnglat);
		startDownload(zoom, leftTopLnglat, rightBottomLnglat);
	}

	/**
	 * 功能描述：<br>
	 * 初始化
	 * @return void 修改记录:
	 */
	private static void init() {
		// 设置代理
		try {
			boolean isProxy = false;
			isProxy = Boolean.valueOf(ConfigUtil.get("isProxy"));
			if (isProxy) {
				DownloadUtil.setProxy(isProxy);
				DownloadUtil.setProxyAddress(ConfigUtil.get("proxyAddress"));
				try {
					DownloadUtil.setProxyPort(Integer.parseInt(ConfigUtil.get("proxyPort")));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}

		// 获取存放目录
		downloadDir = ConfigUtil.get("downloadDir");

		// 创建线程池
		pool = Executors.newFixedThreadPool(Integer.parseInt(ConfigUtil.get("threadCount")));
		roundCount = Integer.parseInt(ConfigUtil.get("roundCount").trim());

		// 获取放大倍数
		String[] tmpZoom = ConfigUtil.get("zoom").split(",");
		zoom = new int[tmpZoom.length];
		for (int i = 0, len = zoom.length; i < len; i++) {
			zoom[i] = Integer.parseInt(tmpZoom[i].trim());
		}

		// 计算经纬度范围
		String[] tmpLngLat = ConfigUtil.get("leftTopLnglat").split(",");
		leftTopLnglat = new Lnglat(Double.valueOf(tmpLngLat[0].trim()), Double.valueOf(tmpLngLat[1].trim()));
		tmpLngLat = ConfigUtil.get("rightBottomLnglat").split(",");
		rightBottomLnglat = new Lnglat(Double.valueOf(tmpLngLat[0].trim()), Double.valueOf(tmpLngLat[1].trim()));
	}

	private static void printCalculatSize(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		int size = calculateDownloadSize(zoom, leftTopLnglat, rightBottomLnglat);
		System.out.println("下载tile数量:" + size);
		System.out.println("下载大小:" + size * 22 + "k, " + size * 22 / 1024 + "M, " + size * 22 / 1024 / 1024 + "G");
	}

	private static int calculateDownloadSize(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		int size = 0;
		for (int i = 0, len = zoom.length; i < len; i++) {
			Tile leftTopTile = CoordinateUtil.lnglatToTile(zoom[i], leftTopLnglat);
			Tile rightBottomTile = CoordinateUtil.lnglatToTile(zoom[i], rightBottomLnglat);
			size += (rightBottomTile.getX() - leftTopTile.getX() + 1) * (rightBottomTile.getY() - leftTopTile.getY() + 1);
		}
		return size;
	}

	private static void startDownload(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		totalSize = calculateDownloadSize(zoom, leftTopLnglat, rightBottomLnglat);

		List<Tile> tmpTileList = new ArrayList<Tile>();
		int tmpTileListSize = 0;
		for (int i = 0, len = zoom.length; i < len; i++) {
			Tile leftTopTile = CoordinateUtil.lnglatToTile(zoom[i], leftTopLnglat);
			Tile rightBottomTile = CoordinateUtil.lnglatToTile(zoom[i], rightBottomLnglat);

			for (int x = leftTopTile.getX(); x <= rightBottomTile.getX(); x++) {
				for (int y = leftTopTile.getY(); y <= rightBottomTile.getY(); y++) {
					tmpTileList.add(new Tile(x, y, zoom[i]));
					tmpTileListSize++;
					if (tmpTileListSize >= roundCount) {
						tmpTileListSize = 0;
						// startDownloadThread(tmpTileList);
						startSavefile(tmpTileList);
					}
				}
			}
		}
		if (tmpTileListSize != 0) {
			tmpTileListSize = 0;
			if (mode == MODE_FILE) {
				startSavefile(tmpTileList);
			} else {
				startDownloadThread(tmpTileList);
			}
		}
	}

	/**
	 * 功能描述：<br>
	 * 开始下载线程
	 * @param tmpTileList
	 * @return void 修改记录:
	 */
	private static void startDownloadThread(List<Tile> tmpTileList) {
		final Tile[] threadTaskTiles = tmpTileList.toArray(new Tile[0]);
		tmpTileList.clear();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				for (int i = 0, len = threadTaskTiles.length; i < len; i++) {
					try {
						logger.debug("进度：第 " + addCurrentIndex() + " 个，总 " + totalSize + " 个，失败 " + Main.getFailedCount() + " 个");
						downloadXYZ(threadTaskTiles[i]);
					} catch (Exception e) {
						Main.addFailedCount();
						logger.error(e);
						logger.error("下载失败:x(" + threadTaskTiles[i].getX() + "),y(" + threadTaskTiles[i].getY() + "),z(" + threadTaskTiles[i].getZoom() + ")");
					}
				}
			}
		});
	}

	/**
	 * 保存瓦片图片
	 * @param tile
	 * @throws Exception
	 */
	private static void downloadXYZ(Tile tile) throws Exception {
		// String url = "http://mt0.googleapis.com/vt?src=apiv3&x=" +tile.getX() + "&y=" + tile.getY() + "&z=" + tile.getZoom();
		String url = path + tile.getX() + "&y=" + tile.getY() + "&z=" + tile.getZoom();
		String storePath = downloadDir + "/tiles/" + tile.getZoom() + "/" + tile.getX() + "/" + tile.getY() + ".png";
		storePath = storePath.replace("//", "/");
		DownloadUtil.download(url, storePath, false);
	}

	/**
	 * 功能描述：<br>
	 * 开始保存url文件
	 * @param tmpTileList
	 * @return void 修改记录:
	 */
	private static void startSavefile(final List<Tile> tmpTileList) {
		final Tile[] threadTaskTiles = tmpTileList.toArray(new Tile[0]);
		tmpTileList.clear();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				saveTileList(threadTaskTiles);
			}
		});
	}

	private static void saveTileList(Tile[] tmpTileList) {
		// Map<Integer, List<String>> map=new HashMap<Integer, List<String>>();
		Map<Integer, List<Tile>> map = new HashMap<Integer, List<Tile>>();
		List<Tile> list = null;
		Integer z;
		for (Tile t : tmpTileList) {
			try {
				logger.info("进度：第 " + addCurrentIndex() + " 个，总 " + totalSize + " 个，失败 " + Main.getFailedCount() + " 个");
				z = t.getZoom();
				if (!map.containsKey(z)) {
					list = new ArrayList<Tile>();
					map.put(z, list);
				} else {
					list = map.get(z);
				}
				list.add(t);
			} catch (Exception e) {
				Main.addFailedCount();
				e.printStackTrace();
				logger.error("下载失败:x(" + t.getX() + "),y(" + t.getY() + "),z(" + t.getZoom() + ")");
			}

		}
		saveUrlList(map);
	}

	private static Map<Integer, List<String>> convertTileList(List<Tile> tmpTileList) {
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		List<String> list = null;
		Integer x, z;
		for (Tile t : tmpTileList) {
			x = t.getX();
			z = t.getZoom();
			String line = "x=" + x + ",y=" + t.getY() + ",z=" + z;
			if (!map.containsKey(x)) {
				list = new ArrayList<String>();
				list.add(line);
			} else {
				list = map.get(x);
			}
			list.add(line);
		}
		return map;
	}

	/**
	 * 将url追加写入文件
	 * @param map
	 * @throws IOException
	 */
	private static void saveUrlList(Map<Integer, List<Tile>> map) {
		File file = null;
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		String line = null;
		Integer z=null, x=null;
		for (Map.Entry<Integer, List<Tile>> entry : map.entrySet()) {
			List<Tile> tiles = entry.getValue();
			for (Tile t : tiles) {
				try {
					z = entry.getKey();
//					String storePath = downloadDir + "/tiles/" + z + "/" + t.getX() + ".txt";
					String storePath = downloadDir + "/"+z + "/" + t.getX() + ".txt";
					file = new File(storePath);
					if (!file.exists()) {
						File parentDir = new File(file.getParent());
						if (!parentDir.exists()) {
							parentDir.mkdirs();
						}
					}
					fos = new FileOutputStream(file, true);
					osw = new OutputStreamWriter(fos, "UTF-8");
					bw = new BufferedWriter(osw);
//					line = "x=" + t.getX() + ",y=" + t.getY() + ",z=" + z;
					line = t.getX() + "," + t.getY() + "," + z;
					bw.write(line + "\n");
					bw.close();
					osw.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("x=" + x + "下载出错!");
				}
			}
		}
	}

}

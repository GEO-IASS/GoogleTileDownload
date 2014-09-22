package com.cjnetwork.tiles.util;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.cjnetwork.tiles.model.Lnglat;
import com.cjnetwork.tiles.model.Tile;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * <p>file name: CoordinateUtil.java</p>
 * <p>despription: lnglat 和tile 坐标系转换工具</p>
 */
public class CoordinateUtil {
	static{
		DOMConfigurator.configure("D:/Workspace_sts3.5/GoogleTileDownload/src/log4j.xml");
	}
	private static final Logger logger = Logger.getLogger(CoordinateUtil.class);
	private static Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:900913");
	public static double[] cast(double lon, double lat) {
		Point2D.Double src = new Point2D.Double(lon, lat);
		Point2D.Double dst = new Point2D.Double(0, 0);
		proj.transform(src, dst);
		return new double[] { dst.x, dst.y };
	}
	
	/**
	 * 功能描述：<br>
	 * 将tile坐标系转换为lnglat坐标系
	 * 
	 * @param tile
	 * @return Lnglat
	 */
	public static Lnglat tileToLnglat(Tile tile){
		double n = Math.pow(2, tile.getZoom());
		double lng = tile.getX() / n * 360.0 - 180.0;
		double lat = Math.atan(Math.sinh(Math.PI * (1 - 2 * tile.getY() / n)));
		lat = lat * 180.0 / Math.PI;
		return new Lnglat(lng, lat);
	}
	
	/**
	 * 功能描述：<br>
	 * 将lnglat坐标系转换为tile坐标系
	 * 
	 * @param zoom
	 *            缩放级别
	 * @param lnglat
	 * @return Tile 
	 */
	public static Tile lnglatToTile(int zoom, Lnglat lnglat){
//		double n = Math.pow(2, zoom);
//		double tileX = ((lnglat.getLng() + 180) / 360) * n;
//		double tileY = (1 - (Math.log(Math.tan(Math.toRadians(lnglat.getLat())) + (1 / Math.cos(Math.toRadians(lnglat.getLat())))) / Math.PI)) / 2 * n;
//		return new Tile(new Double(tileX).intValue(), new Double(tileY).intValue(), zoom);
		
//		double res = 9.554628533935547;
//		double maxExtentLeft=-20037508.342789;
//		double maxExtentTop=20037508.342789;
//		double[] lonlat=cast(lnglat.getLng(),lnglat.getLat());
//		double boundsLeft=lonlat[0];
//		double boundsTop=lonlat[1];
//		int x = (int) Math.round((boundsLeft -maxExtentLeft) / (res * 256));
//		int y = (int) Math.round((maxExtentTop- boundsTop) / (res * 256));
//		return new Tile(x,y, zoom);
		return lonlatToTile(zoom,lnglat);
	}
	
	public static Tile lonlatToTile(int zoom, Lnglat lnglat){
//		this.maxExtent.left=-20037508.342789 
//		this.maxExtent.top=20037508.342789 
//		this.tileSize.w=256 
//		double[] resArr={4.777314266967774,9.554628533935547,19.109257067871095,38.21851413574219};
		double n = Math.pow(2, 14-zoom);	// 以14作为默认放大倍数，对应的放大系数为9.554628533935547
//		double res = resArr[14-zoom];
		double res = 9.554628533935547*n;
		logger.debug("res="+res);
		double maxExtentLeft=-20037508.342789;
		double maxExtentTop=20037508.342789;
		double[] lonlat=cast(lnglat.getLng(),lnglat.getLat());
		double boundsLeft=lonlat[0];
		double boundsTop=lonlat[1];
		logger.debug("boundsLeft="+boundsLeft);
		logger.debug("boundsTop="+boundsTop);
		int x = (int) Math.round((boundsLeft -maxExtentLeft) / (res * 256));
		int y = (int) Math.round((maxExtentTop- boundsTop) / (res * 256));
		return new Tile(x,y, zoom);
	}
	
	public static void main(String[] args) {
//		Lnglat lnglat = new Lnglat(106.171875, 29.84064389983442);
//		Lnglat lnglat = new Lnglat(120.171875, 29.84064389983442);
//		System.out.println(lnglatToTile(10, lnglat));

//		Tile tile = new Tile(814, 423, 10);
//		System.out.println(tile);
//		System.out.println(tileToLnglat(tile));
		
		Lnglat lnglat = new Lnglat(120.15434, 30.27491);
		System.out.println(lonlatToTile(12,lnglat));
		System.out.println("complete...");
	}
}

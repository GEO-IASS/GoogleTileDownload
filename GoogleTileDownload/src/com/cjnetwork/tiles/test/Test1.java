package com.cjnetwork.tiles.test;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.cjnetwork.tiles.model.Tile;
import com.cjnetwork.tiles.util.CoordinateConversion;
import com.cjnetwork.tiles.util.UrlFileHandler;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

public class Test1 {
	private static Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:900913");
//	private static Projection proj1 = ProjectionFactory.getNamedPROJ4CoordinateSystem("EPSG:4326");

	public static double[] cast(double lon, double lat) {
		Point2D.Double src = new Point2D.Double(lon, lat);
		Point2D.Double dst = new Point2D.Double(0, 0);
		proj.transform(src, dst);
		return new double[] { dst.x, dst.y };
	}

	public static double[] cast1(double lon, double lat) {
		Point2D.Double src = new Point2D.Double(lon, lat);
		Point2D.Double dst = new Point2D.Double(0, 0);
		proj.transform(src, dst);
		return new double[] { dst.x, dst.y };
	}

	@Test
	public void test1() {
		// lon: 13378459.553503, lat: 3534958.0561278
		// 120.1713914 30.237369
		// 51 R 226246 3352677
		 CoordinateConversion conversion=new CoordinateConversion();
		 String utm=conversion.latLon2UTM(30.27491,120.15434);
		 System.out.println("utm="+utm);

		//lon: 13375519.943539977, lat: 3538936.131882049
		//120.15434, 30.27491
		//[1.3375519945401864E7, 3538936.1323746727]
//		System.out.println(Arrays.toString(cast(120.15434, 30.27491)));
	}
	
	// 根据某个目录下的url文件下载瓦片
	@Test
	public void test2() {
		String path="D:/temp/work_release/tiles/16";
		UrlFileHandler handler=new UrlFileHandler(path);
		handler.execute();
	}
	
}

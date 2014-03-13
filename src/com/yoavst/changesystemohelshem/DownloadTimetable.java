package com.yoavst.changesystemohelshem;

import java.io.IOException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;

/**
 * Only used to create timetable for the whole school in JSON. The class isn't
 * used in the application, but should be used to generate timetable if changed.
 * 
 * @author Yoav Sternberg
 */
@EBean
public class DownloadTimetable {
	@Background
	void download() {
		try {
			/*
			 * Level I: Layer Level II: Class Level III: Day Level IV: Hour
			 */
			String[][][][] classTimeTable = new String[4][][][];
			classTimeTable[0] = new String[12][6][10];
			classTimeTable[1] = new String[11][6][10];
			classTimeTable[2] = new String[11][6][10];
			classTimeTable[3] = new String[11][6][10];
			// For each layer
			for (int mLayer = 9; mLayer <= 12; mLayer++) {
				// For each class in layer
				for (int mClass = 1; mClass <= classTimeTable[mLayer - 9].length; mClass++) {
					// Download the class's timetable
					Document doc = Jsoup.connect(
							"http://www.ohel-shem.com/new_schedule/index.php?layer="
									+ mLayer + "&class=" + mClass).get();
					// Get the timetable body
					Element tableBody = doc.select(".schedule tbody").first();
					// Skip the first row and column and save the data
					boolean skippedRowOne = false;
					int hour = 1;
					for (Element row : tableBody.select("tr")) {
						if (!skippedRowOne) {
							skippedRowOne = true;
							continue;
						}
						boolean skippedColumnOne = false;
						int day = 1;

						for (Element cell : row.select("td")) {
							if (!skippedColumnOne) {
								skippedColumnOne = true;
								continue;
							}
							if (hour <= 10)
								classTimeTable[mLayer - 9][mClass - 1][day - 1][hour - 1] = cell
										.text();
							day++;
						}
						hour++;
						day = 1;
					}
				}
			}
			// The timetable converted to JSON
			@SuppressWarnings("unused")
			String gson = new Gson().toJson(classTimeTable);
			// TODO do something with the String (E.G: update R.raw.timetable)
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

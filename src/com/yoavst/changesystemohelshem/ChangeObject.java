package com.yoavst.changesystemohelshem;

import java.io.Serializable;

/**
 * In this object you store all the info about one lesson.
 * Each Object stores the following data:
 * <ul>
 * <li> The lesson number
 * <li> The change text
 * <li> The lesson name
 * <li> The color of the change
 * </ul>
 * @author Yoav Sternberg
 */
public class ChangeObject implements Serializable {
	/**
	 * Requiered by Java
	 */
	private static final long serialVersionUID = -5347845913979770686L;
	private final int lessonNumber;
	private final String changeText;
	private final String lesson;
	private final int changeColor;

	public ChangeObject(int lessonNumber, String changeText, String lesson,
			int changeColor) {
		super();
		this.lessonNumber = lessonNumber;
		this.changeText = changeText;
		this.lesson = lesson;
		this.changeColor = changeColor;
	}

	public int getLessonNumber() {
		return lessonNumber;
	}

	public String getChangeText() {
		return changeText;
	}

	public String getLesson() {
		return lesson;
	}

	public int getColor() {
		return changeColor;
	}

	@Override
	public String toString() {
		return "ChangeObject [lessonNumber=" + lessonNumber + ", changeText="
				+ changeText + ", lesson=" + lesson + ", changeColor="
				+ changeColor + "]";
	}

}

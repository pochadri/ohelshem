package com.yoavst.changesystemohelshem.views;

import it.gmariotti.cardslib.library.internal.CardExpand;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yoavst.changesystemohelshem.R;

/**
 * Card expend class that expend inside the card.
 * 
 * @author Based on Gabriel Mariotti demo app for CardsLib.
 */
public class CardExpandInside extends CardExpand {
	int mLocation;
	Context mContext;
	String[] mText;

	public CardExpandInside(Context context, int location, String[] text) {
		super(context, R.layout.card_expend_area);
		// Save the index and String array
		mLocation = location;
		mText = text;
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		// Set text for the expand area
		TextView textView = (TextView) view
				.findViewById(R.id.card_expend_textview);
		String text = mText[mLocation];
		textView.setText(Html.fromHtml(text));
		// If text include links
		if (text.contains("</a>")) {
			// Make the links clickable
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
}
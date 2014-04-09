package com.yoavst.changesystemohelshem.views;

import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.Context;

import com.yoavst.changesystemohelshem.R;

/**
 * Card Header class but RTL.
 */
public class CardHeaderRtl extends CardHeader {
	/**
	 * Infalte the RTL inner header. For complete RTL header, you should infalte
	 * the whole RTL header in the card xml.
	 */
	public CardHeaderRtl(Context context) {
		super(context, R.layout.card_inner_header);
	}

}
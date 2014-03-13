package com.yoavst.changesystemohelshem.fragments;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import com.yoavst.changesystemohelshem.MyApp;
import com.yoavst.changesystemohelshem.R;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * This Fragment use CardsLib by Gabriele Mariotti.
 * 
 * @author Yoav Sternberg
 */
@EFragment(R.layout.fragment_help)
public class HelpFragment extends SherlockFragment {
	int[] cardsViewsIds = new int[] { R.id.card_question1, R.id.card_question2,
			R.id.card_question3, R.id.card_question4, R.id.card_question5,
			R.id.card_question6, R.id.card_question7, R.id.card_question8,
			R.id.card_question9, R.id.card_question10, R.id.card_question11 };

	@AfterViews
	void initCards() {
		Card[] cards = MyApp.getHelpCards();
		if (cards != null) {
			for (int i = 0; i < cards.length; i++) {
				// Get CardView
				CardView cardView = (CardView) getActivity().findViewById(
						cardsViewsIds[i]);
				// Make the card to be expandable on clicking everywhere on the card
				ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand
						.builder().highlightView(false).setupView(cardView);
				cards[i].setViewToClickToExpand(viewToClickToExpand);
				// Set the card to the CardView
				cardView.setCard(cards[i]);
			}
		}
	}

}

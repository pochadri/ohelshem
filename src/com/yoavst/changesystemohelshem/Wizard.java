package com.yoavst.changesystemohelshem;

import android.content.Context;
import android.content.res.Resources;
import co.juliansuarez.libwizardpager.wizard.model.AbstractWizardModel;
import co.juliansuarez.libwizardpager.wizard.model.BranchPage;
import co.juliansuarez.libwizardpager.wizard.model.CustomerInfoPage;
import co.juliansuarez.libwizardpager.wizard.model.PageList;
import co.juliansuarez.libwizardpager.wizard.model.SingleFixedChoicePage;

/**
 * The wizard class. There are some problems with this class and SetupActivity,
 * but it somehow works.
 */
public class Wizard extends AbstractWizardModel {
	String chooseClass;

	/**
	 * @param context
	 *            Should be the app context and not Fragment or Activity context
	 */
	public Wizard(Context context) {
		super(context);

	}

	@Override
	protected PageList onNewRootPageList() {
		Resources res = mContext.getResources();
		chooseClass = res.getString(R.string.choose_class);
		BranchPage selectLayer = new BranchPage(this,
				res.getString(R.string.choose_layer));
		selectLayer.setRequired(true);
		String[] layers = res.getStringArray(R.array.layers);
		int[] classes = res.getIntArray(R.array.layers_classes);
		for (int i = 0; i < layers.length; i++) {
			selectLayer.addBranch(layers[i],
					getPageOfSelectClassNumber(classes[i], layers[i]));
		}
		SingleFixedChoicePage page = new SingleFixedChoicePage(this,
				res.getString(R.string.want_get_update));
		page.setChoices(res.getString(R.string.yes), res.getString(R.string.no))
				.setValue(res.getString(R.string.no));
		page.setRequired(true);
		return new PageList(selectLayer, page, new CustomerInfoPage(this,
				"Your info").setRequired(true));
	}

	private SingleFixedChoicePage getPageOfSelectClassNumber(
			int numberOfClasses, String className) {
		SingleFixedChoicePage page = new SingleFixedChoicePage(this,
				chooseClass);
		String[] classes = new String[numberOfClasses];
		for (int i = 1; i <= numberOfClasses; i++) {
			classes[i - 1] = className + "'" + i;
		}
		page.setChoices(classes).setRequired(true);
		return page;
	}
}

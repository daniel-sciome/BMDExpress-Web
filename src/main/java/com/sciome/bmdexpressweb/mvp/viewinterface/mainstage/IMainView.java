package com.sciome.bmdexpressweb.mvp.viewinterface.mainstage;

/**
 * View interface for the main application window.
 * Defines methods for updating UI labels and showing dialogs.
 * Copied from desktop application (100% reusable - pure Java interface).
 */
public interface IMainView
{

	public void updateProjectLabel(String label);

	public void updateSelectionLabel(String label);

	public void updateActionStatusLabel(String label);

	public void showErrorAlert(String message);

	public void showMessageDialog(String message);

	public void showWarningDialog(String message);

}

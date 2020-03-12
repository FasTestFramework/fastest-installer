package com.infogain.automation.installer.actions;

import com.infogain.automation.installer.AutomationSoftwareUpdateAction;

public class AutomationValidationAction extends AutomationSoftwareUpdateAction {
	
	@Override
	public int execute() {
		return 0;
	}
	@Override
	public boolean validateAction() {
		return false;
	}


	
	@Override
	public int undoAutomationSoftwareUpdateAction() {
		return 0;
	}

	
	@Override
	public int cleanUp() {
		return 0;
	}
}

package app.commands;

import java.io.IOException;

public class GuidedMerge extends Command {

	@Override
	public void runCommand() throws IOException {
		Command subCommand = new PartialMerge();
		subCommand.args = this.args;
		subCommand.commandOptions = this.commandOptions;
		subCommand.runCommand();
		
		
	}

}
